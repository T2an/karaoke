package fr.enssat.singwithme.heyrendt_quintin.util

import android.util.Log
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
import kotlin.math.log

/**
 * Classe utile pour le téléchargement et le parsing d'une musique
 *
 * @param songUrl, le lien de la musique
 */
class SongUtil(private val songUrl: String) {

    // Initialise une instance Moshi avec un adaptateur pour le parsing de JSON
    private val moshiBuilder: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Initialise un adapter JSON pour une musique
    @OptIn(ExperimentalStdlibApi::class)
    private val songAdapter: JsonAdapter<Song> = this.moshiBuilder.adapter<Song>()

    /**
     * Télécharge une musique avec une requête HTTP
     *
     * @return le contenu de la musique au format texte
     * @throws Exception, une exception en cas de problème lors de la requête HTTP
     */
    suspend fun downloadSong(): String {
        val client = HttpClient(CIO)
        val response: HttpResponse
        try {
            response = client.get(songUrl)
            return response.bodyAsText()
        } catch(e: Exception) {
            throw Exception("Impossible de télécharger la musique pour le moment.")
        } finally {
            client.close()
        }
    }

    /**
     * Transforme le fichier md de la musique en objet Song
     *
     * @return la musique
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
        val lyrics = lyricSegments.map { line ->
            line.joinToString { it.text }
        }

        return Song(title, author, soundtrack, lyrics, lyricSegments)
    }

    /**
     * Transforme les paroles en liste de LyricSegment
     *
     * @return la liste des segments de paroles
     */
    private fun parseLyrics(rawLyrics: List<String>): List<List<LyricSegment>> {
        val regex = """\{ (\d+):(\d+)(?:\.(\d+))? \}""".toRegex() // Pour capturer les timestamps
        val segments = mutableListOf<List<LyricSegment>>()

        for (line in rawLyrics) {
            val matches = regex.findAll(line).toList()
            val lyricSegments = mutableListOf<LyricSegment>()

            for (i in matches.indices) {
                val match = matches[i]
                val startMinute = match.groupValues[1].toInt()
                val startSecond = match.groupValues[2].toInt()
                val startCenti = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 0

                val startTime = startMinute * 60 + startSecond + (startCenti / 100f)

                // Si on est au dernier timestamp, vérifier s'il y a un texte après le dernier
                val isLastTimestamp = i == matches.size - 1
                val endIndex = if (isLastTimestamp) line.length else matches[i + 1].range.first
                val text = line.substring(match.range.last + 1, endIndex).trim()

                // Ajouter le segment uniquement s'il y a du texte
                if (text.isNotEmpty()) {
                    lyricSegments.add(LyricSegment(startTime, text, -1f))
                }
            }

            if(lyricSegments.isNotEmpty()) segments.add(lyricSegments)
        }

        // Calculer les durées restantes pour les segments
        for (i in segments.indices) {
            for (j in segments[i].indices) {
                val current = segments[i][j]
                val next = if (j + 1 < segments[i].size) {
                    segments[i][j + 1]
                } else if (i + 1 < segments.size && segments[i + 1].isNotEmpty()) {
                    segments[i + 1].firstOrNull()
                } else null

                if (next != null) {
                    // La durée est calculée à partir du startTime suivant
                    current.duration = next.startTime - current.startTime
                }
            }
        }

        return segments
    }

    /**
     * Converti un timestamp en secondes
     */
    private fun String.toFloatTime(): Float {
        val parts = this.split(":")
        val minutes = parts[0].toFloat()
        val seconds = parts[1].toFloat()
        return minutes * 60 + seconds
    }

    /**
     * Transforme un JSON en musique
     *
     * @return la musique
     */
    fun fromJson(json: String): Song? {
        return songAdapter.fromJson(json)
    }

    /**
     * Transforme une musique en JSON
     *
     * @return le JSON
     */
    fun toJson(song: Song): String {
        return songAdapter.toJson(song)
    }
}