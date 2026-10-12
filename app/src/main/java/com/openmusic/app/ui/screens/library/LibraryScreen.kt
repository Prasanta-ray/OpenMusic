package com.openmusic.app.ui.screens.library

import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.openmusic.app.data.local.entities.PlaylistEntity
import com.openmusic.app.data.local.entities.PlaylistWithSongs
import com.openmusic.app.domain.model.Song
import com.openmusic.app.ui.components.SongListItem

enum class LibrarySortOption(val title: String) {
    RECENT("Recently Added"),
    TITLE("Title (A-Z)"),
    ARTIST("Artist (A-Z)"),
    DURATION("Duration")
}

@Composable
fun LibraryScreen(
    downloadedSongs: List<Song>,
    favoriteSongs: List<Song> = emptyList(),
    playlists: List<PlaylistEntity> = emptyList(),
    deviceSongs: List<Song> = emptyList(),
    isScanningDevice: Boolean = false,
    selectedPlaylistWithSongs: PlaylistWithSongs? = null,
    currentPlayingId: String?,
    downloadProgress: Map<String, Float> = emptyMap(),
    onSongClick: (Song) -> Unit,
    onDeleteDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit = {},
    onCreatePlaylist: (String) -> Unit = {},
    onSelectPlaylist: (Long?) -> Unit = {},
    onDeletePlaylist: (Long) -> Unit = {},
    onRemoveSongFromPlaylist: (playlistId: Long, videoId: String) -> Unit = { _, _ -> },
    onScanDeviceTracks: () -> Unit = {},
    onPlaySongList: (List<Song>, Int, Boolean) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Downloads, 1: Favorites, 2: Playlists, 3: Device
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistTitle by remember { mutableStateOf("") }
    var isPlaylistGridView by remember { mutableStateOf(true) }
    var currentSort by remember { mutableStateOf(LibrarySortOption.RECENT) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Helper sorting function
    fun sortSongs(list: List<Song>): List<Song> {
        return when (currentSort) {
            LibrarySortOption.RECENT -> list
            LibrarySortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            LibrarySortOption.ARTIST -> list.sortedBy { it.artist.lowercase() }
            LibrarySortOption.DURATION -> list.sortedByDescending { it.durationMs }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistTitle,
                    onValueChange = { newPlaylistTitle = it },
                    label = { Text("Playlist Title") },
                    placeholder = { Text("e.g. Chill Wave, Workout Vibes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistTitle.isNotBlank()) {
                            onCreatePlaylist(newPlaylistTitle.trim())
                            newPlaylistTitle = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    enabled = newPlaylistTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // If a playlist is selected, show Playlist Detail View
        if (selectedPlaylistWithSongs != null) {
            PlaylistDetailView(
                playlistWithSongs = selectedPlaylistWithSongs,
                currentPlayingId = currentPlayingId,
                downloadProgress = downloadProgress,
                onBackClick = { onSelectPlaylist(null) },
                onSongClick = onSongClick,
                onPlaySongList = onPlaySongList,
                onDeletePlaylist = { onDeletePlaylist(selectedPlaylistWithSongs.playlist.playlistId) },
                onRemoveSong = { videoId ->
                    onRemoveSongFromPlaylist(selectedPlaylistWithSongs.playlist.playlistId, videoId)
                },
                onDownloadClick = onDeleteDownloadClick
            )
            return
        }

        // Library Main View Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Sort & Filter Menu Trigger
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    LibrarySortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.title,
                                    fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                currentSort = option
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tactile Overview Stats Bar (Quick interactive jump chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (selectedTab == 0) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    if (selectedTab == 0) Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.15f))
                    ) else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                    )
                ),
                modifier = Modifier.clickable { selectedTab = 0 }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${downloadedSongs.size} Offline",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (selectedTab == 1) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    if (selectedTab == 1) Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.15f))
                    ) else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                    )
                ),
                modifier = Modifier.clickable { selectedTab = 1 }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (selectedTab == 1) Color.White else Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${favoriteSongs.size} Liked",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (selectedTab == 2) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    if (selectedTab == 2) Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.15f))
                    ) else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                    )
                ),
                modifier = Modifier.clickable { selectedTab = 2 }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (selectedTab == 2) Color.White else MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${playlists.size} Playlists",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (selectedTab == 3) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    if (selectedTab == 3) Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.15f))
                    ) else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                    )
                ),
                modifier = Modifier.clickable { selectedTab = 3 }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (selectedTab == 3) Color.White else MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${deviceSongs.size} Local",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area based on Selected Tab
        when (selectedTab) {
            0 -> {
                // Offline Downloads List
                val sortedDownloads = sortSongs(downloadedSongs)
                if (sortedDownloads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No downloaded tracks yet.\nSearch songs and tap the download icon to save tracks for offline listening!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sorted by: ${currentSort.title}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { onPlaySongList(sortedDownloads, 0, false) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Play All", style = MaterialTheme.typography.labelMedium)
                                    }
                                    OutlinedButton(
                                        onClick = { onPlaySongList(sortedDownloads, 0, true) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        items(sortedDownloads) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = song.videoId == currentPlayingId,
                                downloadProgress = downloadProgress[song.videoId],
                                onClick = { onSongClick(song) },
                                onDownloadClick = { onDeleteDownloadClick(song) },
                                onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            1 -> {
                // Liked Songs List
                val sortedFavorites = sortSongs(favoriteSongs)
                if (sortedFavorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE91E63).copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No liked tracks yet.\nTap the heart icon on any player screen to build your favorites collection!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sorted by: ${currentSort.title}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { onPlaySongList(sortedFavorites, 0, false) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Play All", style = MaterialTheme.typography.labelMedium)
                                    }
                                    OutlinedButton(
                                        onClick = { onPlaySongList(sortedFavorites, 0, true) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Shuffle", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        items(sortedFavorites) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = song.videoId == currentPlayingId,
                                downloadProgress = downloadProgress[song.videoId],
                                onClick = { onSongClick(song) },
                                onDownloadClick = { onDeleteDownloadClick(song) },
                                onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            2 -> {
                // Playlists List with Grid vs List Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curated Playlists (${playlists.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Grid vs List Toggle
                        IconButton(onClick = { isPlaylistGridView = !isPlaylistGridView }) {
                            Icon(
                                imageVector = if (isPlaylistGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List View",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        FilledTonalButton(
                            onClick = { showCreatePlaylistDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No playlists yet.\nTap '+ New' to create your customized mixtape collections!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isPlaylistGridView) {
                    // 2-Column Grid View for Playlists
                    val playlistPairs = playlists.chunked(2)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(playlistPairs) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (playlist in pair) {
                                    ElevatedCard(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectPlaylist(playlist.playlistId) },
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFF6A1B9A),
                                                                Color(0xFF283593)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.QueueMusic,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(42.dp)
                                                )
                                                IconButton(
                                                    onClick = { onDeletePlaylist(playlist.playlistId) },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                        .size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = Color.White.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = playlist.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Custom Collection",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    // Classical List View for Playlists
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPlaylist(playlist.playlistId) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFF7B1FA2), Color(0xFF303F9F))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueueMusic,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Playlist Collection",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeletePlaylist(playlist.playlistId) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Playlist",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            3 -> {
                // Local Storage Device Audio Tracks
                DeviceTracksView(
                    deviceSongs = sortSongs(deviceSongs),
                    isScanningDevice = isScanningDevice,
                    currentPlayingId = currentPlayingId,
                    onScanDeviceTracks = onScanDeviceTracks,
                    onSongClick = onSongClick,
                    onPlaySongList = onPlaySongList,
                    onAddToPlaylistClick = onAddToPlaylistClick
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlistWithSongs: PlaylistWithSongs,
    currentPlayingId: String?,
    downloadProgress: Map<String, Float>,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlaySongList: (List<Song>, Int, Boolean) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveSong: (videoId: String) -> Unit,
    onDownloadClick: (Song) -> Unit
) {
    val songs = playlistWithSongs.songs.map { entity ->
        Song(
            videoId = entity.videoId,
            title = entity.title,
            artist = entity.artist,
            thumbnailUrl = entity.thumbnailUrl,
            durationMs = entity.durationMs,
            streamUrl = entity.streamUrl,
            cacheKey = entity.cacheKey,
            isDownloaded = entity.isDownloaded,
            addedTimestamp = entity.addedTimestamp,
            lastPlayedTimestamp = entity.lastPlayedTimestamp
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Playlists"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlistWithSongs.playlist.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeletePlaylist) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Playlist",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onPlaySongList(songs, 0, false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All")
                }
                OutlinedButton(
                    onClick = { onPlaySongList(songs, 0, true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracks in this playlist yet.\nSearch songs and tap 'Add to Playlist'!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = song.videoId == currentPlayingId,
                        downloadProgress = downloadProgress[song.videoId],
                        onClick = { onSongClick(song) },
                        onDownloadClick = { onDownloadClick(song) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun DeviceTracksView(
    deviceSongs: List<Song>,
    isScanningDevice: Boolean,
    currentPlayingId: String?,
    onScanDeviceTracks: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlaySongList: (List<Song>, Int, Boolean) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit
) {
    val context = LocalContext.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            onScanDeviceTracks()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && deviceSongs.isEmpty()) {
            onScanDeviceTracks()
        }
    }

    if (!hasPermission) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant storage permission to scan and play music files stored on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(permission) }) {
                    Text("Grant Permission")
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deviceSongs.size} tracks found",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (deviceSongs.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { onPlaySongList(deviceSongs, 0, false) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play All", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    IconButton(
                        onClick = onScanDeviceTracks,
                        enabled = !isScanningDevice
                    ) {
                        if (isScanningDevice) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rescan"
                            )
                        }
                    }
                }
            }

            if (deviceSongs.isEmpty() && !isScanningDevice) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No audio files found on your device.\nTap refresh to scan again.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(deviceSongs) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = song.videoId == currentPlayingId,
                            downloadProgress = null,
                            onClick = { onSongClick(song) },
                            onDownloadClick = { /* Already local */ },
                            onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
