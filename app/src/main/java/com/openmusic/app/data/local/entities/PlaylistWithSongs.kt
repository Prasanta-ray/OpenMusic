package com.openmusic.app.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Relation class fetching a playlist along with its associated tracks without redundant joins.
 */
data class PlaylistWithSongs(
    @Embedded
    val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "videoId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<SongEntity>
)
