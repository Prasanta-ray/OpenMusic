package com.openmusic.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Nightlife
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openmusic.app.domain.model.SearchFilter
import com.openmusic.app.domain.model.Song
import com.openmusic.app.ui.components.SongListItem

/**
 * Curated music genre definition for the Browse Grid (No Emojis, Pure Vector Icons)
 */
data class GenreCategory(
    val title: String,
    val searchKeyword: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color
)

private val BROWSE_GENRES = listOf(
    GenreCategory(
        title = "Bollywood Hits",
        searchKeyword = "Bollywood Hits",
        icon = Icons.Outlined.Movie,
        startColor = Color(0xFFE65100),
        endColor = Color(0xFFC2185B)
    ),
    GenreCategory(
        title = "Punjabi Wave",
        searchKeyword = "Punjabi Songs",
        icon = Icons.Outlined.Bolt,
        startColor = Color(0xFF7B1FA2),
        endColor = Color(0xFF303F9F)
    ),
    GenreCategory(
        title = "Global Top 50",
        searchKeyword = "Global Top 50",
        icon = Icons.Outlined.Public,
        startColor = Color(0xFF00695C),
        endColor = Color(0xFF00897B)
    ),
    GenreCategory(
        title = "Lo-Fi & Chill",
        searchKeyword = "Lo-Fi Beats",
        icon = Icons.Outlined.Coffee,
        startColor = Color(0xFF283593),
        endColor = Color(0xFF4527A0)
    ),
    GenreCategory(
        title = "Gym & Phonk",
        searchKeyword = "Gym Workout Songs",
        icon = Icons.Outlined.FitnessCenter,
        startColor = Color(0xFFC62828),
        endColor = Color(0xFF880E4F)
    ),
    GenreCategory(
        title = "Romantic Songs",
        searchKeyword = "Romantic Love Songs",
        icon = Icons.Outlined.FavoriteBorder,
        startColor = Color(0xFFE91E63),
        endColor = Color(0xFFFF8F00)
    ),
    GenreCategory(
        title = "Rock & Metal",
        searchKeyword = "Rock Anthems",
        icon = Icons.Default.MusicNote,
        startColor = Color(0xFF37474F),
        endColor = Color(0xFF212121)
    ),
    GenreCategory(
        title = "Bhakti & Spiritual",
        searchKeyword = "Bhakti Songs",
        icon = Icons.Outlined.Spa,
        startColor = Color(0xFFEF6C00),
        endColor = Color(0xFFF57F17)
    ),
    GenreCategory(
        title = "Indie & Acoustic",
        searchKeyword = "Indie Acoustic Music",
        icon = Icons.Outlined.WbSunny,
        startColor = Color(0xFF2E7D32),
        endColor = Color(0xFF558B2F)
    ),
    GenreCategory(
        title = "EDM & Club",
        searchKeyword = "EDM Dance Hits",
        icon = Icons.Outlined.GraphicEq,
        startColor = Color(0xFF0288D1),
        endColor = Color(0xFF6A1B9A)
    ),
    GenreCategory(
        title = "90s Nostalgia",
        searchKeyword = "90s Bollywood Hits",
        icon = Icons.Outlined.Radio,
        startColor = Color(0xFF5D4037),
        endColor = Color(0xFF8D6E63)
    ),
    GenreCategory(
        title = "Night Drive",
        searchKeyword = "Night Drive Songs",
        icon = Icons.Outlined.Nightlife,
        startColor = Color(0xFF0D47A1),
        endColor = Color(0xFF1976D2)
    )
)

private val TRENDING_SEARCH_TAGS = listOf(
    "Arijit Singh",
    "Punjabi Pop",
    "Lo-Fi Beats",
    "Top 50 Global",
    "AP Dhillon",
    "Rock Classics",
    "Anirudh Hits",
    "Gym Phonk",
    "Taylor Swift",
    "Sidhu Moose Wala",
    "Shreya Ghoshal"
)

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onSearch: (String) -> Unit,
    suggestions: List<String> = emptyList(),
    searchHistory: List<String> = emptyList(),
    onDeleteHistoryItem: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
    results: List<Song>,
    isSearching: Boolean,
    errorMessage: String?,
    currentPlayingId: String?,
    downloadProgress: Map<String, Float> = emptyMap(),
    onSongClick: (Song) -> Unit,
    onPlayAllClick: (List<Song>) -> Unit = {},
    onShuffleClick: (List<Song>) -> Unit = {},
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text("Search songs, artists, albums...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearch(query)
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(SearchFilter.SONGS, SearchFilter.ALL, SearchFilter.ARTISTS, SearchFilter.ALBUMS)
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area: Loading, Results, Suggestions, Empty State (Trending + Browse Grid), or Empty Results
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Searching across millions of tracks...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (query.isNotBlank() && results.isNotEmpty()) {
            // Search Results List with Header & Play All / Shuffle buttons
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${results.size} tracks found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onPlayAllClick(results) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play All",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { onShuffleClick(results) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shuffle", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                items(results) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = song.videoId == currentPlayingId,
                        downloadProgress = downloadProgress[song.videoId],
                        onClick = {
                            keyboardController?.hide()
                            onSongClick(song)
                        },
                        onDownloadClick = { onDownloadClick(song) },
                        onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // MiniPlayer padding
                }
            }
        } else if (query.isNotBlank() && suggestions.isNotEmpty()) {
            // Instant Search Suggestions Dropdown List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(suggestions) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide()
                                onQueryChange(suggestion)
                                onSearch(suggestion)
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                onQueryChange(suggestion)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NorthWest,
                                contentDescription = "Fill query",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else if (query.isBlank()) {
            // Empty Search State: Trending Pills, Recent Searches, and Glassmorphic 2-Column Genre Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Trending Searches Bar
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRENDING SEARCHES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TRENDING_SEARCH_TAGS) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0x14FFFFFF),
                                    border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.12f)),
                                    modifier = Modifier.clickable {
                                        keyboardController?.hide()
                                        onQueryChange(tag)
                                        onSearch(tag)
                                    }
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Recent Searches (if available)
                if (searchHistory.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RECENT SEARCHES",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = onClearHistory) {
                                    Text(
                                        text = "Clear all",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            // Show recent queries as clean removable glass chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(searchHistory) { historyItem ->
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = Color(0x14FFFFFF),
                                        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.10f)),
                                        modifier = Modifier.clickable {
                                            keyboardController?.hide()
                                            onQueryChange(historyItem)
                                            onSearch(historyItem)
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = historyItem,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            IconButton(
                                                onClick = { onDeleteHistoryItem(historyItem) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Browse All / Explore Genres 2-Column Grid
                item {
                    Text(
                        text = "BROWSE ALL CATEGORIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Chunk genres into 2-column rows
                val genrePairs = BROWSE_GENRES.chunked(2)
                items(genrePairs) { rowGenres ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (genre in rowGenres) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable {
                                        keyboardController?.hide()
                                        onQueryChange(genre.searchKeyword)
                                        onSearch(genre.searchKeyword)
                                    },
                                border = BorderStroke(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.04f))
                                    )
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(genre.startColor, genre.endColor)
                                            )
                                        )
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .padding(12.dp)
                                ) {
                                    // Title Text on Top Left
                                    Text(
                                        text = genre.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.TopStart),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Decorative Vector Icon in Frosted Disc on Bottom Right
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = genre.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (rowGenres.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // MiniPlayer padding
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracks found for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
