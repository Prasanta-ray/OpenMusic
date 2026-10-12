package com.openmusic.app.data.remote.lyrics

import com.openmusic.app.domain.model.LyricLine
import com.openmusic.app.domain.model.SongLyrics

object LrcParser {

    private val timeTagRegex = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?\]""")

    fun parse(videoId: String, syncedLyrics: String?, plainLyrics: String?, syncOffsetMs: Long = 0L): SongLyrics {
        if (syncedLyrics.isNullOrBlank()) {
            return SongLyrics(
                videoId = videoId,
                isSynced = false,
                lines = emptyList(),
                plainLyrics = plainLyrics,
                syncOffsetMs = syncOffsetMs
            )
        }

        val lines = mutableListOf<LyricLine>()

        syncedLyrics.lineSequence().forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isBlank() || trimmed.startsWith("[ar:") || trimmed.startsWith("[ti:") ||
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") || trimmed.startsWith("[length:")
            ) {
                return@forEach
            }

            val matches = timeTagRegex.findAll(trimmed).toList()
            if (matches.isNotEmpty()) {
                val text = timeTagRegex.replace(trimmed, "").trim()
                if (text.isNotBlank()) {
                    for (match in matches) {
                        val min = match.groupValues[1].toLongOrNull() ?: 0L
                        val sec = match.groupValues[2].toLongOrNull() ?: 0L
                        val millisStr = match.groupValues[3]
                        val ms = when (millisStr.length) {
                            1 -> (millisStr.toLongOrNull() ?: 0L) * 100
                            2 -> (millisStr.toLongOrNull() ?: 0L) * 10
                            3 -> millisStr.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        val timestampMs = min * 60_000L + sec * 1_000L + ms
                        lines.add(LyricLine(timestampMs, text))
                    }
                }
            }
        }

        lines.sortBy { it.timestampMs }

        return SongLyrics(
            videoId = videoId,
            isSynced = lines.isNotEmpty(),
            lines = lines,
            plainLyrics = plainLyrics,
            syncOffsetMs = syncOffsetMs
        )
    }
}
