package com.openmusic.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StreamResponseDto(
    val videoDetails: VideoDetailsDto? = null,
    val streamingData: StreamingDataDto? = null
)

@Serializable
data class VideoDetailsDto(
    val videoId: String? = null,
    val title: String? = null,
    val lengthSeconds: String? = null,
    val author: String? = null,
    val channelId: String? = null,
    val thumbnail: ThumbnailContainerDto? = null
)

@Serializable
data class ThumbnailContainerDto(
    val thumbnails: List<ThumbnailDto> = emptyList()
)

@Serializable
data class ThumbnailDto(
    val url: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class StreamingDataDto(
    val expiresInSeconds: String? = null,
    val formats: List<FormatDto> = emptyList(),
    val adaptiveFormats: List<FormatDto> = emptyList()
)

@Serializable
data class FormatDto(
    val itag: Int,
    val mimeType: String,
    val bitrate: Long? = null,
    val averageBitrate: Long? = null,
    val contentLength: String? = null,
    val audioQuality: String? = null,
    val audioSampleRate: String? = null,
    val url: String? = null,
    val signatureCipher: String? = null
)
