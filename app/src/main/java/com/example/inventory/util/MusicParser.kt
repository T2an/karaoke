package com.example.inventory.util

import com.example.inventory.data.Song

/**
 * Util class for music parsing
 */
class MusicParser {

    /**
     * Parse a string into a Song object
     */
    fun parseSong(input: String): Song {
        val lines = input.lines()
        var title = ""
        var author = ""
        var soundtrack = ""
        val lyrics = mutableListOf<String>()

        var isLyricsSection = false
        var i = 0
        while (i < lines.size) {
            when {
                lines[i].startsWith("# title") -> {
                    i++
                    title = lines[i]
                }
                lines[i].startsWith("# author") -> {
                    i++
                    author = lines[i]
                }
                lines[i].startsWith("# soundtrack") -> {
                    i++
                    soundtrack = lines[i]
                }
                lines[i].startsWith("# lyrics") -> isLyricsSection = true
                isLyricsSection -> lyrics.add(lines[i])
            }
            i++
        }

        return Song(title, author, soundtrack, lyrics)
    }
}