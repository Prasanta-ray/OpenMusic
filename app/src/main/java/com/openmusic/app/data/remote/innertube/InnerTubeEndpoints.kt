package com.openmusic.app.data.remote.innertube

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object InnerTubeEndpoints {

    const val MUSIC_API_BASE_URL = "https://music.youtube.com/youtubei/v1"
    const val YOUTUBE_API_BASE_URL = "https://www.youtube.com/youtubei/v1"

    const val SEARCH_ENDPOINT = "$MUSIC_API_BASE_URL/search"
    const val NEXT_ENDPOINT = "$MUSIC_API_BASE_URL/next"
    const val PLAYER_ENDPOINT = "$YOUTUBE_API_BASE_URL/player"

    // Target audio stream format tags
    const val ITAG_OPUS_160KBPS = 251 // Opus ~160kbps (optimal fidelity/bandwidth for Snapdragon 675)
    const val ITAG_OPUS_70KBPS = 250  // Opus ~70kbps (data saver stream)
    const val ITAG_OPUS_50KBPS = 249  // Opus ~50kbps (ultra data saver stream)
    const val ITAG_AAC_128KBPS = 140  // AAC ~128kbps (standard fallback)
    const val ITAG_AAC_48KBPS = 139   // AAC ~48kbps (data saver fallback)

    // User Agents & Headers
    const val USER_AGENT_WEB_REMIX = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
    const val USER_AGENT_ANDROID = "com.google.android.youtube/21.26.364 (Linux; U; Android 11) gzip"
    const val CLIENT_NAME_ANDROID = "3"
    const val CLIENT_VERSION_ANDROID = "21.26.364"
    const val REFERER_MUSIC = "https://music.youtube.com/"

    /**
     * Client context for YouTube Music Web Remix (used for search and next/radio queue).
     */
    fun createWebRemixContext(): JsonObject = buildJsonObject {
        putJsonObject("client") {
            put("clientName", "WEB_REMIX")
            put("clientVersion", "1.20241021.01.00")
            put("hl", "en")
            put("gl", "US")
            put("utcOffsetMinutes", 0)
        }
    }

    /**
     * Client context using ANDROID for /player to retrieve direct unthrottled streams.
     */
    fun createAndroidContext(visitorData: String? = null): JsonObject = buildJsonObject {
        putJsonObject("client") {
            put("clientName", "ANDROID")
            put("clientVersion", CLIENT_VERSION_ANDROID)
            put("androidSdkVersion", 30)
            put("userAgent", USER_AGENT_ANDROID)
            put("osName", "Android")
            put("osVersion", "11")
            put("hl", "en")
            put("gl", "US")
            if (!visitorData.isNullOrBlank()) {
                put("visitorData", visitorData)
            }
        }
    }
}
