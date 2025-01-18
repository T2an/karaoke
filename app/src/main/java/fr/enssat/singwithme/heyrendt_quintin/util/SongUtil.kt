package fr.enssat.singwithme.heyrendt_quintin.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.enssat.singwithme.heyrendt_quintin.data.LyricSegment
import fr.enssat.singwithme.heyrendt_quintin.data.Song
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

/**
 * Util class for music parsing
 */
class SongUtil {

    private val moshiBuilder: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @OptIn(ExperimentalStdlibApi::class)
    private val songAdapter: JsonAdapter<Song> = this.moshiBuilder.adapter<Song>()

    suspend fun downloadSong(url: String): String {
        val client = HttpClient(CIO)
        val response: HttpResponse
        try {
            response = client.get(url)
            return response.bodyAsText()
        } catch(e: Exception) {
            throw Exception("Impossible de télécharger la musique pour le moment.")
        } finally {
            client.close()
        }
    }

    /**
     * Parse a string into a Song object
     */
    fun parseSong(song: String): Song {
        val lines = song.lines()
        var title = ""
        var author = ""
        var soundtrack = ""
        val rawLyrics = mutableListOf<String>()

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
                isLyricsSection -> rawLyrics.add(lines[i])
            }
            i++
        }

        val lyricSegments = parseLyrics(rawLyrics)
        val lyrics = lyricSegments.map { t -> t.text }

        return Song(title, author, soundtrack, lyrics, lyricSegments)
    }

    private fun parseLyrics(rawLyrics: List<String>): List<LyricSegment> {
        val regex = Regex("\\{\\s*(\\d+:\\d+(\\.\\d+)?)\\s*\\}([^\\{]*)(\\{\\s*(\\d+:\\d+(\\.\\d+)?)\\s*\\})?")
        // Regex pour extraire timestamps et texte

        val segments = mutableListOf<LyricSegment>()

        val allMatches = rawLyrics.flatMap { regex.findAll(it).toList() } // Trouver toutes les correspondances
        for ((index, match) in allMatches.withIndex()) {
            val startTimestamp = match.groups[1]?.value?.toFloatTime() ?: continue
            val text = match.groups[3]?.value?.trim() ?: ""
            val explicitEndTimestamp = match.groups[5]?.value?.toFloatTime()

            // Déterminer le timestamp de fin
            val endTimestamp = when {
                explicitEndTimestamp != null -> explicitEndTimestamp // Si un timestamp explicite existe
                index + 1 < allMatches.size -> allMatches[index + 1].groups[1]?.value?.toFloatTime() // Début de la ligne suivante
                else -> null // Dernière ligne sans timestamp explicite
            }

            if (endTimestamp != null) {
                val duration = endTimestamp - startTimestamp
                segments.add(LyricSegment(startTime = startTimestamp, text = text, duration = duration))
            } else {
                // Dernière ligne sans ligne suivante, appliquer une durée par défaut
                segments.add(LyricSegment(startTime = startTimestamp, text = text, duration = 2f))
            }
        }

        return segments
    }

    // Fonction pour convertir un timestamp en secondes
    private fun String.toFloatTime(): Float {
        val parts = this.split(":")
        val minutes = parts[0].toFloat()
        val seconds = parts[1].toFloat()
        return minutes * 60 + seconds
    }

    fun fromJson(json: String): Song? {
        return songAdapter.fromJson(json)
    }

    fun toJson(song: Song): String {
        return songAdapter.toJson(song)
    }
}