package com.openmusic.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an audio track stored in the local library.
 * Includes composite indices on [isDownloaded] and [lastPlayedTimestamp] to ensure
 * zero table scans during library filtering on constrained hardware (Snapdragon 675).
 */
@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["isDownloaded"]),
        Index(value = ["lastPlayedTimestamp"]),
        Index(value = ["isFavorite"])
    ]
)
data class SongEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationMs: Long,
    val streamUrl: String?,
    val cacheKey: String?,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0L
)
