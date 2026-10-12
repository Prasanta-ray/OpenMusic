package com.openmusic.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NextResponseDto(
    val contents: NextContentsDto? = null
)

@Serializable
data class NextContentsDto(
    val singleColumnMusicWatchNextResultsRenderer: SingleColumnMusicWatchNextResultsRendererDto? = null
)

@Serializable
data class SingleColumnMusicWatchNextResultsRendererDto(
    val tabbedRenderer: WatchNextTabbedRendererDto? = null
)

@Serializable
data class WatchNextTabbedRendererDto(
    val watchNextTabbedResultsRenderer: WatchNextTabbedResultsRendererDto? = null
)

@Serializable
data class WatchNextTabbedResultsRendererDto(
    val tabs: List<WatchNextTabDto> = emptyList()
)

@Serializable
data class WatchNextTabDto(
    val tabRenderer: WatchNextTabRendererDto? = null
)

@Serializable
data class WatchNextTabRendererDto(
    val content: WatchNextTabContentDto? = null
)

@Serializable
data class WatchNextTabContentDto(
    val musicQueueRenderer: MusicQueueRendererDto? = null
)

@Serializable
data class MusicQueueRendererDto(
    val content: MusicQueueContentDto? = null
)

@Serializable
data class MusicQueueContentDto(
    val playlistPanelRenderer: PlaylistPanelRendererDto? = null
)

@Serializable
data class PlaylistPanelRendererDto(
    val contents: List<PlaylistPanelItemDto> = emptyList(),
    val playlistId: String? = null
)

@Serializable
data class PlaylistPanelItemDto(
    val playlistPanelVideoRenderer: PlaylistPanelVideoRendererDto? = null
)

@Serializable
data class PlaylistPanelVideoRendererDto(
    val videoId: String? = null,
    val title: RunsContainerDto? = null,
    val shortBylineText: RunsContainerDto? = null,
    val lengthText: RunsContainerDto? = null,
    val thumbnail: ThumbnailContainerDto? = null
)
