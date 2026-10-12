package com.openmusic.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.AudioStreamInfo

@Composable
fun AudioFormatBadge(
    streamInfo: AudioStreamInfo?,
    isOfflineCache: Boolean,
    quality: AudioQuality = AudioQuality.HIGH,
    isDataSaver: Boolean = (quality == AudioQuality.LOW),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, icon, text) = when {
        isOfflineCache -> Quad(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.CloudDone,
            "OFFLINE CACHE"
        )
        streamInfo != null -> {
            val label = when {
                streamInfo.isDataSaverQuality || quality == AudioQuality.LOW ->
                    "${streamInfo.codecName} • ${streamInfo.bitrateKbps} kbps (SAVER)"
                streamInfo.itag == 140 || quality == AudioQuality.STANDARD ->
                    "${streamInfo.codecName} • ${streamInfo.bitrateKbps} kbps (STD)"
                else ->
                    "${streamInfo.codecName} • ${streamInfo.bitrateKbps} kbps (HIGH)"
            }
            Quad(
                when (quality) {
                    AudioQuality.LOW -> MaterialTheme.colorScheme.secondaryContainer
                    AudioQuality.STANDARD -> MaterialTheme.colorScheme.tertiaryContainer
                    AudioQuality.HIGH -> MaterialTheme.colorScheme.primaryContainer
                },
                when (quality) {
                    AudioQuality.LOW -> MaterialTheme.colorScheme.onSecondaryContainer
                    AudioQuality.STANDARD -> MaterialTheme.colorScheme.onTertiaryContainer
                    AudioQuality.HIGH -> MaterialTheme.colorScheme.onPrimaryContainer
                },
                Icons.Default.GraphicEq,
                label
            )
        }
        else -> {
            val label = when (quality) {
                AudioQuality.LOW -> "OPUS • 50 kbps (SAVER)"
                AudioQuality.STANDARD -> "AAC • 128 kbps (STD)"
                AudioQuality.HIGH -> "OPUS • 160 kbps (HIGH)"
            }
            Quad(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Default.GraphicEq,
                label
            )
        }
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Audio Format",
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = contentColor
            )

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
