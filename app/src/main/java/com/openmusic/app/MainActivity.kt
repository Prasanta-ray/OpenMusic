package com.openmusic.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openmusic.app.data.local.cache.MediaCacheManager
import com.openmusic.app.data.local.database.AppDatabase
import com.openmusic.app.data.remote.innertube.InnerTubeApiClient
import com.openmusic.app.data.repository.MusicRepositoryImpl
import com.openmusic.app.domain.model.Song
import com.openmusic.app.player.controller.MusicPlayerController
import com.openmusic.app.ui.MainViewModel
import com.openmusic.app.ui.components.AddToPlaylistDialog
import com.openmusic.app.ui.components.MiniPlayer
import com.openmusic.app.ui.navigation.Screen
import com.openmusic.app.ui.screens.home.HomeScreen
import com.openmusic.app.ui.screens.library.LibraryScreen
import com.openmusic.app.ui.screens.player.PlayerScreen
import com.openmusic.app.ui.screens.search.SearchScreen
import com.openmusic.app.ui.theme.OpenMusicTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val db = AppDatabase.getDatabase(applicationContext)
                        val cacheMgr = MediaCacheManager(application)
                        val api = InnerTubeApiClient()
                        val repo = MusicRepositoryImpl(db.songDao(), db.playlistDao(), db.lyricsDao(), db.searchHistoryDao(), cacheMgr, api)
                        val player = MusicPlayerController(applicationContext)
                        return MainViewModel(repo, player, cacheMgr, applicationContext) as T
                    }
                }
            )
            val isAmoledBlack by viewModel.isAmoledBlackTheme.collectAsState()

            OpenMusicTheme(darkTheme = true, amoledBlack = isAmoledBlack) {
                MainAppContent(viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()

    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylistWithSongs by viewModel.selectedPlaylistWithSongs.collectAsState()
    val currentLyrics by viewModel.currentLyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()
    val currentArtistDetail by viewModel.currentArtistDetail.collectAsState()
    val isLoadingArtist by viewModel.isLoadingArtist.collectAsState()
    val currentAlbumDetail by viewModel.currentAlbumDetail.collectAsState()
    val isLoadingAlbum by viewModel.isLoadingAlbum.collectAsState()
    val audioFxState by viewModel.audioFxState.collectAsState()
    val sleepTimerState by viewModel.sleepTimerState.collectAsState()
    val streamingQuality by viewModel.streamingQuality.collectAsState()
    val downloadQuality by viewModel.downloadQuality.collectAsState()
    val isDataSaver by viewModel.isDataSaverEnabled.collectAsState()
    val deviceSongs by viewModel.deviceSongs.collectAsState()
    val isScanningDevice by viewModel.isScanningDevice.collectAsState()
    val currentVibe by viewModel.currentVibe.collectAsState()
    var isSplashScreenVisible by remember { mutableStateOf(true) }
    var navigationStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val currentScreen = navigationStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        navigationStack = navigationStack + screen
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack = navigationStack.dropLast(1)
        }
    }

    val isCurrentSongFavorite = playerState.currentSong?.let { song ->
        favoriteSongs.any { it.videoId == song.videoId }
    } ?: false

    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDataSaverDialog by remember { mutableStateOf(false) }
    var showPlaybackSpeedDialog by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    androidx.compose.runtime.LaunchedEffect(isPlayerExpanded) {
        if (isPlayerExpanded) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    BackHandler(enabled = isSplashScreenVisible || isPlayerExpanded || navigationStack.size > 1) {
        if (isSplashScreenVisible) {
            isSplashScreenVisible = false
        } else if (isPlayerExpanded) {
            viewModel.collapsePlayer()
        } else {
            navigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column {
                    // MiniPlayer persistent bar
                    if (playerState.currentSong != null && !isPlayerExpanded) {
                        MiniPlayer(
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            isFavorite = isCurrentSongFavorite,
                            currentPositionMs = playerState.currentPositionMs,
                            durationMs = playerState.durationMs,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onFavoriteClick = { playerState.currentSong?.let { viewModel.toggleFavorite(it) } },
                            onExpandClick = { viewModel.expandPlayer() }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        tonalElevation = 0.dp,
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Home,
                            onClick = {
                                viewModel.selectScreen(Screen.Home)
                                navigationStack = listOf(Screen.Home)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Search,
                            onClick = {
                                viewModel.selectScreen(Screen.Search)
                                navigationStack = listOf(Screen.Search)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Search) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text("Search") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Library,
                            onClick = {
                                viewModel.selectScreen(Screen.Library)
                                navigationStack = listOf(Screen.Library)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Library) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                    contentDescription = "Library"
                                )
                            },
                            label = { Text("Library") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    is Screen.Home -> HomeScreen(
                        trendingSongs = searchResults,
                        recentlyPlayed = recentlyPlayed,
                        favoriteSongs = favoriteSongs,
                        downloadedSongs = downloadedSongs,
                        playlists = playlists,
                        currentPlayingId = playerState.currentSong?.videoId,
                        downloadProgress = downloadProgress,
                        currentVibe = currentVibe,
                        isAudioFxActive = audioFxState.isEnabled,
                        sleepTimerRemainingSeconds = sleepTimerState.remainingMillis / 1000,
                        isSleepTimerActive = sleepTimerState.isActive,
                        onVibeSelect = { viewModel.selectVibe(it) },
                        onRefresh = { viewModel.refreshTrendingFeed() },
                        onEqualizerClick = { showEqualizerDialog = true },
                        onSleepTimerClick = { showSleepTimerDialog = true },
                        onPlayAllTrending = { viewModel.playSongList(searchResults, 0, false) },
                        onShuffleTrending = { viewModel.playSongList(searchResults, 0, true) },
                        onPlaySongList = { songs, startIdx, shuffle -> viewModel.playSongList(songs, startIdx, shuffle) },
                        onSongClick = { viewModel.playSong(it) },
                        onDownloadClick = { viewModel.toggleDownload(it) },
                        onAddToPlaylistClick = { songForAddToPlaylist = it },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onPlayNextInQueue = { viewModel.playNextInQueue(it) },
                        onNavigateToLibrary = {
                            viewModel.selectScreen(Screen.Library)
                            navigationStack = listOf(Screen.Library)
                        },
                        onNavigateToSearch = {
                            viewModel.selectScreen(Screen.Search)
                            navigationStack = listOf(Screen.Search)
                        },
                        onSettingsClick = { navigateTo(Screen.Settings) }
                    )
                    is Screen.Search -> SearchScreen(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        selectedFilter = searchFilter,
                        onFilterChange = { viewModel.onFilterChange(it) },
                        onSearch = { viewModel.search(it) },
                        suggestions = searchSuggestions,
                        searchHistory = searchHistory,
                        onDeleteHistoryItem = { viewModel.deleteSearchHistory(it) },
                        onClearHistory = { viewModel.clearSearchHistory() },
                        results = searchResults,
                        isSearching = isSearching,
                        errorMessage = searchError,
                        currentPlayingId = playerState.currentSong?.videoId,
                        downloadProgress = downloadProgress,
                        onSongClick = { viewModel.playSong(it) },
                        onPlayAllClick = { viewModel.playSongList(searchResults, 0, false) },
                        onShuffleClick = { viewModel.playSongList(searchResults, 0, true) },
                        onDownloadClick = { viewModel.toggleDownload(it) },
                        onAddToPlaylistClick = { songForAddToPlaylist = it }
                    )
                    is Screen.Library -> LibraryScreen(
                        downloadedSongs = downloadedSongs,
                        favoriteSongs = favoriteSongs,
                        playlists = playlists,
                        deviceSongs = deviceSongs,
                        isScanningDevice = isScanningDevice,
                        selectedPlaylistWithSongs = selectedPlaylistWithSongs,
                        currentPlayingId = playerState.currentSong?.videoId,
                        downloadProgress = downloadProgress,
                        onSongClick = { viewModel.playSong(it) },
                        onDeleteDownloadClick = { viewModel.toggleDownload(it) },
                        onAddToPlaylistClick = { songForAddToPlaylist = it },
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                        onSelectPlaylist = { viewModel.selectPlaylist(it) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onRemoveSongFromPlaylist = { playlistId, videoId ->
                            viewModel.removeSongFromPlaylist(playlistId, videoId)
                        },
                        onScanDeviceTracks = { viewModel.scanDeviceTracks() },
                        onPlaySongList = { songs, startIdx, shuffle -> viewModel.playSongList(songs, startIdx, shuffle) }
                    )
                    is Screen.ArtistDetail -> com.openmusic.app.ui.screens.artist.ArtistDetailScreen(
                        artistDetail = currentArtistDetail,
                        isLoading = isLoadingArtist,
                        currentPlayingId = playerState.currentSong?.videoId,
                        downloadProgress = downloadProgress,
                        onBackClick = { navigateBack() },
                        onSongClick = { viewModel.playSong(it) },
                        onPlayAllClick = {
                            currentArtistDetail?.let {
                                viewModel.playSongList(it.topSongs, 0, false)
                            }
                        },
                        onShuffleAllClick = {
                            currentArtistDetail?.let {
                                viewModel.playSongList(it.topSongs, 0, true)
                            }
                        },
                        onAlbumClick = { album ->
                            viewModel.loadAlbum(album.title, album.artist)
                            navigateTo(Screen.AlbumDetail(album.title, artist = album.artist))
                        },
                        onDownloadClick = { viewModel.toggleDownload(it) },
                        onAddToPlaylistClick = { songForAddToPlaylist = it }
                    )
                    is Screen.AlbumDetail -> com.openmusic.app.ui.screens.album.AlbumDetailScreen(
                        album = currentAlbumDetail,
                        isLoading = isLoadingAlbum,
                        currentPlayingId = playerState.currentSong?.videoId,
                        downloadProgress = downloadProgress,
                        onBackClick = { navigateBack() },
                        onArtistClick = { artistName ->
                            viewModel.loadArtist(artistName)
                            navigateTo(Screen.ArtistDetail(artistName))
                        },
                        onSongClick = { viewModel.playSong(it) },
                        onPlayAllClick = {
                            currentAlbumDetail?.let {
                                viewModel.playSongList(it.tracks, 0, false)
                            }
                        },
                        onShuffleAllClick = {
                            currentAlbumDetail?.let {
                                viewModel.playSongList(it.tracks, 0, true)
                            }
                        },
                        onDownloadClick = { viewModel.toggleDownload(it) },
                        onAddToPlaylistClick = { songForAddToPlaylist = it }
                    )
                    is Screen.Settings -> com.openmusic.app.ui.screens.settings.SettingsScreen(
                        viewModel = viewModel,
                        onBackClick = { navigateBack() },
                        onNavigateToScreen = { navigateTo(it) }
                    )
                    is Screen.LegalHub -> com.openmusic.app.ui.screens.legal.LegalHubScreen(
                        onBackClick = { navigateBack() },
                        onNavigate = { navigateTo(it) }
                    )
                    is Screen.AboutCreator -> com.openmusic.app.ui.screens.legal.AboutCreatorScreen(
                        onBackClick = { navigateBack() },
                        onContactClick = { navigateTo(Screen.ContactCreator) }
                    )
                    is Screen.ContactCreator -> com.openmusic.app.ui.screens.legal.ContactCreatorScreen(
                        onBackClick = { navigateBack() }
                    )
                    is Screen.TermsAndConditions -> com.openmusic.app.ui.screens.legal.TermsAndConditionsScreen(
                        onBackClick = { navigateBack() }
                    )
                    is Screen.PrivacyPolicy -> com.openmusic.app.ui.screens.legal.PrivacyPolicyScreen(
                        onBackClick = { navigateBack() }
                    )
                    is Screen.Splash -> com.openmusic.app.ui.screens.splash.LoadingScreen(
                        onLoadingComplete = { navigateBack() }
                    )
                    else -> Unit
                }
            }
        }

        // Add to Playlist Dialog
        songForAddToPlaylist?.let { song ->
            AddToPlaylistDialog(
                song = song,
                playlists = playlists,
                onAddToPlaylist = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, song)
                },
                onCreateAndAdd = { title ->
                    viewModel.createPlaylistAndAddSong(title, song)
                },
                onDismiss = { songForAddToPlaylist = null }
            )
        }

        // Fullscreen Now Playing Overlay
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PlayerScreen(
                playerState = playerState,
                isFavorite = isCurrentSongFavorite,
                isDataSaver = isDataSaver,
                quality = streamingQuality,
                sleepTimerState = sleepTimerState,
                lyrics = currentLyrics,
                isLoadingLyrics = isLoadingLyrics,
                downloadProgress = downloadProgress,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onFavoriteClick = { playerState.currentSong?.let { viewModel.toggleFavorite(it) } },
                onEqualizerClick = { showEqualizerDialog = true },
                onSleepTimerClick = { showSleepTimerDialog = true },
                onCodecBadgeClick = { showDataSaverDialog = true },
                onSpeedBadgeClick = { showPlaybackSpeedDialog = true },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeatMode() },
                onSeekTo = { viewModel.seekTo(it) },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onSongClick = { viewModel.playSong(it) },
                onArtistClick = { artistName ->
                    viewModel.collapsePlayer()
                    viewModel.loadArtist(artistName)
                    navigateTo(Screen.ArtistDetail(artistName))
                },
                onDownloadClick = { viewModel.toggleDownload(it) },
                onMoveQueueItem = { from, to -> viewModel.moveQueueItem(from, to) },
                onRemoveQueueItem = { index -> viewModel.removeQueueItem(index) },
                onClearQueue = { viewModel.clearQueue() },
                onAdjustSyncOffset = { viewModel.adjustLyricsSyncOffset(it) },
                onResetSyncOffset = { viewModel.resetLyricsSyncOffset() },
                onCollapse = { viewModel.collapsePlayer() }
            )
        }

        // Hardware Equalizer, Bass Boost & Loudness Normalizer Dialog
        if (showEqualizerDialog) {
            com.openmusic.app.ui.components.EqualizerDialog(
                audioFxState = audioFxState,
                onToggleEnabled = { viewModel.setAudioFxEnabled(it) },
                onPresetSelected = { viewModel.setAudioFxPreset(it) },
                onBandLevelChange = { band, level -> viewModel.setAudioFxBandLevel(band, level) },
                onBassBoostChange = { strength -> viewModel.setAudioFxBassBoost(strength) },
                onLoudnessNormalizerChange = { viewModel.setAudioFxLoudnessNormalizerEnabled(it) },
                onTargetGainChange = { viewModel.setAudioFxTargetGainMb(it) },
                onDismiss = { showEqualizerDialog = false }
            )
        }

        // Sleep Timer Dialog
        if (showSleepTimerDialog) {
            com.openmusic.app.ui.components.SleepTimerDialog(
                sleepTimerState = sleepTimerState,
                onStartMinutes = { viewModel.startSleepTimerMinutes(it) },
                onStartEndOfTrack = { viewModel.startSleepTimerEndOfTrack() },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                onDismiss = { showSleepTimerDialog = false }
            )
        }

        // Stream Quality & Granular Audio Selector Dialog
        if (showDataSaverDialog) {
            com.openmusic.app.ui.components.AudioQualityDialog(
                streamInfo = playerState.streamInfo,
                isOfflineCache = playerState.isOfflineCache || (playerState.currentSong?.isDownloaded == true),
                currentStreamingQuality = streamingQuality,
                currentDownloadQuality = downloadQuality,
                onSelectStreamingQuality = { viewModel.setStreamingQuality(it) },
                onSelectDownloadQuality = { viewModel.setDownloadQuality(it) },
                onDismiss = { showDataSaverDialog = false }
            )
        }

        // Playback Speed Dialog
        if (showPlaybackSpeedDialog) {
            com.openmusic.app.ui.components.PlaybackSpeedDialog(
                currentSpeed = playerState.playbackSpeed,
                onSpeedSelected = { viewModel.setPlaybackSpeed(it) },
                onDismiss = { showPlaybackSpeedDialog = false }
            )
        }

        // App Launch Splash & Loading Screen with Logo and Versions
        androidx.compose.animation.AnimatedVisibility(
            visible = isSplashScreenVisible,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
        ) {
            com.openmusic.app.ui.screens.splash.LoadingScreen(
                onLoadingComplete = { isSplashScreenVisible = false }
            )
        }
    }
}
