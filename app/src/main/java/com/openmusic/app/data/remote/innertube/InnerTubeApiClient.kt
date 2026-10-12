package com.openmusic.app.data.remote.innertube

import com.openmusic.app.data.remote.dto.MusicShelfItemDto
import com.openmusic.app.data.remote.dto.NextResponseDto
import com.openmusic.app.data.remote.dto.SearchResponseDto
import com.openmusic.app.data.remote.dto.StreamResponseDto
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.domain.model.AudioStreamInfo
import com.openmusic.app.domain.model.SearchFilter
import com.openmusic.app.domain.model.Song
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * YouTube InnerTube API client featuring a dual-client strategy:
 * - Uses WEB_REMIX for rich catalog search and continuous radio queue generation.
 * - Uses ANDROID_VR client context for /player to retrieve direct unthrottled audio streams without JS signature ciphers.
 */
class InnerTubeApiClient {

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

    /**
     * Searches YouTube Music catalog. Defaults strictly to [SearchFilter.SONGS].
     */
    suspend fun search(
        query: String,
        filter: SearchFilter = SearchFilter.SONGS
    ): Result<List<Song>> = runCatching {
        val requestBody = buildJsonObject {
            put("context", InnerTubeEndpoints.createWebRemixContext())
            put("query", query)
            if (filter.param != null) {
                put("params", filter.param)
            }
        }

        val response = client.post(InnerTubeEndpoints.SEARCH_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("User-Agent", InnerTubeEndpoints.USER_AGENT_WEB_REMIX)
            header("Referer", InnerTubeEndpoints.REFERER_MUSIC)
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20241021.01.00")
            setBody(requestBody.toString())
        }.body<SearchResponseDto>()

        parseSearchResponse(response)
    }

    private var visitorData: String = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"

    /**
     * Fetches stream metadata using ANDROID client context to retrieve direct unthrottled streams.
     */
    suspend fun getStreamInfo(videoId: String): Result<StreamResponseDto> = runCatching {
        val requestBody = buildJsonObject {
            put("context", InnerTubeEndpoints.createAndroidContext(visitorData))
            put("videoId", videoId)
            put("racyCheckOk", true)
            put("contentCheckOk", true)
        }

        client.post(InnerTubeEndpoints.PLAYER_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("User-Agent", InnerTubeEndpoints.USER_AGENT_ANDROID)
            header("X-YouTube-Client-Name", InnerTubeEndpoints.CLIENT_NAME_ANDROID)
            header("X-YouTube-Client-Version", InnerTubeEndpoints.CLIENT_VERSION_ANDROID)
            setBody(requestBody.toString())
        }.body<StreamResponseDto>()
    }

    /**
     * Resolves the optimal audio stream based on granular [AudioQuality].
     * Targets:
     * - HIGH: itag 251 (Opus ~160kbps) or itag 140 (AAC ~128kbps) for studio fidelity.
     * - STANDARD: itag 140 (AAC ~128kbps) or itag 251 (Opus ~160kbps).
     * - LOW: itag 249 (Opus ~50kbps), itag 250 (Opus ~70kbps), or itag 139 (AAC ~48kbps) for cellular data conservation.
     */
    suspend fun getOptimalAudioStream(
        videoId: String,
        quality: AudioQuality = AudioQuality.HIGH
    ): Result<AudioStreamInfo> = runCatching {
        val response = getStreamInfo(videoId).getOrThrow()
        val streamingData = response.streamingData
            ?: throw IllegalStateException("No streaming data available for video: $videoId")

        val audioFormats = streamingData.adaptiveFormats
            .filter { it.mimeType.startsWith("audio/") && !it.url.isNullOrBlank() }

        val optimalFormat = when (quality) {
            AudioQuality.HIGH -> {
                audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_OPUS_160KBPS }
                    ?: audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_AAC_128KBPS }
                    ?: audioFormats.maxByOrNull { it.bitrate ?: 0L }
            }
            AudioQuality.STANDARD -> {
                audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_AAC_128KBPS }
                    ?: audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_OPUS_160KBPS }
                    ?: audioFormats.firstOrNull { (it.bitrate ?: 0L) in 100_000L..150_000L }
                    ?: audioFormats.firstOrNull()
            }
            AudioQuality.LOW -> {
                audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_OPUS_50KBPS }
                    ?: audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_OPUS_70KBPS }
                    ?: audioFormats.firstOrNull { it.itag == InnerTubeEndpoints.ITAG_AAC_48KBPS }
                    ?: audioFormats.minByOrNull { it.bitrate ?: Long.MAX_VALUE }
            }
        } ?: streamingData.formats.firstOrNull { !it.url.isNullOrBlank() }
          ?: throw IllegalStateException("No valid direct audio stream found for $videoId")

        val fallbackBitrate = when (quality) {
            AudioQuality.LOW -> 50_000L
            AudioQuality.STANDARD -> 128_000L
            AudioQuality.HIGH -> 160_000L
        }

        AudioStreamInfo(
            videoId = videoId,
            streamUrl = optimalFormat.url!!,
            mimeType = optimalFormat.mimeType,
            bitrate = optimalFormat.bitrate ?: fallbackBitrate,
            itag = optimalFormat.itag,
            contentLength = optimalFormat.contentLength?.toLongOrNull(),
            expiresInSeconds = streamingData.expiresInSeconds?.toLongOrNull()
        )
    }

    suspend fun getOptimalAudioStream(
        videoId: String,
        isDataSaver: Boolean
    ): Result<AudioStreamInfo> = getOptimalAudioStream(
        videoId = videoId,
        quality = if (isDataSaver) AudioQuality.LOW else AudioQuality.HIGH
    )

    /**
     * Fetches an automated 10-20 track radio/autoplay queue via the /next endpoint.
     */
    suspend fun getNextQueue(
        videoId: String,
        playlistId: String? = null
    ): Result<List<Song>> = runCatching {
        val radioPlaylistId = playlistId ?: "RDAMVM$videoId"
        val requestBody = buildJsonObject {
            put("context", InnerTubeEndpoints.createWebRemixContext())
            put("videoId", videoId)
            put("playlistId", radioPlaylistId)
            put("isAudioOnly", true)
        }

        val response = client.post(InnerTubeEndpoints.NEXT_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("User-Agent", InnerTubeEndpoints.USER_AGENT_WEB_REMIX)
            header("Referer", InnerTubeEndpoints.REFERER_MUSIC)
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20241021.01.00")
            setBody(requestBody.toString())
        }.body<NextResponseDto>()

        parseNextResponse(response)
    }

    private fun parseSearchResponse(dto: SearchResponseDto): List<Song> {
        val songs = mutableListOf<Song>()
        val tabs = dto.contents?.tabbedSearchResultsRenderer?.tabs ?: return emptyList()

        for (tab in tabs) {
            val sections = tab.tabRenderer?.content?.sectionListRenderer?.contents ?: continue
            for (section in sections) {
                // 1. Top card result
                section.musicCardShelfRenderer?.let { card ->
                    val videoId = card.onTap?.watchEndpoint?.videoId
                        ?: card.title?.runs?.firstOrNull()?.navigationEndpoint?.watchEndpoint?.videoId
                    if (!videoId.isNullOrBlank()) {
                        val title = card.title?.runs?.joinToString("") { it.text } ?: "Unknown Title"
                        val artist = card.subtitle?.runs?.firstOrNull()?.text ?: "Unknown Artist"
                        val thumbnailUrl = card.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
                            ?.maxByOrNull { it.width ?: 0 }?.url ?: ""
                        songs.add(
                            Song(
                                videoId = videoId,
                                title = title,
                                artist = artist,
                                thumbnailUrl = thumbnailUrl
                            )
                        )
                    }
                }

                // 2. Shelf items
                section.musicShelfRenderer?.contents?.let { items ->
                    parseShelfItems(items, songs)
                }

                // 3. ItemSection items
                section.itemSectionRenderer?.contents?.let { items ->
                    parseShelfItems(items, songs)
                }
            }
        }
        return songs.distinctBy { it.videoId }
    }

    private fun parseShelfItems(items: List<MusicShelfItemDto>, destination: MutableList<Song>) {
        for (item in items) {
            val renderer = item.musicResponsiveListItemRenderer ?: continue
            val videoId = renderer.playlistItemData?.videoId
                ?: renderer.navigationEndpoint?.watchEndpoint?.videoId
                ?: continue

            val title = renderer.flexColumns.getOrNull(0)
                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                ?.joinToString("") { it.text } ?: "Unknown Title"

            val subtitleRuns = renderer.flexColumns.getOrNull(1)
                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
            val filteredRuns = subtitleRuns.map { it.text.trim() }
                .filter { it.isNotBlank() && it != "•" && it != "Song" && it != "Video" && it != "Episode" && !it.matches(Regex("""\d+:\d+""")) }
            val artist = filteredRuns.firstOrNull() ?: subtitleRuns.firstOrNull()?.text ?: "Unknown Artist"

            val durationStr = subtitleRuns.lastOrNull()?.text
            val durationMs = parseDurationMs(durationStr)

            val thumbnailUrl = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
                ?.maxByOrNull { it.width ?: 0 }?.url ?: ""

            destination.add(
                Song(
                    videoId = videoId,
                    title = title,
                    artist = artist,
                    thumbnailUrl = thumbnailUrl,
                    durationMs = durationMs
                )
            )
        }
    }

    private fun parseNextResponse(dto: NextResponseDto): List<Song> {
        val songs = mutableListOf<Song>()
        val tabs = dto.contents?.singleColumnMusicWatchNextResultsRenderer
            ?.tabbedRenderer?.watchNextTabbedResultsRenderer?.tabs ?: return emptyList()

        for (tab in tabs) {
            val panel = tab.tabRenderer?.content?.musicQueueRenderer?.content?.playlistPanelRenderer ?: continue
            for (item in panel.contents) {
                val renderer = item.playlistPanelVideoRenderer ?: continue
                val videoId = renderer.videoId ?: continue

                val title = renderer.title?.runs?.joinToString("") { it.text } ?: "Unknown Title"
                val artist = renderer.shortBylineText?.runs?.joinToString("") { it.text } ?: "Unknown Artist"
                val durationMs = parseDurationMs(renderer.lengthText?.runs?.firstOrNull()?.text)
                val thumbnailUrl = renderer.thumbnail?.thumbnails?.maxByOrNull { it.width ?: 0 }?.url ?: ""

                songs.add(
                    Song(
                        videoId = videoId,
                        title = title,
                        artist = artist,
                        thumbnailUrl = thumbnailUrl,
                        durationMs = durationMs
                    )
                )
            }
        }
        return songs.distinctBy { it.videoId }
    }

    private fun parseDurationMs(durationStr: String?): Long {
        if (durationStr.isNullOrBlank()) return 0L
        val parts = durationStr.split(":").mapNotNull { it.trim().toLongOrNull() }
        return when (parts.size) {
            2 -> (parts[0] * 60 + parts[1]) * 1000L
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000L
            else -> 0L
        }
    }

    /**
     * Retrieves autocomplete search suggestions using high-speed suggestqueries endpoint.
     */
    suspend fun getSearchSuggestions(query: String): Result<List<String>> = runCatching {
        if (query.isBlank()) return@runCatching emptyList()
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val url = "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=$encoded"
        val response = client.get(url).body<String>()
        val jsonElement = jsonConfig.parseToJsonElement(response)
        val array = jsonElement.jsonArray
        if (array.size > 1) {
            array[1].jsonArray.map { it.jsonPrimitive.content }
        } else {
            emptyList()
        }
    }

    fun close() {
        client.close()
    }
}
