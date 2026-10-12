package com.openmusic.app.data.remote.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun parse_validSyncedLyrics_returnsSortedLines() {
        val lrc = """
            [ti:Sparks]
            [ar:Coldplay]
            [00:32.43]Did I drive you away?
            [00:38.17]I know what you'll say
            [00:41.32]You say, "Oh, sing one you know"
            [01:06.95]Yeah, that's what I'll do
        """.trimIndent()

        val result = LrcParser.parse("test_id", lrc, null)

        assertTrue(result.isSynced)
        assertEquals(4, result.lines.size)

        // Line 0
        assertEquals(32430L, result.lines[0].timestampMs)
        assertEquals("Did I drive you away?", result.lines[0].text)

        // Line 1
        assertEquals(38170L, result.lines[1].timestampMs)
        assertEquals("I know what you'll say", result.lines[1].text)

        // Line 2
        assertEquals(41320L, result.lines[2].timestampMs)
        assertEquals("You say, \"Oh, sing one you know\"", result.lines[2].text)

        // Line 3
        assertEquals(66950L, result.lines[3].timestampMs)
        assertEquals("Yeah, that's what I'll do", result.lines[3].text)
    }

    @Test
    fun parse_emptySyncedLyrics_returnsUnsyncedWithPlainFallback() {
        val plain = "Plain lyrics without timestamps"
        val result = LrcParser.parse("test_id", null, plain)

        assertFalse(result.isSynced)
        assertTrue(result.lines.isEmpty())
        assertEquals(plain, result.plainLyrics)
    }
}
