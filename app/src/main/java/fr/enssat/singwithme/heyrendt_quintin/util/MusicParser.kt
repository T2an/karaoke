package fr.enssat.singwithme.heyrendt_quintin.util

import android.util.Log
import fr.enssat.singwithme.heyrendt_quintin.data.Song
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

/**
 * Util class for music parsing
 */
class MusicParser {

    /**
     * Parse a string into a Song object
     */
    suspend fun parseSong(url: String): Song? {
        val client = HttpClient(CIO)
        var response: HttpResponse? = null
        try {
            response = client.get(url)
            Log.i("DEBUG", response.bodyAsText())

        } catch(e: Exception) {
            e.message?.let { Log.i("DEBUG", it) }
        } finally {
            client.close()
        }

        if (response == null) return null

        val lines = response.bodyAsText().lines()
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