package com.openmusic.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.openmusic.app.data.local.dao.PlaylistDao
import com.openmusic.app.data.local.dao.SongDao
import com.openmusic.app.data.local.entities.PlaylistEntity
import com.openmusic.app.data.local.entities.PlaylistSongCrossRef
import com.openmusic.app.data.local.entities.SongEntity

/**
 * Main Room Database configuration for OpenMusic.
 * Registers SongEntity, PlaylistEntity, and PlaylistSongCrossRef.
 */
@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        com.openmusic.app.data.local.entities.LyricsEntity::class,
        com.openmusic.app.data.local.entities.SearchHistoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricsDao(): com.openmusic.app.data.local.dao.LyricsDao
    abstract fun searchHistoryDao(): com.openmusic.app.data.local.dao.SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "open_music_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
