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

/**
 * Util class for playlist parsing
 */
class PlaylistUtil {

    private val moshiBuilder: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @OptIn(ExperimentalStdlibApi::class)
    private val playlistItemAdapter: JsonAdapter<List<PlaylistItem>> = this.moshiBuilder.adapter<List<PlaylistItem>>()

    suspend fun downloadPlaylist(url: String): String {
        val client = HttpClient(CIO)
        val response: HttpResponse
        try {
            response = client.get(url)
            return response.bodyAsText()
        } catch(e: Exception) {
            throw Exception("Impossible de télécharger la playlist pour le moment.")
        } finally {
            client.close()
        }
    }

    fun fromJson(json: String): List<PlaylistItem> {
        return playlistItemAdapter.fromJson(json) ?: emptyList()
    }

    fun toJson(playlistItems: List<PlaylistItem>): String {
        return playlistItemAdapter.toJson(playlistItems)
    }
}