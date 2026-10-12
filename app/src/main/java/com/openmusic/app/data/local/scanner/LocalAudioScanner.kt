package com.openmusic.app.data.local.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.openmusic.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-performance on-device audio scanner leveraging Android's MediaStore ContentProvider.
 * Queries local storage off the main thread with optimized projection and query filters
 * to prevent CPU spikes and audio glitches on Snapdragon 675 Kryo cores.
 */
class LocalAudioScanner(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "LocalAudioScanner"
        private val ALBUM_ART_BASE_URI = Uri.parse("content://media/external/audio/albumart")
    }

    /**
     * Scans the device for music tracks with duration >= 10 seconds.
     * Excludes ringtones, alarms, and notifications.
     */
    suspend fun scanLocalAudio(): List<Song> = withContext(ioDispatcher) {
        val songs = mutableListOf<Song>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Filter out ringtones/notifications and short sound effects (< 10 seconds)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Title"
                    val artistRaw = cursor.getString(artistColumn)
                    val artist = if (artistRaw.isNullOrBlank() || artistRaw.equals("<unknown>", ignoreCase = true)) {
                        "Unknown Artist"
                    } else {
                        artistRaw
                    }
                    val album = cursor.getString(albumColumn) ?: ""
                    val albumId = cursor.getLong(albumIdColumn)
                    val durationMs = cursor.getLong(durationColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val dateAddedSeconds = cursor.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, albumId)

                    songs.add(
                        Song(
                            videoId = "local_$id",
                            title = title,
                            artist = artist,
                            thumbnailUrl = albumArtUri.toString(),
                            durationMs = durationMs,
                            streamUrl = contentUri.toString(),
                            cacheKey = "local_$id",
                            isDownloaded = true,
                            isFavorite = false,
                            addedTimestamp = if (dateAddedSeconds > 0) dateAddedSeconds * 1000 else System.currentTimeMillis(),
                            album = album,
                            mimeType = mimeType
                        )
                    )
                }
            }
            Log.d(TAG, "Scanned ${songs.size} local audio files successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore for local audio tracks", e)
        }

        songs
    }

    /**
     * Resolves human-readable format tag from audio MIME type.
     */
    fun getFormatTag(mimeType: String?): String {
        return when {
            mimeType == null -> "AUDIO"
            mimeType.contains("mpeg") || mimeType.contains("mp3") -> "MP3"
            mimeType.contains("flac") -> "FLAC"
            mimeType.contains("mp4") || mimeType.contains("m4a") || mimeType.contains("aac") -> "AAC"
            mimeType.contains("ogg") || mimeType.contains("opus") -> "OGG"
            mimeType.contains("wav") -> "WAV"
            else -> "AUDIO"
        }
    }
}
