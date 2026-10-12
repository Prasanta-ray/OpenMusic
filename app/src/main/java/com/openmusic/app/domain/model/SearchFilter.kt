package com.openmusic.app.domain.model

/**
 * Filter parameters for YouTube Music InnerTube search queries.
 * The default filter is strictly set to [SONGS].
 */
enum class SearchFilter(val param: String?) {
    SONGS("Eg-KAQwIABAAGAEgASgB"),
    ALL(null),
    ALBUMS("Eg-KAQwBAhABGAEgASgB"),
    ARTISTS("Eg-KAQwIAhABGAEgASgB"),
    COMMUNITY_PLAYLISTS("Eg-KAQwEAaAEGAEgASgB")
}
