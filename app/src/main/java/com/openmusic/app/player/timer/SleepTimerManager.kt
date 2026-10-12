package com.openmusic.app.player.timer

import com.openmusic.app.player.controller.MusicPlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class SleepTimerMode {
    data object None : SleepTimerMode()
    data class Minutes(val totalMinutes: Int) : SleepTimerMode()
    data object EndOfTrack : SleepTimerMode()
}

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingMillis: Long = 0L,
    val mode: SleepTimerMode = SleepTimerMode.None
) {
    val remainingFormatted: String
        get() {
            if (!isActive) return ""
            val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

/**
 * Manages the background Sleep Timer with a smooth 30-second volume fade-out.
 * Automatically pauses playback when expired and restores player volume.
 */
class SleepTimerManager(
    private val playerController: MusicPlayerController
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    fun startMinutesTimer(minutes: Int) {
        cancelTimer()
        val durationMs = minutes * 60 * 1000L
        val targetEndTime = System.currentTimeMillis() + durationMs

        _state.value = SleepTimerState(
            isActive = true,
            remainingMillis = durationMs,
            mode = SleepTimerMode.Minutes(minutes)
        )

        timerJob = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = (targetEndTime - now).coerceAtLeast(0L)

                _state.update { it.copy(remainingMillis = remaining) }

                // Smooth 30-second fade out
                if (remaining <= 30_000L) {
                    val fadeFactor = (remaining.toFloat() / 30_000f).coerceIn(0f, 1f)
                    playerController.setVolume(fadeFactor)
                }

                if (remaining <= 0L) {
                    playerController.pause()
                    playerController.setVolume(1.0f)
                    _state.value = SleepTimerState()
                    break
                }

                delay(500)
            }
        }
    }

    fun startEndOfTrackTimer() {
        cancelTimer()
        val currentSong = playerController.playerState.value.currentSong
        if (currentSong == null) return

        val duration = playerController.playerState.value.durationMs
        val position = playerController.playerState.value.currentPositionMs
        val initialRemaining = (duration - position).coerceAtLeast(0L)

        _state.value = SleepTimerState(
            isActive = true,
            remainingMillis = initialRemaining,
            mode = SleepTimerMode.EndOfTrack
        )

        val trackId = currentSong.videoId

        timerJob = scope.launch {
            while (isActive) {
                val state = playerController.playerState.value
                // If song changed or stopped, trigger timer completion
                if (state.currentSong?.videoId != trackId) {
                    playerController.pause()
                    playerController.setVolume(1.0f)
                    _state.value = SleepTimerState()
                    break
                }

                val remaining = (state.durationMs - state.currentPositionMs).coerceAtLeast(0L)
                _state.update { it.copy(remainingMillis = remaining) }

                // Smooth 15-second fade-out before end of track
                if (remaining <= 15_000L) {
                    val fadeFactor = (remaining.toFloat() / 15_000f).coerceIn(0f, 1f)
                    playerController.setVolume(fadeFactor)
                }

                if (remaining <= 1_000L || !state.isPlaying) {
                    playerController.pause()
                    playerController.setVolume(1.0f)
                    _state.value = SleepTimerState()
                    break
                }

                delay(500)
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        playerController.setVolume(1.0f)
        _state.value = SleepTimerState()
    }
}
