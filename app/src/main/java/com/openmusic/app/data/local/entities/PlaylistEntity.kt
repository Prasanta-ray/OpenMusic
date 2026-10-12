package com.openmusic.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user-curated playlist.
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null
)
