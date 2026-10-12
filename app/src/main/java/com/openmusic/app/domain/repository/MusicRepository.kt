package com.openmusic.app.domain.repository

import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.AudioStreamInfo
import com.openmusic.app.domain.model.SearchFilter
import com.openmusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllDownloadedSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getRecentlyPlayedSongs(limit: Int = 20): Flow<List<Song>>
    fun getAllSongs(): Flow<List<Song>>
    fun getSong(videoId: String): Flow<Song?>

    suspend fun search(query: String, filter: SearchFilter = SearchFilter.SONGS): Result<List<Song>>
    suspend fun getStreamUrl(videoId: String): Result<String>
    suspend fun getOptimalAudioStream(videoId: String, isDataSaver: Boolean = false): Result<AudioStreamInfo>
    suspend fun getOptimalAudioStream(videoId: String, quality: AudioQuality): Result<AudioStreamInfo>
    suspend fun getRelatedTracks(videoId: String): Result<List<Song>>

    suspend fun saveSong(song: Song)
    suspend fun toggleFavorite(song: Song)
    suspend fun updateDownloadStatus(videoId: String, isDownloaded: Boolean, cacheKey: String?)
    suspend fun updateLastPlayed(videoId: String, timestamp: Long = System.currentTimeMillis())
    suspend fun deleteSong(videoId: String)
    suspend fun isSongCached(videoId: String, cacheKey: String?, durationMs: Long): Boolean

    val downloadProgress: Flow<Map<String, Float>>
    suspend fun downloadTrack(song: Song): Result<Unit>
    suspend fun downloadTrack(song: Song, quality: AudioQuality): Result<Unit>
    suspend fun cancelDownload(videoId: String)
    suspend fun removeDownload(videoId: String, cacheKey: String?)

    // Playlist Management
    fun getAllPlaylists(): Flow<List<com.openmusic.app.data.local.entities.PlaylistEntity>>
    fun getPlaylistWithSongs(playlistId: Long): Flow<com.openmusic.app.data.local.entities.PlaylistWithSongs?>
    suspend fun createPlaylist(title: String): Long
    suspend fun addSongToPlaylist(playlistId: Long, song: Song)
    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String)
    suspend fun deletePlaylist(playlistId: Long)

    // Lyrics
    suspend fun getLyrics(song: Song): Result<com.openmusic.app.domain.model.SongLyrics>
    suspend fun updateLyricsSyncOffset(videoId: String, offsetMs: Long)

    // Search History & Suggestions
    fun getSearchHistory(): Flow<List<String>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearSearchHistory()
    suspend fun getSearchSuggestions(query: String): Result<List<String>>

    // Artist & Album Details
    suspend fun getArtistDetail(artistName: String): Result<com.openmusic.app.domain.model.ArtistDetail>
    suspend fun getAlbumDetail(albumName: String, artist: String): Result<com.openmusic.app.domain.model.Album>

    /**
     * Cache & Storage Management
     */
    fun getTotalCacheBytes(): Long
    fun clearDiskCache(): Long

    /**
     * Backward-compatible alias for [getAllDownloadedSongs].
     */
    fun getCachedSongs(): Flow<List<Song>> = getAllDownloadedSongs()
}
