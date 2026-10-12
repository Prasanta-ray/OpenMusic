package com.openmusic.app.domain.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class SongLyrics(
    val videoId: String,
    val isSynced: Boolean,
    val lines: List<LyricLine>,
    val plainLyrics: String? = null,
    val syncOffsetMs: Long = 0L
)
