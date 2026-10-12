package com.openmusic.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openmusic.app.data.local.entities.LyricsEntity

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics WHERE videoId = :videoId LIMIT 1")
    suspend fun getLyrics(videoId: String): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Query("UPDATE lyrics SET syncOffsetMs = :offsetMs WHERE videoId = :videoId")
    suspend fun updateSyncOffset(videoId: String, offsetMs: Long)

    @Query("SELECT syncOffsetMs FROM lyrics WHERE videoId = :videoId")
    suspend fun getSyncOffset(videoId: String): Long?

    @Query("DELETE FROM lyrics WHERE videoId = :videoId")
    suspend fun deleteLyrics(videoId: String)
}
