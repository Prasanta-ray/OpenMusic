package com.openmusic.app.domain.model

data class Album(
    val browseId: String = "",
    val title: String,
    val artist: String,
    val year: String? = null,
    val thumbnailUrl: String = "",
    val tracks: List<Song> = emptyList()
)
