package com.openmusic.app.player.controller

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import com.openmusic.app.domain.model.AudioStreamInfo
import com.openmusic.app.domain.model.Song
import com.openmusic.app.player.service.PlaybackService
import com.openmusic.app.player.state.PlayerState
import com.openmusic.app.player.state.RepeatMode
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
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Connects the UI to [PlaybackService] via AndroidX Media3's [MediaController].
 * Ensures seamless background playback, state synchronization, and lock-screen controls.
 */
@OptIn(UnstableApi::class)
class MusicPlayerController(
    private val context: Context
) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val imageLoader = ImageLoader(context)
    private var originalQueue: List<Song> = emptyList()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    var onTrackEndedListener: (() -> Unit)? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener({
            try {
                val controller = future.get()
                mediaController = controller
                setupPlayerListener(controller)
                syncState(controller)
            } catch (e: Exception) {
                // If service is not yet bound or connecting
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTicker()
                } else {
                    progressJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _playerState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = player.duration.coerceAtLeast(0L),
                        bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                    )
                }
                if (playbackState == Player.STATE_ENDED) {
                    onTrackEndedListener?.invoke() ?: playNext()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (mediaItem != null) {
                    val currentQueue = _playerState.value.queue
                    val matchingSong = currentQueue.firstOrNull { it.videoId == mediaItem.mediaId }
                    if (matchingSong != null) {
                        _playerState.update { it.copy(currentSong = matchingSong) }
                    }
                }
            }
        })
    }

    private fun syncState(player: Player) {
        if (_playerState.value.playbackSpeed != 1.0f) {
            player.playbackParameters = PlaybackParameters(_playerState.value.playbackSpeed)
        }
        if (_playerState.value.repeatMode == RepeatMode.ONE) {
            player.repeatMode = Player.REPEAT_MODE_ONE
        }
        _playerState.update {
            it.copy(
                isPlaying = player.isPlaying,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(0L)
            )
        }
        if (player.isPlaying) {
            startProgressTicker()
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playerState.update {
                            it.copy(
                                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                                bufferedPositionMs = controller.bufferedPosition.coerceAtLeast(0L),
                                durationMs = controller.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private suspend fun getOrCreateController(): MediaController? {
        mediaController?.let { return it }
        val future = controllerFuture ?: run {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            MediaController.Builder(context, sessionToken).buildAsync().also { controllerFuture = it }
        }
        return withContext(Dispatchers.IO) {
            try {
                future.get()
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun loadArtworkBytes(url: String): ByteArray? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(400, 400)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            val drawable = result.drawable as? BitmapDrawable ?: return@withContext null
            val stream = ByteArrayOutputStream()
            drawable.bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    fun play(
        song: Song,
        streamInfo: AudioStreamInfo? = null,
        isOfflineCache: Boolean = false
    ) {
        val streamUrl = song.streamUrl ?: return
        scope.launch {
            val controller = getOrCreateController() ?: return@launch
            val artworkBytes = loadArtworkBytes(song.thumbnailUrl)

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(Uri.parse(song.thumbnailUrl))
                .apply {
                    if (artworkBytes != null) {
                        setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    }
                }
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(song.videoId)
                .setUri(streamUrl)
                .setCustomCacheKey(song.videoId)
                .setMediaMetadata(mediaMetadata)
                .build()

            controller.setMediaItem(mediaItem)
            controller.playbackParameters = PlaybackParameters(_playerState.value.playbackSpeed)
            if (_playerState.value.repeatMode == RepeatMode.ONE) {
                controller.repeatMode = Player.REPEAT_MODE_ONE
            }
            controller.prepare()
            controller.play()

            _playerState.update {
                it.copy(
                    currentSong = song,
                    isPlaying = true,
                    currentPositionMs = 0L,
                    durationMs = song.durationMs,
                    streamInfo = streamInfo,
                    isOfflineCache = isOfflineCache
                )
            }
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_ENDED) {
                controller.seekTo(0)
            }
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun setQueue(songs: List<Song>) {
        originalQueue = songs
        if (_playerState.value.isShuffleEnabled) {
            val current = _playerState.value.currentSong
            val shuffled = if (current != null) {
                val remaining = songs.filter { it.videoId != current.videoId }.shuffled()
                listOf(current) + remaining
            } else {
                songs.shuffled()
            }
            _playerState.update { it.copy(queue = shuffled) }
        } else {
            _playerState.update { it.copy(queue = songs) }
        }
    }

    fun toggleShuffle() {
        val currentState = _playerState.value
        val willBeShuffled = !currentState.isShuffleEnabled
        if (willBeShuffled) {
            if (originalQueue.isEmpty() || originalQueue.size != currentState.queue.size) {
                originalQueue = currentState.queue
            }
            val current = currentState.currentSong
            val shuffled = if (current != null) {
                val remaining = originalQueue.filter { it.videoId != current.videoId }.shuffled()
                listOf(current) + remaining
            } else {
                originalQueue.shuffled()
            }
            _playerState.update { it.copy(isShuffleEnabled = true, queue = shuffled) }
        } else {
            val restored = if (originalQueue.isNotEmpty()) originalQueue else currentState.queue
            _playerState.update { it.copy(isShuffleEnabled = false, queue = restored) }
        }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playerState.update { it.copy(repeatMode = nextMode) }
        mediaController?.let { controller ->
            when (nextMode) {
                RepeatMode.ONE -> controller.repeatMode = Player.REPEAT_MODE_ONE
                RepeatMode.ALL -> controller.repeatMode = Player.REPEAT_MODE_OFF
                RepeatMode.OFF -> controller.repeatMode = Player.REPEAT_MODE_OFF
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.25f, 3.0f)
        _playerState.update { it.copy(playbackSpeed = clampedSpeed) }
        mediaController?.playbackParameters = PlaybackParameters(clampedSpeed)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        _playerState.update { state ->
            val list = state.queue.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            state.copy(queue = list)
        }
    }

    fun removeQueueItem(index: Int) {
        _playerState.update { state ->
            val list = state.queue.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            state.copy(queue = list)
        }
    }

    fun clearQueue() {
        _playerState.update { state ->
            val current = state.currentSong
            state.copy(queue = if (current != null) listOf(current) else emptyList())
        }
    }

    fun playNext() {
        val state = _playerState.value
        val currentQueue = state.queue
        val currentSong = state.currentSong ?: return

        if (state.repeatMode == RepeatMode.ONE) {
            seekTo(0)
            mediaController?.play()
            return
        }

        val currentIndex = currentQueue.indexOfFirst { it.videoId == currentSong.videoId }
        if (currentIndex != -1 && currentIndex + 1 < currentQueue.size) {
            val nextSong = currentQueue[currentIndex + 1]
            play(nextSong)
        } else if (state.repeatMode == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            val firstSong = currentQueue.first()
            play(firstSong)
        }
    }

    fun playPrevious() {
        val state = _playerState.value
        val currentQueue = state.queue
        val currentSong = state.currentSong ?: return

        if (state.currentPositionMs > 3000L) {
            seekTo(0)
            return
        }

        val currentIndex = currentQueue.indexOfFirst { it.videoId == currentSong.videoId }
        if (currentIndex > 0) {
            val prevSong = currentQueue[currentIndex - 1]
            play(prevSong)
        } else if (state.repeatMode == RepeatMode.ALL && currentQueue.isNotEmpty()) {
            val lastSong = currentQueue.last()
            play(lastSong)
        } else {
            mediaController?.seekTo(0)
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun setVolume(volume: Float) {
        mediaController?.volume = volume.coerceIn(0f, 1f)
    }

    fun getVolume(): Float {
        return mediaController?.volume ?: 1f
    }

    fun release() {
        progressJob?.cancel()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }
}
