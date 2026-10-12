package com.openmusic.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.openmusic.app.R
import com.openmusic.app.data.local.entities.PlaylistEntity
import com.openmusic.app.domain.model.Song
import java.util.Calendar
import java.util.Locale

// Helper for Shimmer Effect
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.5f),
                Color.LightGray.copy(alpha = 0.2f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

@Composable
fun AnimatedEqualizerIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(360, delayMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, delayMillis = 50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val heights = listOf(anim1, anim2, anim3)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.take(barCount).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return ""
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

private val GlassBackground = Color(0x18FFFFFF)
private val GlassBackgroundCard = Color(0x14FFFFFF)
private val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.18f),
        Color.White.copy(alpha = 0.04f)
    ),
    start = Offset(0f, 0f),
    end = Offset(400f, 400f)
)
private val GlassBorderHairline = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.02f)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    trendingSongs: List<Song>,
    recentlyPlayed: List<Song>,
    favoriteSongs: List<Song> = emptyList(),
    downloadedSongs: List<Song> = emptyList(),
    playlists: List<PlaylistEntity> = emptyList(),
    currentPlayingId: String?,
    downloadProgress: Map<String, Float> = emptyMap(),
    currentVibe: String = "All",
    isAudioFxActive: Boolean = false,
    sleepTimerRemainingSeconds: Long = 0L,
    isSleepTimerActive: Boolean = false,
    onVibeSelect: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onEqualizerClick: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onPlayAllTrending: () -> Unit = {},
    onShuffleTrending: () -> Unit = {},
    onPlaySongList: (List<Song>, Int, Boolean) -> Unit = { _, _, _ -> },
    onSongClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit = {},
    onToggleFavorite: (Song) -> Unit = {},
    onPlayNextInQueue: (Song) -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(hour) {
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }
    val subGreeting = remember(hour) {
        when (hour) {
            in 4..11 -> "Pure Sound • High Fidelity Audio"
            in 12..16 -> "High Energy • Bit-Perfect Decoding"
            in 17..21 -> "Acoustic Warmth • Studio Precision"
            else -> "Late Night Sessions • Hexagon DSP"
        }
    }

    val vibes = listOf("All", "Chill", "Workout", "Focus", "Party", "Hindi Pop", "Bollywood", "Lo-Fi", "Rock", "Acoustic", "Romance")

    var isCompactListView by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    Box(modifier = modifier.fillMaxSize()) {
        // Ambient Top Gradient with Subtle Radial Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryGlow, surfaceColor)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Header Section: Brand Crest, Contextual Greeting & Glassmorphic Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 36.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo Crest Avatar with Classic Hairline Border
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCF2DC))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "OpenMusic",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = subGreeting,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Glassmorphic Quick Action Buttons: Equalizer DSP, Sleep Timer, Refresh, Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Equalizer DSP Glass Button
                        Surface(
                            shape = CircleShape,
                            color = if (isAudioFxActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, if (isAudioFxActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { onEqualizerClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isAudioFxActive) {
                                    AnimatedEqualizerIndicator(
                                        modifier = Modifier.size(16.dp, 14.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Equalizer",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Sleep Timer Glass Button / Pill
                        Surface(
                            shape = if (isSleepTimerActive) RoundedCornerShape(19.dp) else CircleShape,
                            color = if (isSleepTimerActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, if (isSleepTimerActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .height(38.dp)
                                .clickable { onSleepTimerClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = if (isSleepTimerActive) 10.dp else 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Sleep Timer",
                                    tint = if (isSleepTimerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                                if (isSleepTimerActive) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val mins = (sleepTimerRemainingSeconds / 60).coerceAtLeast(1)
                                    Text(
                                        text = "${mins}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Refresh Glass Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { onRefresh() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Settings Glass Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { onSettingsClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hardware Audio Engine Glassmorphic Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, GlassBorderBrush)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "32-BIT HEXAGON AUDIO DSP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isAudioFxActive) "Hardware 5-Band Studio EQ • Active" else "Bit-Perfect Processing • Tap to tune",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onEqualizerClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tune EQ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Spotify-Style 6-Item Quick Access Grid (2 columns x 3 rows) with Frosted Glass Look
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Liked Songs & Offline Downloads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAccessCard(
                            modifier = Modifier.weight(1f),
                            title = "Liked Songs",
                            subtitle = if (favoriteSongs.isNotEmpty()) "${favoriteSongs.size} tracks" else "Your favorites",
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF881337), Color(0xFF4C0519))),
                            icon = Icons.Default.Favorite,
                            onClick = {
                                if (favoriteSongs.isNotEmpty()) {
                                    onPlaySongList(favoriteSongs, 0, false)
                                } else {
                                    onNavigateToLibrary()
                                }
                            },
                            onPlayClick = {
                                if (favoriteSongs.isNotEmpty()) onPlaySongList(favoriteSongs, 0, false)
                            }
                        )

                        QuickAccessCard(
                            modifier = Modifier.weight(1f),
                            title = "Downloads",
                            subtitle = if (downloadedSongs.isNotEmpty()) "${downloadedSongs.size} offline" else "Saved tracks",
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF064E3B), Color(0xFF022C22))),
                            icon = Icons.Default.DownloadDone,
                            onClick = {
                                if (downloadedSongs.isNotEmpty()) {
                                    onPlaySongList(downloadedSongs, 0, false)
                                } else {
                                    onNavigateToLibrary()
                                }
                            },
                            onPlayClick = {
                                if (downloadedSongs.isNotEmpty()) onPlaySongList(downloadedSongs, 0, false)
                            }
                        )
                    }

                    // Row 2: Last Played Song & Top Playlist (or Trending Mix)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val lastSong = recentlyPlayed.firstOrNull()
                        if (lastSong != null) {
                            QuickAccessSongCard(
                                modifier = Modifier.weight(1f),
                                song = lastSong,
                                isPlaying = lastSong.videoId == currentPlayingId,
                                onClick = { onSongClick(lastSong) }
                            )
                        } else {
                            QuickAccessCard(
                                modifier = Modifier.weight(1f),
                                title = "Top Hits",
                                subtitle = "Trending selection",
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))),
                                icon = Icons.Outlined.GraphicEq,
                                onClick = onPlayAllTrending,
                                onPlayClick = onPlayAllTrending
                            )
                        }

                        val topPlaylist = playlists.firstOrNull()
                        if (topPlaylist != null) {
                            QuickAccessCard(
                                modifier = Modifier.weight(1f),
                                title = topPlaylist.title,
                                subtitle = "Playlist",
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF3B0764), Color(0xFF1E1B4B))),
                                icon = Icons.Default.QueueMusic,
                                onClick = onNavigateToLibrary,
                                onPlayClick = onNavigateToLibrary
                            )
                        } else {
                            QuickAccessCard(
                                modifier = Modifier.weight(1f),
                                title = "Discovery Mix",
                                subtitle = "Curated radio",
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF7C2D12), Color(0xFF431407))),
                                icon = Icons.Default.MusicNote,
                                onClick = { onVibeSelect("Chill") },
                                onPlayClick = onPlayAllTrending
                            )
                        }
                    }

                    // Row 3: Recently Played 2 & 3 (if available)
                    if (recentlyPlayed.size > 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val song2 = recentlyPlayed[1]
                            QuickAccessSongCard(
                                modifier = Modifier.weight(1f),
                                song = song2,
                                isPlaying = song2.videoId == currentPlayingId,
                                onClick = { onSongClick(song2) }
                            )

                            val song3 = recentlyPlayed[2]
                            QuickAccessSongCard(
                                modifier = Modifier.weight(1f),
                                song = song3,
                                isPlaying = song3.videoId == currentPlayingId,
                                onClick = { onSongClick(song3) }
                            )
                        }
                    }
                }
            }

            // 4. Vibe / Mood Filter Chips Row (Clean Minimalist Glass Pills - No Emojis)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                ) {
                    items(vibes) { vibe ->
                        val isSelected = vibe.equals(currentVibe, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else Color(0x10FFFFFF),
                            border = BorderStroke(
                                0.75.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.clickable { onVibeSelect(vibe) }
                        ) {
                            Text(
                                text = vibe,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // 5. Featured Charts Carousel (Top Horizontal Cards)
            if (trendingSongs.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FEATURED CHARTS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Explore All →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onPlayAllTrending() }
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(trendingSongs.take(8), key = { _, s -> s.videoId }) { index, song ->
                                FeaturedChartCard(
                                    rank = index + 1,
                                    song = song,
                                    isPlaying = song.videoId == currentPlayingId,
                                    onClick = { onSongClick(song) }
                                )
                            }
                        }
                    }
                }
            }

            // 6. Curated Mood Mixes (Clean Glassmorphic Radios - No Emojis)
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
                    Text(
                        text = "CURATED RADIOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            MoodMixBanner(
                                title = "Late Night Lo-Fi",
                                subtitle = "Calm your mind • Relax & focus",
                                icon = Icons.Outlined.Nightlight,
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF141936), Color(0xFF1E1035))),
                                onClick = { onVibeSelect("Lo-Fi") }
                            )
                        }
                        item {
                            MoodMixBanner(
                                title = "High Voltage Workout",
                                subtitle = "Adrenaline & heavy bass",
                                icon = Icons.Outlined.FitnessCenter,
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF38101C), Color(0xFF1F0C24))),
                                onClick = { onVibeSelect("Workout") }
                            )
                        }
                        item {
                            MoodMixBanner(
                                title = "Bollywood Classics",
                                subtitle = "Chartbusters & timeless melodies",
                                icon = Icons.Outlined.Movie,
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF361C0D), Color(0xFF240E1B))),
                                onClick = { onVibeSelect("Bollywood") }
                            )
                        }
                        item {
                            MoodMixBanner(
                                title = "Acoustic Sunset",
                                subtitle = "Warm guitars & soothing vocals",
                                icon = Icons.Default.MusicNote,
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF0F2617), Color(0xFF0D1F17))),
                                onClick = { onVibeSelect("Chill") }
                            )
                        }
                    }
                }
            }

            // 7. "Continue Listening" Glassmorphic Hero Card
            if (recentlyPlayed.isNotEmpty()) {
                val lastSong = recentlyPlayed.first()
                val isLastSongPlaying = lastSong.videoId == currentPlayingId

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onSongClick(lastSong) },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x14FFFFFF),
                        border = BorderStroke(1.dp, GlassBorderBrush)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = lastSong.thumbnailUrl,
                                    contentDescription = lastSong.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isLastSongPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AnimatedEqualizerIndicator(
                                            modifier = Modifier.size(24.dp, 20.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "CONTINUE LISTENING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = lastSong.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lastSong.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable { onSongClick(lastSong) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isLastSongPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 8. Trending Hits Glassmorphic Sticky Header
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (currentVibe == "All") "TRENDING NOW" else "$currentVibe Hits".uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (trendingSongs.isNotEmpty()) {
                                Text(
                                    text = "${trendingSongs.size} tracks available",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        }

                        if (trendingSongs.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = onPlayAllTrending,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play All", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = onShuffleTrending,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { isCompactListView = !isCompactListView },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCompactListView) Icons.Default.ViewAgenda else Icons.Default.ViewHeadline,
                                        contentDescription = "Toggle View",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 9. Ranked Track List Items
            if (trendingSongs.isEmpty()) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerEffect()
                    )
                }
            } else {
                itemsIndexed(trendingSongs, key = { _, s -> s.videoId }) { index, song ->
                    val isFav = favoriteSongs.any { it.videoId == song.videoId }
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(350)) + slideInVertically(
                            animationSpec = tween(350),
                            initialOffsetY = { it / 3 }
                        )
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = if (isCompactListView) 2.dp else 4.dp)) {
                            RankedSongListItem(
                                rank = index + 1,
                                song = song,
                                isPlaying = song.videoId == currentPlayingId,
                                isFavorite = isFav,
                                isCompact = isCompactListView,
                                downloadProgress = downloadProgress[song.videoId],
                                onClick = { onSongClick(song) },
                                onDownloadClick = { onDownloadClick(song) },
                                onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                                onToggleFavorite = { onToggleFavorite(song) },
                                onPlayNextInQueue = { onPlayNextInQueue(song) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-components: Quick Access Cards (Glassmorphic Grid Style)
// -------------------------------------------------------------

@Composable
fun QuickAccessCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    gradient: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(1.dp, GlassBorderBrush)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessSongCard(
    modifier: Modifier = Modifier,
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else Color(0x14FFFFFF),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
            else GlassBorderBrush
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedEqualizerIndicator(
                            modifier = Modifier.size(20.dp, 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-components: Featured Chart Card (Glassmorphic Card)
// -------------------------------------------------------------

@Composable
fun FeaturedChartCard(
    rank: Int,
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f) else Color(0x14FFFFFF),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
            else GlassBorderBrush
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(134.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Classy Metallic Glass Rank Badge
                val (metallicBorder, metallicText) = when (rank) {
                    1 -> Color(0xFFFFD700) to Color(0xFFFFD700)
                    2 -> Color(0xFFE2E8F0) to Color(0xFFE2E8F0)
                    3 -> Color(0xFFCD7F32) to Color(0xFFCD7F32)
                    else -> Color.White.copy(alpha = 0.3f) to Color.White.copy(alpha = 0.85f)
                }

                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 10.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(0.5.dp, metallicBorder),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "#%02d", rank),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = metallicText,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // Frosted Play / Pause FAB Overlay on Bottom-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.65f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        AnimatedEqualizerIndicator(
                            modifier = Modifier.size(18.dp, 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

// -------------------------------------------------------------
// Sub-components: Mood Mix Banner (Clean Glassmorphic Radios)
// -------------------------------------------------------------

@Composable
fun MoodMixBanner(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassBorderBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .background(Color.White.copy(alpha = 0.05f))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-components: Ranked Song List Item with 3-Dot Options Menu
// -------------------------------------------------------------

@Composable
fun RankedSongListItem(
    rank: Int,
    song: Song,
    isPlaying: Boolean,
    isFavorite: Boolean,
    isCompact: Boolean,
    downloadProgress: Float?,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayNextInQueue: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
        else Color(0x0EFFFFFF),
        border = BorderStroke(
            0.5.dp,
            if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = if (isCompact) 6.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = String.format(Locale.getDefault(), "%02d", rank),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.width(28.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Thumbnail
            Box(
                modifier = Modifier
                    .size(if (isCompact) 44.dp else 52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedEqualizerIndicator(
                            modifier = Modifier.size(20.dp, 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Artist, Duration & HD Badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val durationStr = formatDuration(song.durationMs)
                    if (durationStr.isNotBlank()) {
                        Text(
                            text = " • $durationStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "HD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Quick Actions: Favorite Heart
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Download Status
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.size(36.dp)
            ) {
                if (downloadProgress != null) {
                    if (downloadProgress < 0f) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (song.isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 3-Dot More Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Next in Queue") },
                        leadingIcon = {
                            Icon(Icons.Default.QueueMusic, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onPlayNextInQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylistClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isFavorite) "Remove from Liked" else "Add to Liked") },
                        leadingIcon = {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onToggleFavorite()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (song.isDownloaded) "Remove Download" else "Download Offline") },
                        leadingIcon = {
                            Icon(if (song.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onDownloadClick()
                        }
                    )
                }
            }
        }
    }
}