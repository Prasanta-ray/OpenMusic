package com.openmusic.app.ui.components

import androidx.compose.runtime.Composable
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.AudioStreamInfo

/**
 * Backward-compatible bridge to [AudioQualityDialog].
 */
@Composable
fun DataSaverDialog(
    streamInfo: AudioStreamInfo?,
    isOfflineCache: Boolean,
    isDataSaver: Boolean,
    onToggleDataSaver: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AudioQualityDialog(
        streamInfo = streamInfo,
        isOfflineCache = isOfflineCache,
        currentStreamingQuality = if (isDataSaver) AudioQuality.LOW else AudioQuality.HIGH,
        currentDownloadQuality = AudioQuality.HIGH,
        onSelectStreamingQuality = { onToggleDataSaver(it == AudioQuality.LOW) },
        onSelectDownloadQuality = {},
        onDismiss = onDismiss
    )
}
