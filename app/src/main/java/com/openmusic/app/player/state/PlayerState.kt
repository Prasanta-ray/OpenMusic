package com.openmusic.app.player.state

import com.openmusic.app.domain.model.AudioStreamInfo
import com.openmusic.app.domain.model.Song

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isBuffering: Boolean = false,
    val queue: List<Song> = emptyList(),
    val streamInfo: AudioStreamInfo? = null,
    val isOfflineCache: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f
)

sealed interface PlayerIntent {
    data class PlaySong(val song: Song) : PlayerIntent
    data object PlayPause : PlayerIntent
    data object Next : PlayerIntent
    data object Previous : PlayerIntent
    data class SeekTo(val positionMs: Long) : PlayerIntent
}

sealed interface PlayerEffect {
    data class Error(val message: String) : PlayerEffect
}
