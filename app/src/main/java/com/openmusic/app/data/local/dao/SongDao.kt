package com.openmusic.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openmusic.app.data.local.entities.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local songs and cached library items.
 * Queries leverage indices on isDownloaded and lastPlayedTimestamp.
 */
@Dao
interface SongDao {

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY addedTimestamp DESC")
    fun getAllDownloadedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY addedTimestamp DESC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY addedTimestamp DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE videoId = :videoId LIMIT 1")
    fun getSongById(videoId: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE videoId = :videoId LIMIT 1")
    suspend fun getSong(videoId: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isDownloaded = :isDownloaded, cacheKey = :cacheKey WHERE videoId = :videoId")
    suspend fun updateDownloadStatus(videoId: String, isDownloaded: Boolean, cacheKey: String?)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE videoId = :videoId")
    suspend fun updateFavoriteStatus(videoId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET lastPlayedTimestamp = :timestamp WHERE videoId = :videoId")
    suspend fun updatePlaybackTimestamp(videoId: String, timestamp: Long)

    @Query("DELETE FROM songs WHERE videoId = :videoId")
    suspend fun deleteSong(videoId: String)

    @Delete
    suspend fun deleteSong(song: SongEntity)
}
