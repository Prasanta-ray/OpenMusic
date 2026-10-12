package com.openmusic.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Search : Screen("search", "Search")
    data object Library : Screen("library", "Library")
    data object Player : Screen("player", "Now Playing")
    data object Settings : Screen("settings", "Settings")
    data class ArtistDetail(val artistName: String, val artistBrowseId: String? = null) : Screen("artist_detail", artistName)
    data class AlbumDetail(val albumName: String, val albumBrowseId: String? = null, val artist: String = "") : Screen("album_detail", albumName)
    data object AboutCreator : Screen("about_creator", "About Creator")
    data object ContactCreator : Screen("contact_creator", "Contact Creator")
    data object TermsAndConditions : Screen("terms_conditions", "Terms & Conditions")
    data object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy & User Data")
    data object Splash : Screen("splash", "Welcome & Versions")
    data object LegalHub : Screen("legal_hub", "Legal & Creator Hub")
}
