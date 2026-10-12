package com.openmusic.app.data.repository

import com.openmusic.app.data.local.cache.MediaCacheManager
import com.openmusic.app.data.local.dao.SongDao
import com.openmusic.app.data.local.entities.SongEntity
import com.openmusic.app.data.remote.innertube.InnerTubeApiClient
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.AudioStreamInfo
import com.openmusic.app.domain.model.SearchFilter
import com.openmusic.app.domain.model.Song
import com.openmusic.app.domain.repository.MusicRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

import com.openmusic.app.data.local.dao.PlaylistDao
import com.openmusic.app.data.local.entities.PlaylistEntity
import com.openmusic.app.data.local.entities.PlaylistSongCrossRef
import com.openmusic.app.data.local.entities.PlaylistWithSongs

/**
 * Implementation of [MusicRepository] connecting Room local storage,
 * ExoPlayer MediaCacheManager, and YouTube InnerTube remote client.
 * Guarantees all database, remote, and cache operations run on [ioDispatcher].
 */
class MusicRepositoryImpl(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val lyricsDao: com.openmusic.app.data.local.dao.LyricsDao,
    private val searchHistoryDao: com.openmusic.app.data.local.dao.SearchHistoryDao,
    private val cacheManager: MediaCacheManager,
    private val apiClient: InnerTubeApiClient,
    private val lyricsApiClient: com.openmusic.app.data.remote.lyrics.LyricsApiClient = com.openmusic.app.data.remote.lyrics.LyricsApiClient(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MusicRepository {

    override fun getAllDownloadedSongs(): Flow<List<Song>> {
        return songDao.getAllDownloadedSongs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRecentlyPlayedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getRecentlyPlayed(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSong(videoId: String): Flow<Song?> {
        return songDao.getSongById(videoId).map { it?.toDomain() }
    }

    override suspend fun search(
        query: String,
        filter: SearchFilter
    ): Result<List<Song>> = withContext(ioDispatcher) {
        apiClient.search(query, filter)
    }

    override suspend fun getOptimalAudioStream(
        videoId: String,
        isDataSaver: Boolean
    ): Result<AudioStreamInfo> = withContext(ioDispatcher) {
        apiClient.getOptimalAudioStream(videoId, if (isDataSaver) AudioQuality.LOW else AudioQuality.HIGH)
    }

    override suspend fun getOptimalAudioStream(
        videoId: String,
        quality: AudioQuality
    ): Result<AudioStreamInfo> = withContext(ioDispatcher) {
        apiClient.getOptimalAudioStream(videoId, quality)
    }

    override suspend fun getRelatedTracks(videoId: String): Result<List<Song>> = withContext(ioDispatcher) {
        apiClient.getNextQueue(videoId)
    }

    override suspend fun saveSong(song: Song) = withContext(ioDispatcher) {
        songDao.insertSong(song.toEntity())
    }

    override suspend fun toggleFavorite(song: Song) = withContext(ioDispatcher) {
        val existing = songDao.getSong(song.videoId)
        if (existing != null) {
            songDao.updateFavoriteStatus(song.videoId, !existing.isFavorite)
        } else {
            songDao.insertSong(song.toEntity().copy(isFavorite = true))
        }
    }

    override suspend fun updateDownloadStatus(
        videoId: String,
        isDownloaded: Boolean,
        cacheKey: String?
    ) = withContext(ioDispatcher) {
        cacheManager.syncDownloadStatus(videoId, cacheKey, isDownloaded)
    }

    override suspend fun updateLastPlayed(
        videoId: String,
        timestamp: Long
    ) = withContext(ioDispatcher) {
        songDao.updatePlaybackTimestamp(videoId, timestamp)
    }

    override suspend fun deleteSong(videoId: String) = withContext(ioDispatcher) {
        val existing = songDao.getSong(videoId)
        if (existing?.cacheKey != null) {
            cacheManager.removeCache(existing.cacheKey)
        }
        songDao.deleteSong(videoId)
    }

    override suspend fun isSongCached(
        videoId: String,
        cacheKey: String?,
        durationMs: Long
    ): Boolean = withContext(ioDispatcher) {
        val key = cacheKey ?: videoId
        cacheManager.isFullyCached(key, durationMs)
    }

    override val downloadProgress: Flow<Map<String, Float>>
        get() = cacheManager.downloadProgress

    override suspend fun downloadTrack(song: Song): Result<Unit> = downloadTrack(song, AudioQuality.HIGH)

    override suspend fun downloadTrack(song: Song, quality: AudioQuality): Result<Unit> = withContext(ioDispatcher) {
        val streamUrl = song.streamUrl ?: run {
            val streamResult = apiClient.getOptimalAudioStream(song.videoId, quality)
            if (streamResult.isFailure) {
                return@withContext Result.failure(streamResult.exceptionOrNull() ?: Exception("Failed to resolve stream for download"))
            }
            streamResult.getOrNull()?.streamUrl ?: return@withContext Result.failure(Exception("Stream URL is empty"))
        }
        cacheManager.downloadTrack(song, streamUrl)
    }

    override suspend fun cancelDownload(videoId: String) = withContext(ioDispatcher) {
        cacheManager.cancelDownload(videoId)
    }

    override suspend fun removeDownload(videoId: String, cacheKey: String?) = withContext(ioDispatcher) {
        cacheManager.removeTrackCacheAndSync(videoId, cacheKey)
    }

    override suspend fun getStreamUrl(videoId: String): Result<String> = withContext(ioDispatcher) {
        getOptimalAudioStream(videoId).map { it.streamUrl }
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylists()
    }

    override fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?> {
        return playlistDao.getPlaylistWithSongs(playlistId)
    }

    override suspend fun createPlaylist(title: String): Long = withContext(ioDispatcher) {
        playlistDao.insertPlaylist(PlaylistEntity(title = title))
    }

    override suspend fun addSongToPlaylist(playlistId: Long, song: Song) = withContext(ioDispatcher) {
        songDao.insertSong(song.toEntity())
        playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId = playlistId, videoId = song.videoId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String) = withContext(ioDispatcher) {
        playlistDao.removeSongFromPlaylist(playlistId, videoId)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(ioDispatcher) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun getLyrics(song: Song): Result<com.openmusic.app.domain.model.SongLyrics> = withContext(ioDispatcher) {
        runCatching {
            // 1. Check local DB cache first to eliminate unnecessary network hits
            val cached = lyricsDao.getLyrics(song.videoId)
            if (cached != null) {
                return@runCatching com.openmusic.app.data.remote.lyrics.LrcParser.parse(
                    videoId = song.videoId,
                    syncedLyrics = cached.syncedLyrics,
                    plainLyrics = cached.plainLyrics,
                    syncOffsetMs = cached.syncOffsetMs
                )
            }

            // 2. Fetch from LRCLIB
            val durationSec = (song.durationMs / 1000).toInt()
            val response = lyricsApiClient.getLyrics(song.title, song.artist, durationSec)

            if (response != null && (!response.syncedLyrics.isNullOrBlank() || !response.plainLyrics.isNullOrBlank())) {
                // Save to local cache
                lyricsDao.insertLyrics(
                    com.openmusic.app.data.local.entities.LyricsEntity(
                        videoId = song.videoId,
                        plainLyrics = response.plainLyrics,
                        syncedLyrics = response.syncedLyrics,
                        syncOffsetMs = 0L
                    )
                )
                com.openmusic.app.data.remote.lyrics.LrcParser.parse(
                    videoId = song.videoId,
                    syncedLyrics = response.syncedLyrics,
                    plainLyrics = response.plainLyrics,
                    syncOffsetMs = 0L
                )
            } else {
                com.openmusic.app.domain.model.SongLyrics(
                    videoId = song.videoId,
                    isSynced = false,
                    lines = emptyList(),
                    plainLyrics = null,
                    syncOffsetMs = 0L
                )
            }
        }
    }

    override suspend fun updateLyricsSyncOffset(videoId: String, offsetMs: Long) = withContext(ioDispatcher) {
        lyricsDao.updateSyncOffset(videoId, offsetMs)
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return searchHistoryDao.getRecentQueries()
    }

    override suspend fun addSearchQuery(query: String) = withContext(ioDispatcher) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertQuery(com.openmusic.app.data.local.entities.SearchHistoryEntity(query.trim()))
        }
    }

    override suspend fun deleteSearchQuery(query: String) = withContext(ioDispatcher) {
        searchHistoryDao.deleteQuery(query)
    }

    override suspend fun clearSearchHistory() = withContext(ioDispatcher) {
        searchHistoryDao.clearHistory()
    }

    override suspend fun getSearchSuggestions(query: String): Result<List<String>> = withContext(ioDispatcher) {
        apiClient.getSearchSuggestions(query)
    }

    override suspend fun getArtistDetail(artistName: String): Result<com.openmusic.app.domain.model.ArtistDetail> = withContext(ioDispatcher) {
        runCatching {
            val songsResult = apiClient.search(artistName, SearchFilter.SONGS).getOrDefault(emptyList())
            val albumsResult = apiClient.search(artistName, SearchFilter.ALBUMS).getOrDefault(emptyList())
            val bannerUrl = songsResult.firstOrNull()?.thumbnailUrl
            val albums = albumsResult.map { song ->
                com.openmusic.app.domain.model.Album(
                    browseId = song.videoId,
                    title = song.title,
                    artist = song.artist,
                    thumbnailUrl = song.thumbnailUrl
                )
            }
            com.openmusic.app.domain.model.ArtistDetail(
                name = artistName,
                bannerUrl = bannerUrl,
                topSongs = songsResult,
                albums = albums
            )
        }
    }

    override suspend fun getAlbumDetail(albumName: String, artist: String): Result<com.openmusic.app.domain.model.Album> = withContext(ioDispatcher) {
        runCatching {
            val query = if (artist.isNotBlank()) "$albumName $artist" else albumName
            val songs = apiClient.search(query, SearchFilter.SONGS).getOrDefault(emptyList())
            val thumb = songs.firstOrNull()?.thumbnailUrl ?: ""
            com.openmusic.app.domain.model.Album(
                browseId = "",
                title = albumName,
                artist = artist,
                thumbnailUrl = thumb,
                tracks = songs
            )
        }
    }

    private fun SongEntity.toDomain() = Song(
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        durationMs = durationMs,
        streamUrl = streamUrl,
        cacheKey = cacheKey,
        isDownloaded = isDownloaded,
        isFavorite = isFavorite,
        addedTimestamp = addedTimestamp,
        lastPlayedTimestamp = lastPlayedTimestamp
    )

    override fun getTotalCacheBytes(): Long {
        return cacheManager.getTotalCacheSpace()
    }

    override fun clearDiskCache(): Long {
        return cacheManager.clearAllCache()
    }

    private fun Song.toEntity() = SongEntity(
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        durationMs = durationMs,
        streamUrl = streamUrl,
        cacheKey = cacheKey,
        isDownloaded = isDownloaded,
        isFavorite = isFavorite,
        addedTimestamp = addedTimestamp,
        lastPlayedTimestamp = lastPlayedTimestamp
    )
}
