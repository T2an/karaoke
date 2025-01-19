package fr.enssat.singwithme.heyrendt_quintin.util

import fr.enssat.singwithme.heyrendt_quintin.data.LyricSegment
import fr.enssat.singwithme.heyrendt_quintin.data.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SongUtilTest {

    @Test
    fun testParseSong() {
        val song: String = """
            SingWithMe
            # title
            Creep
            # author
            Radiohead
            # soundtrack
            creep.mp3
            # lyrics
            { 0:19 }When you were here before,
            { 0:23 }Couldn't look you in the eye{ 0:25 }
            { 0:28 }You're just like an angel,{ 0:31 }
            { 0:33 }Your skin makes me cry{ 0:36 }

            { 0:39 }You float like a { 0:41 }feather{ 0:43 }
            { 0:44 }In a beautiful world{ 0:47 }
            { 0:49 }I wish I was special{ 0:53 }
            { 0:54 }You're so fuckin' special{ 0:57 }

            { 1:00 }But I'm a creep,{ 1:02 }
            { 1:05 }I'm a weird{ 1:07 }o{ 1:09 }
            { 1:10 }What the hell am I doin' { 1:12 }here ?{ 1:14 }
            { 1:16 }I don't belong here{ 1:17 }
        """.trimIndent()

        val songUtil = SongUtil("Creep/Creep.md")
        val songParsed: Song = songUtil.parseSong(song)

        assertEquals("Creep", songParsed.title)
        assertEquals("Radiohead", songParsed.author)
        assertEquals("creep.mp3", songParsed.soundtrack)

        val lyricSegments: List<List<LyricSegment>> = songParsed.lyricSegments
        assertTrue(lyricSegments.isNotEmpty())

        val firstSegment: LyricSegment = lyricSegments[0].first()
        assertEquals(19.0f, firstSegment.startTime)
        assertEquals("When you were here before,", firstSegment.text)
        assertEquals(4.0f, firstSegment.duration)

        val lastSegment: LyricSegment = lyricSegments.last().last()
        assertEquals(76.0f, lastSegment.startTime)
        assertEquals("I don't belong here", lastSegment.text)
        assertEquals(1.0f, lastSegment.duration)
    }

    @Test
    fun testNoLyrics() {
        val song: String = """
            SingWithMe
            # title
            Creep
            # author
            Radiohead
            # soundtrack
            creep.mp3
            # lyrics
        
        """.trimIndent()

        val songUtil = SongUtil("Creep/Creep.md")
        val songParsed: Song = songUtil.parseSong(song)

        // Test that lyrics are empty when no lyrics exist
        assertTrue(songParsed.lyricSegments.isEmpty())
    }
}