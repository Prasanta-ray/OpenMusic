package com.openmusic.app.domain.model

data class ArtistDetail(
    val name: String,
    val bannerUrl: String? = null,
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList()
)
