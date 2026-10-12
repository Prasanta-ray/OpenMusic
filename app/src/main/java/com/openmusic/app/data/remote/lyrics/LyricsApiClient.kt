package com.openmusic.app.data.remote.lyrics

import com.openmusic.app.data.remote.dto.LrcLibResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class LyricsApiClient {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    suspend fun getLyrics(trackName: String, artistName: String, durationSeconds: Int? = null): LrcLibResponseDto? = runCatching {
        val cleanedTrack = cleanTrackName(trackName)
        val cleanedArtist = cleanArtistName(artistName)

        if (cleanedArtist.isNotBlank()) {
            val response = client.get("https://lrclib.net/api/get") {
                header("User-Agent", "OpenMusic/1.0 (Android; Open Source)")
                parameter("track_name", cleanedTrack)
                parameter("artist_name", cleanedArtist)
                if (durationSeconds != null && durationSeconds > 0) {
                    parameter("duration", durationSeconds)
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<LrcLibResponseDto>()
                if (dto.syncedLyrics != null || dto.plainLyrics != null) {
                    return@runCatching dto
                }
            }
        }

        // Fallback to search endpoint
        val query = if (cleanedArtist.isNotBlank()) "$cleanedTrack $cleanedArtist" else cleanedTrack
        searchLyrics(query, durationSeconds)
    }.getOrNull()

    suspend fun searchLyrics(query: String, durationSeconds: Int? = null): LrcLibResponseDto? = runCatching {
        val response = client.get("https://lrclib.net/api/search") {
            header("User-Agent", "OpenMusic/1.0 (Android; Open Source)")
            parameter("q", query.trim())
        }

        if (response.status == HttpStatusCode.OK) {
            val list = response.body<List<LrcLibResponseDto>>()
            val candidates = list.filter { it.syncedLyrics != null || it.plainLyrics != null }
            if (durationSeconds != null && durationSeconds > 0) {
                candidates.minByOrNull { kotlin.math.abs((it.duration ?: 0.0) - durationSeconds) }
                    ?: candidates.firstOrNull()
            } else {
                candidates.firstOrNull()
            }
        } else {
            null
        }
    }.getOrNull()

    private fun cleanTrackName(title: String): String {
        return title
            .replace(Regex("""^@[\w\d_.-]+\s*-\s*"""), "")
            .replace(Regex("""\((?:official|music|lyric|audio|video|visualizer|remastered)[^)]*\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[(?:official|music|lyric|audio|video|visualizer|remastered)[^\]]*\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""(?:feat\.|ft\.).*""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun cleanArtistName(artist: String): String {
        val trimmed = artist
            .replace(Regex("""^@"""), "")
            .replace(Regex("""\s*-\s*Topic$"""), "")
            .trim()
        return if (trimmed.equals("Song", ignoreCase = true) ||
            trimmed.equals("Video", ignoreCase = true) ||
            trimmed.equals("Episode", ignoreCase = true) ||
            trimmed.equals("Unknown Artist", ignoreCase = true)
        ) "" else trimmed
    }
}
