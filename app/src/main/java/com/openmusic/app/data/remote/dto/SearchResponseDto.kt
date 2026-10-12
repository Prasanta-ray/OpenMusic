package com.openmusic.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val contents: SearchContentsDto? = null
)

@Serializable
data class SearchContentsDto(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRendererDto? = null
)

@Serializable
data class TabbedSearchResultsRendererDto(
    val tabs: List<TabDto> = emptyList()
)

@Serializable
data class TabDto(
    val tabRenderer: TabRendererDto? = null
)

@Serializable
data class TabRendererDto(
    val content: TabContentDto? = null
)

@Serializable
data class TabContentDto(
    val sectionListRenderer: SectionListRendererDto? = null
)

@Serializable
data class SectionListRendererDto(
    val contents: List<SectionContentDto> = emptyList()
)

@Serializable
data class SectionContentDto(
    val musicCardShelfRenderer: MusicCardShelfRendererDto? = null,
    val musicShelfRenderer: MusicShelfRendererDto? = null,
    val itemSectionRenderer: ItemSectionRendererDto? = null
)

@Serializable
data class MusicCardShelfRendererDto(
    val title: RunsContainerDto? = null,
    val subtitle: RunsContainerDto? = null,
    val thumbnail: MusicThumbnailContainerDto? = null,
    val onTap: NavigationEndpointDto? = null
)

@Serializable
data class ItemSectionRendererDto(
    val contents: List<MusicShelfItemDto> = emptyList()
)

@Serializable
data class MusicShelfRendererDto(
    val title: RunsContainerDto? = null,
    val contents: List<MusicShelfItemDto> = emptyList()
)

@Serializable
data class MusicShelfItemDto(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRendererDto? = null
)

@Serializable
data class MusicResponsiveListItemRendererDto(
    val playlistItemData: PlaylistItemDataDto? = null,
    val flexColumns: List<FlexColumnDto> = emptyList(),
    val thumbnail: MusicThumbnailContainerDto? = null,
    val navigationEndpoint: NavigationEndpointDto? = null
)

@Serializable
data class PlaylistItemDataDto(
    val videoId: String? = null
)

@Serializable
data class FlexColumnDto(
    val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRendererDto? = null
)

@Serializable
data class MusicResponsiveListItemFlexColumnRendererDto(
    val text: RunsContainerDto? = null
)

@Serializable
data class RunsContainerDto(
    val runs: List<RunDto> = emptyList()
)

@Serializable
data class RunDto(
    val text: String,
    val navigationEndpoint: NavigationEndpointDto? = null
)

@Serializable
data class MusicThumbnailContainerDto(
    val musicThumbnailRenderer: MusicThumbnailRendererDto? = null
)

@Serializable
data class MusicThumbnailRendererDto(
    val thumbnail: ThumbnailContainerDto? = null
)

@Serializable
data class NavigationEndpointDto(
    val watchEndpoint: WatchEndpointDto? = null
)

@Serializable
data class WatchEndpointDto(
    val videoId: String? = null,
    val playlistId: String? = null
)
