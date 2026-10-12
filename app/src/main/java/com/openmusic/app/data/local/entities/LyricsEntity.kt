package com.openmusic.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted lyrics cache to prevent repeat network queries on low-power hardware.
 */
@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey
    val videoId: String,
    val plainLyrics: String?,
    val syncedLyrics: String?,
    val syncOffsetMs: Long = 0L,
    val fetchedTimestamp: Long = System.currentTimeMillis()
)
