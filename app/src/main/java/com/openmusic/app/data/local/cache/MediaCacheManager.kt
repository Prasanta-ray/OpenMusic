package com.openmusic.app.data.local.cache

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.SimpleCache
import com.openmusic.app.MainApplication
import com.openmusic.app.data.local.dao.SongDao
import com.openmusic.app.data.local.database.AppDatabase
import com.openmusic.app.data.local.entities.SongEntity
import com.openmusic.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Encapsulates operations with ExoPlayer's SimpleCache singleton, manages offline track
 * downloads via CacheWriter, and synchronizes local download states with Room's SongDao off the main thread.
 */
@OptIn(UnstableApi::class)
class MediaCacheManager(
    private val application: Application,
    private val songDao: SongDao = AppDatabase.getDatabase(application).songDao(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val activeWriters = ConcurrentHashMap<String, CacheWriter>()
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    val simpleCache: SimpleCache
        get() = MainApplication.getMediaCache(application)

    /**
     * Checks whether a given audio resource is cached locally.
     * If [length] is greater than 0, verifies the entire span [0, length];
     * otherwise, checks whether the initial chunk is present.
     */
    fun isFullyCached(cacheKey: String, length: Long = -1L): Boolean {
        return if (length > 0) {
            simpleCache.isCached(cacheKey, 0L, length)
        } else {
            val contentLength = ContentMetadata.getContentLength(simpleCache.getContentMetadata(cacheKey))
            if (contentLength > 0) {
                simpleCache.isCached(cacheKey, 0L, contentLength)
            } else {
                simpleCache.isCached(cacheKey, 0L, 1L)
            }
        }
    }

    /**
     * Queries whether a specific segment is present in cache.
     */
    fun isCached(cacheKey: String, position: Long = 0L, length: Long = 1L): Boolean {
        return simpleCache.isCached(cacheKey, position, length)
    }

    /**
     * Returns total cached bytes for a specific cache key.
     */
    fun getCachedBytes(cacheKey: String): Long {
        return simpleCache.getCachedBytes(cacheKey, 0L, Long.MAX_VALUE)
    }

    /**
     * Returns the aggregate cache size across all cached audio files.
     */
    fun getTotalCacheSpace(): Long {
        return try {
            simpleCache.cacheSpace
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Evicts all cached audio files from disk and returns the total freed bytes.
     */
    fun clearAllCache(): Long {
        return try {
            val freed = simpleCache.cacheSpace
            val keys = simpleCache.keys.toList()
            for (key in keys) {
                try {
                    simpleCache.removeResource(key)
                } catch (_: Exception) {}
            }
            freed
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Downloads an entire track into SimpleCache using [CacheWriter] in the background.
     * Reports live progress to [_downloadProgress] and persists offline status in Room upon completion.
     */
    suspend fun downloadTrack(
        song: Song,
        streamUrl: String
    ): Result<Unit> = withContext(ioDispatcher) {
        val cacheKey = song.cacheKey ?: song.videoId
        val dataSpec = DataSpec.Builder()
            .setUri(Uri.parse(streamUrl))
            .setKey(cacheKey)
            .setFlags(DataSpec.FLAG_ALLOW_CACHE_FRAGMENTATION)
            .build()

        val cacheDataSource = createCacheDataSourceFactory().createDataSource() as CacheDataSource
        var lastProgress = 0f
        _downloadProgress.update { it + (song.videoId to 0.01f) }
        android.util.Log.d("MediaCacheManager", "downloadTrack starting for ${song.title} (${song.videoId})")

        val cacheWriter = CacheWriter(
            cacheDataSource,
            dataSpec,
            ByteArray(128 * 1024),
            CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
                val progress = if (requestLength > 0) {
                    (bytesCached.toFloat() / requestLength).coerceIn(0f, 1f)
                } else {
                    -1f
                }
                if (progress < 0f || progress - lastProgress >= 0.02f || progress >= 1f) {
                    lastProgress = progress
                    _downloadProgress.update { it + (song.videoId to progress) }
                    android.util.Log.d("MediaCacheManager", "downloadTrack progress for ${song.videoId}: $progress ($bytesCached / $requestLength)")
                }
            }
        )

        activeWriters[song.videoId] = cacheWriter

        try {
            cacheWriter.cache()
            android.util.Log.d("MediaCacheManager", "CacheWriter completed for ${song.videoId}")

            // Persist song with isDownloaded = true and cacheKey into Room
            val updatedEntity = SongEntity(
                videoId = song.videoId,
                title = song.title,
                artist = song.artist,
                thumbnailUrl = song.thumbnailUrl,
                durationMs = song.durationMs,
                streamUrl = streamUrl,
                cacheKey = cacheKey,
                isDownloaded = true,
                addedTimestamp = if (song.addedTimestamp > 0) song.addedTimestamp else System.currentTimeMillis(),
                lastPlayedTimestamp = song.lastPlayedTimestamp
            )
            songDao.insertSong(updatedEntity)
            songDao.updateDownloadStatus(song.videoId, isDownloaded = true, cacheKey = cacheKey)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) {
                cacheWriter.cancel()
            }
            Result.failure(e)
        } finally {
            activeWriters.remove(song.videoId)
            _downloadProgress.update { it - song.videoId }
        }
    }

    /**
     * Cancels an ongoing download job and cleans up active writers.
     */
    fun cancelDownload(videoId: String) {
        activeWriters[videoId]?.cancel()
        activeWriters.remove(videoId)
        _downloadProgress.update { it - videoId }
    }

    /**
     * Evicts and deletes all cached media chunks for the given [cacheKey] from disk.
     */
    fun removeCache(cacheKey: String) {
        simpleCache.removeResource(cacheKey)
    }

    /**
     * Deletes the cached audio files from disk and updates Room's download status.
     * Executed safely on [ioDispatcher].
     */
    suspend fun removeTrackCacheAndSync(videoId: String, cacheKey: String?) = withContext(ioDispatcher) {
        cancelDownload(videoId)
        val key = cacheKey ?: videoId
        removeCache(key)
        songDao.updateDownloadStatus(videoId, isDownloaded = false, cacheKey = null)
    }

    /**
     * Synchronizes a track's download status with Room.
     * Executed safely on [ioDispatcher].
     */
    suspend fun syncDownloadStatus(
        videoId: String,
        cacheKey: String?,
        isDownloaded: Boolean
    ) = withContext(ioDispatcher) {
        songDao.updateDownloadStatus(videoId, isDownloaded, cacheKey)
    }

    /**
     * Creates an ExoPlayer DataSource.Factory configured with SimpleCache
     * and HTTP upstream for seamless local-first streaming.
     */
    fun createCacheDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        val defaultDataSourceFactory = DefaultDataSource.Factory(application, upstreamFactory)

        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
