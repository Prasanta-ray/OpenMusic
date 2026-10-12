package com.openmusic.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmusic.app.data.local.cache.MediaCacheManager
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.SearchFilter
import com.openmusic.app.domain.model.Song
import com.openmusic.app.domain.repository.MusicRepository
import com.openmusic.app.player.controller.MusicPlayerController
import com.openmusic.app.player.state.RepeatMode
import com.openmusic.app.ui.navigation.Screen
import com.openmusic.app.data.local.entities.PlaylistEntity
import com.openmusic.app.data.local.entities.PlaylistWithSongs
import com.openmusic.app.data.local.scanner.LocalAudioScanner
import com.openmusic.app.domain.model.SongLyrics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val musicRepository: MusicRepository,
    val playerController: MusicPlayerController,
    private val cacheManager: MediaCacheManager,
    context: android.content.Context? = null
) : ViewModel() {

    private val localAudioScanner = context?.let { LocalAudioScanner(it) }
    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    val deviceSongs: StateFlow<List<Song>> = _deviceSongs.asStateFlow()

    private val _isScanningDevice = MutableStateFlow(false)
    val isScanningDevice: StateFlow<Boolean> = _isScanningDevice.asStateFlow()

    fun scanDeviceTracks() {
        val scanner = localAudioScanner ?: return
        viewModelScope.launch {
            _isScanningDevice.value = true
            val songs = scanner.scanLocalAudio()
            _deviceSongs.value = songs
            _isScanningDevice.value = false
        }
    }

    private val prefs = context?.getSharedPreferences("openmusic_prefs", android.content.Context.MODE_PRIVATE)

    private val _streamingQuality = MutableStateFlow(
        AudioQuality.fromString(prefs?.getString("streaming_audio_quality", if (prefs?.getBoolean("data_saver_enabled", false) == true) "LOW" else "HIGH"))
    )
    val streamingQuality: StateFlow<AudioQuality> = _streamingQuality.asStateFlow()

    private val _downloadQuality = MutableStateFlow(
        AudioQuality.fromString(prefs?.getString("download_audio_quality", "HIGH"))
    )
    val downloadQuality: StateFlow<AudioQuality> = _downloadQuality.asStateFlow()

    private val _isDataSaverEnabled = MutableStateFlow(_streamingQuality.value == AudioQuality.LOW)
    val isDataSaverEnabled: StateFlow<Boolean> = _isDataSaverEnabled.asStateFlow()

    fun setStreamingQuality(quality: AudioQuality) {
        _streamingQuality.value = quality
        _isDataSaverEnabled.value = (quality == AudioQuality.LOW)
        prefs?.edit()
            ?.putString("streaming_audio_quality", quality.id)
            ?.putBoolean("data_saver_enabled", quality == AudioQuality.LOW)
            ?.apply()
        val currentSong = playerState.value.currentSong
        if (currentSong != null && !playerState.value.isOfflineCache) {
            playSong(currentSong)
        }
    }

    fun setDownloadQuality(quality: AudioQuality) {
        _downloadQuality.value = quality
        prefs?.edit()?.putString("download_audio_quality", quality.id)?.apply()
    }

    fun setDataSaverEnabled(enabled: Boolean) {
        setStreamingQuality(if (enabled) AudioQuality.LOW else AudioQuality.HIGH)
    }

    private val _isAmoledBlackTheme = MutableStateFlow(
        prefs?.getBoolean("amoled_black_theme", false) ?: false
    )
    val isAmoledBlackTheme: StateFlow<Boolean> = _isAmoledBlackTheme.asStateFlow()

    fun setAmoledBlackTheme(enabled: Boolean) {
        _isAmoledBlackTheme.value = enabled
        prefs?.edit()?.putBoolean("amoled_black_theme", enabled)?.apply()
    }

    private val _diskCacheBytes = MutableStateFlow(musicRepository.getTotalCacheBytes())
    val diskCacheBytes: StateFlow<Long> = _diskCacheBytes.asStateFlow()

    fun refreshDiskCacheBytes() {
        _diskCacheBytes.value = musicRepository.getTotalCacheBytes()
    }

    fun clearDiskCache(): Long {
        val freed = musicRepository.clearDiskCache()
        _diskCacheBytes.value = musicRepository.getTotalCacheBytes()
        return freed
    }

    suspend fun exportPlaylistsAsJson(): String {
        val allPlaylists = musicRepository.getAllPlaylists().firstOrNull() ?: emptyList()
        val jsonArray = org.json.JSONArray()
        for (pl in allPlaylists) {
            val plObj = org.json.JSONObject()
            plObj.put("playlistId", pl.playlistId)
            plObj.put("title", pl.title)
            plObj.put("createdAt", pl.createdAt)
            val songsObj = org.json.JSONArray()
            val plWithSongs = musicRepository.getPlaylistWithSongs(pl.playlistId).firstOrNull()
            plWithSongs?.songs?.forEach { song ->
                val sObj = org.json.JSONObject()
                sObj.put("videoId", song.videoId)
                sObj.put("title", song.title)
                sObj.put("artist", song.artist)
                sObj.put("thumbnailUrl", song.thumbnailUrl)
                sObj.put("durationMs", song.durationMs)
                songsObj.put(sObj)
            }
            plObj.put("songs", songsObj)
            jsonArray.put(plObj)
        }
        return jsonArray.toString(2)
    }

    suspend fun importPlaylistsFromJson(jsonString: String): Result<Int> {
        return try {
            val jsonArray = org.json.JSONArray(jsonString)
            var count = 0
            for (i in 0 until jsonArray.length()) {
                val plObj = jsonArray.getJSONObject(i)
                val title = plObj.optString("title", "Imported Playlist")
                val newPlId = musicRepository.createPlaylist(title)
                val songsArr = plObj.optJSONArray("songs")
                if (songsArr != null) {
                    for (j in 0 until songsArr.length()) {
                        val s = songsArr.getJSONObject(j)
                        val song = Song(
                            videoId = s.getString("videoId"),
                            title = s.getString("title"),
                            artist = s.getString("artist"),
                            thumbnailUrl = s.optString("thumbnailUrl", ""),
                            durationMs = s.optLong("durationMs", 0L)
                        )
                        musicRepository.addSongToPlaylist(newPlId, song)
                    }
                }
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportPlaylistsAsM3u(): String {
        val allPlaylists = musicRepository.getAllPlaylists().firstOrNull() ?: emptyList()
        val sb = StringBuilder()
        sb.append("#EXTM3U\n\n")
        for (pl in allPlaylists) {
            sb.append("#PLAYLIST: ${pl.title}\n")
            val plWithSongs = musicRepository.getPlaylistWithSongs(pl.playlistId).firstOrNull()
            plWithSongs?.songs?.forEach { song ->
                val seconds = song.durationMs / 1000
                sb.append("#EXTINF:$seconds,${song.artist} - ${song.title}\n")
                sb.append("https://music.youtube.com/watch?v=${song.videoId}\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    val searchHistory: StateFlow<List<String>> = musicRepository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchFilter = MutableStateFlow(SearchFilter.SONGS)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    val playerState = playerController.playerState

    val downloadedSongs: StateFlow<List<Song>> = musicRepository.getAllDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val audioFxState = com.openmusic.app.player.audiofx.AudioFxManager.state

    val sleepTimerManager = com.openmusic.app.player.timer.SleepTimerManager(playerController)
    val sleepTimerState = sleepTimerManager.state

    val downloadProgress: StateFlow<Map<String, Float>> = musicRepository.downloadProgress
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.getRecentlyPlayedSongs(20)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = musicRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPlaylistWithSongs: StateFlow<PlaylistWithSongs?> = _selectedPlaylistId
        .flatMapLatest { id ->
            if (id != null) musicRepository.getPlaylistWithSongs(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun createPlaylist(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            musicRepository.createPlaylist(title.trim())
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, song)
        }
    }

    fun createPlaylistAndAddSong(title: String, song: Song) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newId = musicRepository.createPlaylist(title.trim())
            musicRepository.addSongToPlaylist(newId, song)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, videoId: String) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, videoId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            if (_selectedPlaylistId.value == playlistId) {
                _selectedPlaylistId.value = null
            }
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    private val _currentLyrics = MutableStateFlow<SongLyrics?>(null)
    val currentLyrics: StateFlow<SongLyrics?> = _currentLyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    private val _currentArtistDetail = MutableStateFlow<com.openmusic.app.domain.model.ArtistDetail?>(null)
    val currentArtistDetail: StateFlow<com.openmusic.app.domain.model.ArtistDetail?> = _currentArtistDetail.asStateFlow()

    private val _isLoadingArtist = MutableStateFlow(false)
    val isLoadingArtist: StateFlow<Boolean> = _isLoadingArtist.asStateFlow()

    fun loadArtist(artistName: String) {
        if (artistName.isBlank()) return
        viewModelScope.launch {
            _isLoadingArtist.value = true
            musicRepository.getArtistDetail(artistName)
                .onSuccess {
                    _currentArtistDetail.value = it
                    _isLoadingArtist.value = false
                }
                .onFailure {
                    _isLoadingArtist.value = false
                }
        }
    }

    private val _currentAlbumDetail = MutableStateFlow<com.openmusic.app.domain.model.Album?>(null)
    val currentAlbumDetail: StateFlow<com.openmusic.app.domain.model.Album?> = _currentAlbumDetail.asStateFlow()

    private val _isLoadingAlbum = MutableStateFlow(false)
    val isLoadingAlbum: StateFlow<Boolean> = _isLoadingAlbum.asStateFlow()

    fun loadAlbum(albumName: String, artist: String = "") {
        if (albumName.isBlank()) return
        viewModelScope.launch {
            _isLoadingAlbum.value = true
            musicRepository.getAlbumDetail(albumName, artist)
                .onSuccess {
                    _currentAlbumDetail.value = it
                    _isLoadingAlbum.value = false
                }
                .onFailure {
                    _isLoadingAlbum.value = false
                }
        }
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val listToPlay = if (shuffle) songs.shuffled() else songs
        val targetIndex = if (shuffle) 0 else startIndex.coerceIn(0, songs.lastIndex)
        val songToPlay = listToPlay[targetIndex]
        playerController.setQueue(listToPlay)
        playSong(songToPlay)
    }

    init {
        // Pre-populate search with trending hits for initial Home feed
        loadInitialTrendingFeed()

        // Handle auto-advance to next track on playback end
        playerController.onTrackEndedListener = {
            playNext()
        }

        // Observe track changes to automatically load synchronized lyrics
        viewModelScope.launch {
            playerState.map { it.currentSong?.videoId }
                .distinctUntilChanged()
                .collect { videoId ->
                    val song = playerState.value.currentSong
                    if (song != null) {
                        loadLyrics(song)
                    } else {
                        _currentLyrics.value = null
                    }
                }
        }
    }

    fun loadLyrics(song: Song) {
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            musicRepository.getLyrics(song)
                .onSuccess { lyrics ->
                    _currentLyrics.value = lyrics
                    _isLoadingLyrics.value = false
                }
                .onFailure {
                    _isLoadingLyrics.value = false
                }
        }
    }

    fun adjustLyricsSyncOffset(deltaMs: Long) {
        val current = _currentLyrics.value ?: return
        val newOffset = current.syncOffsetMs + deltaMs
        val updated = current.copy(syncOffsetMs = newOffset)
        _currentLyrics.value = updated
        viewModelScope.launch {
            musicRepository.updateLyricsSyncOffset(current.videoId, newOffset)
        }
    }

    fun resetLyricsSyncOffset() {
        val current = _currentLyrics.value ?: return
        val updated = current.copy(syncOffsetMs = 0L)
        _currentLyrics.value = updated
        viewModelScope.launch {
            musicRepository.updateLyricsSyncOffset(current.videoId, 0L)
        }
    }

    fun selectScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    private var suggestionJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        suggestionJob?.cancel()
        if (query.isBlank()) {
            _searchSuggestions.value = emptyList()
        } else {
            suggestionJob = viewModelScope.launch {
                kotlinx.coroutines.delay(200)
                musicRepository.getSearchSuggestions(query)
                    .onSuccess { suggestions ->
                        _searchSuggestions.value = suggestions
                    }
            }
        }
    }

    fun onFilterChange(filter: SearchFilter) {
        _searchFilter.value = filter
        if (_searchQuery.value.isNotBlank()) {
            search(_searchQuery.value)
        }
    }

    fun search(query: String = _searchQuery.value) {
        if (query.isBlank()) return
        _searchQuery.value = query
        _searchSuggestions.value = emptyList()
        viewModelScope.launch {
            musicRepository.addSearchQuery(query)
            _isSearching.value = true
            _searchError.value = null
            musicRepository.search(query, _searchFilter.value)
                .onSuccess { songs ->
                    _searchResults.value = songs
                    _isSearching.value = false
                }
                .onFailure { error ->
                    _searchError.value = error.message ?: "Failed to load results"
                    _isSearching.value = false
                }
        }
    }

    fun deleteSearchHistory(query: String) {
        viewModelScope.launch {
            musicRepository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            musicRepository.clearSearchHistory()
        }
    }

    private val _currentVibe = MutableStateFlow("All")
    val currentVibe: StateFlow<String> = _currentVibe.asStateFlow()

    fun selectVibe(vibe: String) {
        _currentVibe.value = vibe
        viewModelScope.launch {
            _isSearching.value = true
            val query = if (vibe == "All") "Top Songs 2024" else "$vibe Hits"
            musicRepository.search(query, SearchFilter.SONGS)
                .onSuccess { songs ->
                    _searchResults.value = songs
                    _isSearching.value = false
                }
                .onFailure {
                    _isSearching.value = false
                }
        }
    }

    fun refreshTrendingFeed() {
        selectVibe(_currentVibe.value)
    }

    private fun loadInitialTrendingFeed() {
        viewModelScope.launch {
            musicRepository.search("Top Songs 2024", SearchFilter.SONGS)
                .onSuccess { songs ->
                    if (_searchResults.value.isEmpty()) {
                        _searchResults.value = songs
                    }
                }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            if (song.isLocalTrack) {
                // Instant on-device local storage playback directly through ExoPlayer
                val streamUrl = song.streamUrl ?: return@launch
                val playableSong = song.copy(streamUrl = streamUrl)
                playerController.play(playableSong, streamInfo = null, isOfflineCache = true)
                return@launch
            }

            val isCachedLocally = song.isDownloaded || cacheManager.isFullyCached(song.videoId)
            if (isCachedLocally) {
                // Instant offline playback directly from SimpleCache without network calls
                val streamUrl = song.streamUrl?.takeIf { it.isNotBlank() } ?: "https://cached.local/${song.videoId}"
                val playableSong = song.copy(streamUrl = streamUrl)
                playerController.play(playableSong, streamInfo = null, isOfflineCache = true)
                musicRepository.updateLastPlayed(song.videoId)
                return@launch
            }

            // 1. Resolve optimal audio stream based on granular AudioQuality preference
            val quality = _streamingQuality.value
            musicRepository.getOptimalAudioStream(song.videoId, quality = quality)
                .onSuccess { streamInfo ->
                    val playableSong = song.copy(streamUrl = streamInfo.streamUrl)
                    playerController.play(playableSong, streamInfo = streamInfo, isOfflineCache = false)

                    // 2. Persist to Room recent history
                    musicRepository.saveSong(playableSong)
                    musicRepository.updateLastPlayed(song.videoId)

                    // 3. Fetch automated Up Next / radio queue
                    loadRelatedQueue(song.videoId)
                }
                .onFailure { error ->
                    _searchError.value = "Cannot stream: ${error.message}"
                }
        }
    }

    private fun loadRelatedQueue(videoId: String) {
        viewModelScope.launch {
            musicRepository.getRelatedTracks(videoId).onSuccess { queue ->
                playerController.setQueue(queue)
            }
        }
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun playNext() {
        val state = playerState.value
        val currentQueue = state.queue
        val currentSong = state.currentSong ?: return

        if (state.repeatMode == RepeatMode.ONE) {
            seekTo(0)
            playerController.play(currentSong, state.streamInfo, state.isOfflineCache)
            return
        }

        val currentIndex = currentQueue.indexOfFirst { it.videoId == currentSong.videoId }
        if (currentIndex != -1 && currentIndex + 1 < currentQueue.size) {
            val nextSong = currentQueue[currentIndex + 1]
            playSong(nextSong)
        } else if (state.repeatMode == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            val firstSong = currentQueue.first()
            playSong(firstSong)
        } else {
            playerController.playNext()
        }
    }

    fun playNextInQueue(song: Song) {
        val state = playerState.value
        val currentSong = state.currentSong
        val queue = state.queue.toMutableList()
        if (currentSong != null) {
            val currentIndex = queue.indexOfFirst { it.videoId == currentSong.videoId }
            if (currentIndex != -1) {
                queue.add(currentIndex + 1, song)
            } else {
                queue.add(0, song)
            }
        } else {
            queue.add(song)
        }
        playerController.setQueue(queue)
    }

    fun addToQueue(song: Song) {
        val queue = playerState.value.queue.toMutableList()
        queue.add(song)
        playerController.setQueue(queue)
    }

    fun playPrevious() {
        val state = playerState.value
        val currentQueue = state.queue
        val currentSong = state.currentSong ?: return

        if (state.currentPositionMs > 3000L) {
            seekTo(0)
            return
        }

        val currentIndex = currentQueue.indexOfFirst { it.videoId == currentSong.videoId }
        if (currentIndex > 0) {
            val prevSong = currentQueue[currentIndex - 1]
            playSong(prevSong)
        } else if (state.repeatMode == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            val lastSong = currentQueue.last()
            playSong(lastSong)
        } else {
            seekTo(0)
        }
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleRepeatMode() {
        playerController.toggleRepeatMode()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }

    fun toggleDownload(song: Song) {
        android.util.Log.d("MainViewModel", "toggleDownload: ${song.title} (${song.videoId}), isDownloaded=${song.isDownloaded}")
        viewModelScope.launch {
            if (song.isDownloaded) {
                android.util.Log.d("MainViewModel", "Removing download: ${song.videoId}")
                musicRepository.removeDownload(song.videoId, song.cacheKey)
            } else {
                android.util.Log.d("MainViewModel", "Triggering downloadTrack: ${song.videoId} with quality ${_downloadQuality.value}")
                val result = musicRepository.downloadTrack(song, _downloadQuality.value)
                result.onSuccess {
                    android.util.Log.d("MainViewModel", "downloadTrack SUCCESS for ${song.title}")
                }.onFailure { error ->
                    android.util.Log.e("MainViewModel", "downloadTrack FAILED for ${song.title}: ${error.message}", error)
                    _searchError.value = "Download failed: ${error.message}"
                }
            }
        }
    }

    fun cancelDownload(videoId: String) {
        viewModelScope.launch {
            musicRepository.cancelDownload(videoId)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song)
        }
    }

    fun setAudioFxEnabled(enabled: Boolean) = com.openmusic.app.player.audiofx.AudioFxManager.setEnabled(enabled)
    fun setAudioFxPreset(preset: String) = com.openmusic.app.player.audiofx.AudioFxManager.applyPreset(preset)
    fun setAudioFxBandLevel(band: Short, levelMb: Short) = com.openmusic.app.player.audiofx.AudioFxManager.setBandLevel(band, levelMb)
    fun setAudioFxBassBoost(strength: Int) = com.openmusic.app.player.audiofx.AudioFxManager.setBassBoost(strength)
    fun setAudioFxLoudnessNormalizerEnabled(enabled: Boolean) = com.openmusic.app.player.audiofx.AudioFxManager.setLoudnessNormalizerEnabled(enabled)
    fun setAudioFxTargetGainMb(gainMb: Int) = com.openmusic.app.player.audiofx.AudioFxManager.setTargetGainMb(gainMb)

    fun startSleepTimerMinutes(minutes: Int) = sleepTimerManager.startMinutesTimer(minutes)
    fun startSleepTimerEndOfTrack() = sleepTimerManager.startEndOfTrackTimer()
    fun cancelSleepTimer() = sleepTimerManager.cancelTimer()

    fun moveQueueItem(fromIndex: Int, toIndex: Int) = playerController.moveQueueItem(fromIndex, toIndex)
    fun removeQueueItem(index: Int) = playerController.removeQueueItem(index)
    fun clearQueue() = playerController.clearQueue()

    fun expandPlayer() {
        _isPlayerExpanded.value = true
    }

    fun collapsePlayer() {
        _isPlayerExpanded.value = false
    }
}
