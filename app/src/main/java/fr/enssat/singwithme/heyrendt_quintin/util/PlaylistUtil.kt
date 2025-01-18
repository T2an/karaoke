package fr.enssat.singwithme.heyrendt_quintin.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.enssat.singwithme.heyrendt_quintin.data.PlaylistItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

// TODO : Faire des TU et dire dans le readme comment les lancer
/**
 * Classe utile pour le téléchargement et le parsing de la playlist
 *
 * @param playlistUrl, le lien de la playlist
 */
class PlaylistUtil(private val playlistUrl: String) {

    // Initialise une instance Moshi avec un adaptateur pour le parsing de JSON
    private val moshiBuilder: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Initialise un adapter JSON pour les éléments de la playlist
    @OptIn(ExperimentalStdlibApi::class)
    private val playlistItemAdapter: JsonAdapter<List<PlaylistItem>> = this.moshiBuilder.adapter<List<PlaylistItem>>()

    /**
     * Télécharge le fichier playlist avec une requête HTTP
     *
     * @return le contenu de la playlist au format texte
     * @throws Exception, une exception en cas de problème lors de la requête HTTP
     */
    suspend fun downloadPlaylist(): String {
        val client = HttpClient(CIO)
        val response: HttpResponse
        try {
            response = client.get(playlistUrl)
            return response.bodyAsText()
        } catch(e: Exception) {
            throw Exception("Impossible de télécharger la playlist pour le moment.")
        } finally {
            client.close()
        }
    }

    /**
     * Transforme un JSON en liste de PlaylistItem
     *
     * @return la playlist
     */
    fun fromJson(json: String): List<PlaylistItem> {
        return playlistItemAdapter.fromJson(json) ?: emptyList()
    }

    /**
     * Transforme une liste de PlaylistItem en JSON
     *
     * @return le JSON
     */
    fun toJson(playlistItems: List<PlaylistItem>): String {
        return playlistItemAdapter.toJson(playlistItems)
    }
}