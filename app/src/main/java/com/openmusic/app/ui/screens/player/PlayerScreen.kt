package com.openmusic.app.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import com.openmusic.app.player.state.RepeatMode
import com.openmusic.app.ui.components.PlaybackSpeedBadge
import com.openmusic.app.ui.theme.DynamicThemeColors
import com.openmusic.app.ui.theme.DynamicThemeHelper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.TextButton
import coil.compose.AsyncImage
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.Song
import com.openmusic.app.domain.model.SongLyrics
import com.openmusic.app.player.state.PlayerState
import com.openmusic.app.player.timer.SleepTimerState
import com.openmusic.app.ui.components.AudioFormatBadge
import com.openmusic.app.ui.components.QueueListItem
import com.openmusic.app.ui.components.SongListItem

@Composable
fun PlayerScreen(
    playerState: PlayerState,
    isFavorite: Boolean = false,
    isDataSaver: Boolean = false,
    quality: AudioQuality = if (isDataSaver) AudioQuality.LOW else AudioQuality.HIGH,
    sleepTimerState: SleepTimerState = SleepTimerState(),
    lyrics: SongLyrics? = null,
    isLoadingLyrics: Boolean = false,
    downloadProgress: Map<String, Float> = emptyMap(),
    onTogglePlayPause: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onEqualizerClick: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onCodecBadgeClick: () -> Unit = {},
    onSpeedBadgeClick: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit = {},
    onDownloadClick: (Song) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onRemoveQueueItem: (Int) -> Unit = {},
    onClearQueue: () -> Unit = {},
    onAdjustSyncOffset: (Long) -> Unit = {},
    onResetSyncOffset: () -> Unit = {},
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playerState.currentSong ?: return
    var showLyrics by remember { mutableStateOf(false) }
    var showFullQueue by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var dynamicColors by remember(currentSong.videoId) {
        mutableStateOf(DynamicThemeColors())
    }

    LaunchedEffect(currentSong.thumbnailUrl) {
        dynamicColors = DynamicThemeHelper.extractColors(context, currentSong.thumbnailUrl)
    }

    val animatedTopColor by animateColorAsState(
        targetValue = dynamicColors.topGradientColor,
        animationSpec = tween(durationMillis = 600),
        label = "dynamicTopColor"
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = dynamicColors.bottomGradientColor,
        animationSpec = tween(durationMillis = 600),
        label = "dynamicBottomColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedTopColor,
                        animatedBottomColor
                    )
                )
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header / Collapse Button & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse Player",
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = if (showFullQueue) {
                    "Queue (${playerState.queue.size})"
                } else if (showLyrics) {
                    "Synchronized Lyrics"
                } else if (sleepTimerState.isActive) {
                    "Sleep in ${sleepTimerState.remainingFormatted}"
                } else {
                    "Now Playing"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if ((!showLyrics && !showFullQueue && sleepTimerState.isActive) || showFullQueue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSleepTimerClick) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerState.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onEqualizerClick) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Equalizer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = {
                    showLyrics = !showLyrics
                    if (showLyrics) showFullQueue = false
                }) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Toggle Lyrics",
                        tint = if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = {
                    showFullQueue = !showFullQueue
                    if (showFullQueue) showLyrics = false
                }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Toggle Queue",
                        tint = if (showFullQueue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showFullQueue) {
            // Full Queue Management Screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Up Next (${playerState.queue.size} songs)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Now Playing: ${currentSong.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (playerState.queue.size > 1) {
                    TextButton(onClick = onClearQueue) {
                        Text(
                            text = "Clear Queue",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(
                    items = playerState.queue,
                    key = { index, song -> "${song.videoId}_$index" }
                ) { index, queueSong ->
                    QueueListItem(
                        song = queueSong,
                        index = index,
                        queueSize = playerState.queue.size,
                        isPlaying = queueSong.videoId == currentSong.videoId,
                        onClick = { onSongClick(queueSong) },
                        onRemove = { onRemoveQueueItem(index) },
                        onMoveUp = { onMoveQueueItem(index, index - 1) },
                        onMoveDown = { onMoveQueueItem(index, index + 1) }
                    )
                }
            }

            // Bottom Mini Controls for the Queue view
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPrevious) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Track")
                        }
                        FilledIconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = onNext) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next Track")
                        }
                    }

                    TextButton(onClick = { showFullQueue = false }) {
                        Text("Done", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            // Toggle between Large Album Artwork & Synchronized Lyrics View
            if (showLyrics) {
            LyricsView(
                lyrics = lyrics,
                isLoading = isLoadingLyrics,
                currentPositionMs = playerState.currentPositionMs,
                onSeekTo = onSeekTo,
                onAdjustSyncOffset = onAdjustSyncOffset,
                onResetSyncOffset = onResetSyncOffset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
        } else {
            if (currentSong.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = currentSong.thumbnailUrl,
                    contentDescription = currentSong.title,
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title, Artist and Download Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onArtistClick(currentSong.artist) }
                )
            }

            // Favorite Button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Unlike" else "Like",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            val currentSongProgress = downloadProgress[currentSong.videoId]
            IconButton(onClick = { onDownloadClick(currentSong) }) {
                if (currentSongProgress != null) {
                    if (currentSongProgress < 0f) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { currentSongProgress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (currentSong.isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Offline",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Audio Codec & Playback Speed Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AudioFormatBadge(
                streamInfo = playerState.streamInfo,
                isOfflineCache = playerState.isOfflineCache || currentSong.isDownloaded,
                quality = quality,
                isDataSaver = isDataSaver,
                onClick = onCodecBadgeClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            PlaybackSpeedBadge(
                speed = playerState.playbackSpeed,
                onClick = onSpeedBadgeClick
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Slider
        val maxDuration = playerState.durationMs.coerceAtLeast(1L).toFloat()
        val currentPosition = playerState.currentPositionMs.toFloat().coerceIn(0f, maxDuration)

        Slider(
            value = currentPosition,
            onValueChange = { onSeekTo(it.toLong()) },
            valueRange = 0f..maxDuration,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(playerState.currentPositionMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(playerState.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Controls Row (Shuffle, Previous, Play/Pause, Next, Repeat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    modifier = Modifier.size(38.dp)
                )
            }

            FilledIconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    modifier = Modifier.size(38.dp)
                )
            }

            IconButton(onClick = onToggleRepeat) {
                Icon(
                    imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat: ${playerState.repeatMode.name}",
                    tint = if (playerState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Up Next Queue Header & Controls
        if (playerState.queue.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showFullQueue = true }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Up Next (${playerState.queue.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand Queue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (playerState.queue.size > 1) {
                    TextButton(onClick = onClearQueue) {
                        Text(
                            text = "Clear Queue",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(
                    items = playerState.queue,
                    key = { index, song -> "${song.videoId}_$index" }
                ) { index, queueSong ->
                    QueueListItem(
                        song = queueSong,
                        index = index,
                        queueSize = playerState.queue.size,
                        isPlaying = queueSong.videoId == currentSong.videoId,
                        onClick = { onSongClick(queueSong) },
                        onRemove = { onRemoveQueueItem(index) },
                        onMoveUp = { onMoveQueueItem(index, index - 1) },
                        onMoveDown = { onMoveQueueItem(index, index + 1) }
                    )
                }
            }
        }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
