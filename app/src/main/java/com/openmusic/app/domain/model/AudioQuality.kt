package com.openmusic.app.domain.model

/**
 * Granular audio stream and download quality tiers tailored for Snapdragon 675 Kryo DSP
 * and cellular efficiency.
 */
enum class AudioQuality(
    val id: String,
    val displayName: String,
    val details: String,
    val bitrateKbps: Int,
    val codec: String,
    val itag: Int
) {
    HIGH(
        id = "HIGH",
        displayName = "High Fidelity",
        details = "160 kbps Opus (itag 251) • Highest studio fidelity",
        bitrateKbps = 160,
        codec = "OPUS",
        itag = 251
    ),
    STANDARD(
        id = "STANDARD",
        displayName = "Standard",
        details = "128 kbps AAC (itag 140) • Crisp & universally compatible",
        bitrateKbps = 128,
        codec = "AAC",
        itag = 140
    ),
    LOW(
        id = "LOW",
        displayName = "Data Saver",
        details = "50–70 kbps Opus (itag 249/250) • Conserves ~60% cellular data",
        bitrateKbps = 50,
        codec = "OPUS",
        itag = 250
    );

    companion object {
        fun fromString(value: String?): AudioQuality =
            entries.firstOrNull { it.id.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: HIGH
    }
}
