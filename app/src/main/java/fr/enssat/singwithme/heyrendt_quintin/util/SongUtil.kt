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
        val regex = Regex("\\{\\s*(\\d+:\\d+(\\.\\d+)?)\\s*\\}([^\\{]*)(\\{\\s*(\\d+:\\d+(\\.\\d+)?)\\s*\\})?")
        // Regex pour extraire timestamps et texte

        return rawLyrics.map { line -> // Traiter chaque ligne indépendamment
            val segments = mutableListOf<LyricSegment>()
            val matches = regex.findAll(line).toList()

            for ((index, match) in matches.withIndex()) {
                val startTimestamp = match.groups[1]?.value?.toFloatTime() ?: continue
                val text = match.groups[3]?.value?.trim() ?: ""
                val explicitEndTimestamp = match.groups[5]?.value?.toFloatTime()

                // Déterminer le timestamp de fin
                val endTimestamp = when {
                    explicitEndTimestamp != null -> explicitEndTimestamp
                    index + 1 < matches.size -> matches[index + 1].groups[1]?.value?.toFloatTime()
                    else -> null
                }

                if (endTimestamp != null) {
                    val duration = endTimestamp - startTimestamp
                    segments.add(LyricSegment(startTime = startTimestamp, text = text, duration = duration))
                } else {
                    // Dernière ligne sans ligne suivante, durée par défaut
                    segments.add(LyricSegment(startTime = startTimestamp, text = text, duration = 2f))
                }
            }

            segments // Retourner les segments pour cette ligne
        }
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