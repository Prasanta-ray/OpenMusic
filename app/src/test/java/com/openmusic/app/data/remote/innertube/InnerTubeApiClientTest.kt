package com.openmusic.app.data.remote.innertube

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InnerTubeApiClientTest {

    private lateinit var apiClient: InnerTubeApiClient

    @Before
    fun setUp() {
        apiClient = InnerTubeApiClient()
    }

    @After
    fun tearDown() {
        apiClient.close()
    }

    @Test
    fun testGetStreamInfo_resolvesValidAudioStream() = runTest {
        // Test with public YouTube music video ID
        val testVideoId = "dQw4w9WgXcQ"
        val result = apiClient.getOptimalAudioStream(testVideoId)

        assertTrue("Expected successful stream extraction: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val streamInfo = result.getOrThrow()
        assertNotNull(streamInfo.streamUrl)
        assertTrue(streamInfo.streamUrl.startsWith("http"))
        assertTrue("Expected audio or mp4 container, got: ${streamInfo.mimeType}", streamInfo.mimeType.startsWith("audio/") || streamInfo.mimeType.contains("mp4"))
        assertTrue("Expected valid stream itag (251, 140, or 18), got: ${streamInfo.itag}", streamInfo.itag in listOf(251, 140, 18))
    }

    @Test
    fun testGetStreamInfo_resolvesDataSaverAudioStream() = runTest {
        val testVideoId = "dQw4w9WgXcQ"
        val result = apiClient.getOptimalAudioStream(testVideoId, isDataSaver = true)

        assertTrue("Expected successful data saver stream: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val streamInfo = result.getOrThrow()
        assertNotNull(streamInfo.streamUrl)
        assertTrue(streamInfo.streamUrl.startsWith("http"))
        assertTrue("Bitrate should be positive", streamInfo.bitrate > 0)
        assertTrue("Codec name must not be blank", streamInfo.codecName.isNotBlank())
    }

    @Test
    fun testSearch_returnsMusicTracks() = runTest {
        val result = apiClient.search("Never Gonna Give You Up")
        assertTrue("Search should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val songs = result.getOrThrow()
        assertTrue("Expected search results to be non-empty", songs.isNotEmpty())
        val first = songs.first()
        assertTrue("First song must have a non-empty videoId", first.videoId.isNotBlank())
        assertTrue("First song must have a title", first.title.isNotBlank())
    }
}
