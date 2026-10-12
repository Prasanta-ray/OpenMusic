package com.openmusic.app.player.audiofx

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Metadata for a single hardware frequency band.
 */
data class BandInfo(
    val band: Short,
    val centerFreqHz: Int,
    val levelMb: Short,
    val minLevelMb: Short = -1000,
    val maxLevelMb: Short = 1000
) {
    val freqDisplay: String
        get() = if (centerFreqHz >= 1000) {
            "${centerFreqHz / 1000} kHz"
        } else {
            "$centerFreqHz Hz"
        }

    val levelDb: Int
        get() = levelMb / 100
}

/**
 * Immutable state representation for the Equalizer, Bass Boost, and Loudness Normalizer.
 */
data class AudioFxState(
    val isEnabled: Boolean = true,
    val bassBoostStrength: Int = 0, // 0..1000
    val bands: List<BandInfo> = emptyList(),
    val selectedPreset: String = "Flat",
    val isHardwareSupported: Boolean = true,
    val isLoudnessNormalizerEnabled: Boolean = true,
    val targetGainMb: Int = 300 // 0..1000 mB (0 to +10 dB)
)

/**
 * Controller for hardware-accelerated Audio Equalizer, Bass Boost, and Loudness Normalizer.
 * Offloaded to the Snapdragon 675 Hexagon DSP via android.media.audiofx.
 */
object AudioFxManager {

    private const val TAG = "AudioFxManager"

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentSessionId: Int = 0

    private val _state = MutableStateFlow(AudioFxState())
    val state: StateFlow<AudioFxState> = _state.asStateFlow()

    val availablePresets = listOf("Flat", "Bass Boost", "Rock", "Pop", "Vocal", "Treble Boost")

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        currentSessionId = audioSessionId

        releaseEffects()

        try {
            // Instantiate hardware Equalizer
            val eq = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            equalizer = eq

            // Instantiate hardware BassBoost
            val bb = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
                if (strengthSupported) {
                    setStrength(_state.value.bassBoostStrength.toShort())
                }
            }
            bassBoost = bb

            // Instantiate hardware LoudnessEnhancer for volume normalization
            try {
                val le = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _state.value.isEnabled && _state.value.isLoudnessNormalizerEnabled
                    setTargetGain(_state.value.targetGainMb)
                }
                loudnessEnhancer = le
            } catch (e: Exception) {
                Log.w(TAG, "LoudnessEnhancer initialization notice: ${e.message}")
            }

            val numBands = eq.numberOfBands
            val range = eq.bandLevelRange // [minMb, maxMb]
            val minMb = range?.getOrNull(0) ?: (-1000).toShort()
            val maxMb = range?.getOrNull(1) ?: 1000.toShort()

            val bandList = mutableListOf<BandInfo>()
            for (i in 0 until numBands) {
                val band = i.toShort()
                val freq = eq.getCenterFreq(band) / 1000 // mHz to Hz
                val currentLevel = eq.getBandLevel(band)
                bandList.add(
                    BandInfo(
                        band = band,
                        centerFreqHz = freq,
                        levelMb = currentLevel,
                        minLevelMb = minMb,
                        maxLevelMb = maxMb
                    )
                )
            }

            _state.update {
                it.copy(
                    bands = bandList,
                    isHardwareSupported = true
                )
            }
            Log.d(TAG, "Attached to audio session $audioSessionId with $numBands bands")
        } catch (e: Exception) {
            Log.w(TAG, "Hardware audiofx not fully supported in current environment: ${e.message}")
            // Fallback for emulator or unprivileged session
            createFallbackBands()
        }
    }

    private fun createFallbackBands() {
        val fallback = listOf(
            BandInfo(0, 60, 0, -1000, 1000),
            BandInfo(1, 230, 0, -1000, 1000),
            BandInfo(2, 910, 0, -1000, 1000),
            BandInfo(3, 3600, 0, -1000, 1000),
            BandInfo(4, 14000, 0, -1000, 1000)
        )
        _state.update {
            it.copy(
                bands = fallback,
                isHardwareSupported = false
            )
        }
    }

    fun setEnabled(enabled: Boolean) {
        _state.update { it.copy(isEnabled = enabled) }
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            loudnessEnhancer?.enabled = enabled && _state.value.isLoudnessNormalizerEnabled
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle enabled state: ${e.message}")
        }
    }

    fun setLoudnessNormalizerEnabled(enabled: Boolean) {
        _state.update { it.copy(isLoudnessNormalizerEnabled = enabled) }
        try {
            loudnessEnhancer?.enabled = _state.value.isEnabled && enabled
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle loudness normalizer: ${e.message}")
        }
    }

    fun setTargetGainMb(gainMb: Int) {
        val clamped = gainMb.coerceIn(0, 1000)
        _state.update { it.copy(targetGainMb = clamped) }
        try {
            loudnessEnhancer?.setTargetGain(clamped)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set target gain: ${e.message}")
        }
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _state.update { it.copy(bassBoostStrength = clamped) }
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(clamped.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set bass boost: ${e.message}")
        }
    }

    fun setBandLevel(band: Short, levelMb: Short) {
        val currentBands = _state.value.bands.map {
            if (it.band == band) it.copy(levelMb = levelMb) else it
        }
        _state.update {
            it.copy(
                bands = currentBands,
                selectedPreset = "Custom"
            )
        }
        try {
            equalizer?.setBandLevel(band, levelMb)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set band level: ${e.message}")
        }
    }

    fun applyPreset(presetName: String) {
        val bands = _state.value.bands
        if (bands.isEmpty()) return

        // 5 standard relative offsets (in millibels, 100mb = 1dB)
        val offsets: List<Short> = when (presetName) {
            "Bass Boost" -> listOf(600, 350, 0, -100, -200)
            "Rock" -> listOf(450, 250, -100, 300, 450)
            "Pop" -> listOf(-100, 200, 400, 250, -100)
            "Vocal" -> listOf(-200, 100, 500, 400, 100)
            "Treble Boost" -> listOf(-300, -100, 0, 400, 700)
            else -> listOf(0, 0, 0, 0, 0) // Flat
        }

        val updated = bands.mapIndexed { index, bandInfo ->
            val targetLevel = offsets.getOrElse(index) { 0.toShort() }
            try {
                equalizer?.setBandLevel(bandInfo.band, targetLevel)
            } catch (e: Exception) {
                // Ignore
            }
            bandInfo.copy(levelMb = targetLevel)
        }

        val targetBassBoost = if (presetName == "Bass Boost") 700 else 0
        setBassBoost(targetBassBoost)

        _state.update {
            it.copy(
                bands = updated,
                selectedPreset = presetName,
                bassBoostStrength = targetBassBoost
            )
        }
    }

    private fun releaseEffects() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        equalizer = null

        try {
            bassBoost?.release()
        } catch (e: Exception) {
            // Ignore
        }
        bassBoost = null

        try {
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        loudnessEnhancer = null
    }

    fun release() {
        releaseEffects()
        currentSessionId = 0
    }
}
