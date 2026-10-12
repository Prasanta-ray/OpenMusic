package com.openmusic.app.domain.model

/**
 * Domain model representing an audio track.
 */
data class Song(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationMs: Long = 0L,
    val streamUrl: String? = null,
    val cacheKey: String? = null,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0L,
    val album: String = "",
    val mimeType: String? = null
) {
    /**
     * Backward-compatible alias for [videoId].
     */
    val id: String
        get() = videoId

    val isLocalTrack: Boolean
        get() = videoId.startsWith("local_") || (streamUrl != null && (streamUrl.startsWith("content://") || streamUrl.startsWith("file://")))
}
