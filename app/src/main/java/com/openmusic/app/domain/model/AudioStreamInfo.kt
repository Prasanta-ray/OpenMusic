package com.openmusic.app.domain.model

/**
 * Encapsulates resolved audio stream parameters optimized for Snapdragon 675 playback.
 * Targets itag 251 (Opus ~160kbps) or itag 140 (AAC ~128kbps) to minimize memory
 * bandwidth and battery drain.
 */
data class AudioStreamInfo(
    val videoId: String,
    val streamUrl: String,
    val mimeType: String,
    val bitrate: Long,
    val itag: Int,
    val contentLength: Long? = null,
    val expiresInSeconds: Long? = null
) {
    val codecName: String
        get() = when {
            mimeType.contains("opus", ignoreCase = true) -> "OPUS"
            mimeType.contains("mp4a", ignoreCase = true) || mimeType.contains("aac", ignoreCase = true) -> "AAC"
            else -> mimeType.substringAfter("audio/").substringBefore(";").uppercase()
        }

    val bitrateKbps: Int
        get() = (bitrate / 1000).toInt()

    val containerName: String
        get() = when {
            mimeType.contains("webm", ignoreCase = true) -> "WebM"
            mimeType.contains("mp4", ignoreCase = true) -> "M4A"
            else -> "Audio"
        }

    val isDataSaverQuality: Boolean
        get() = itag in listOf(250, 249, 139) || bitrateKbps < 100

    val badgeText: String
        get() = "$codecName • $bitrateKbps kbps"
}
