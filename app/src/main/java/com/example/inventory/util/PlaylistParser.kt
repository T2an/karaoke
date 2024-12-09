package com.example.inventory.util

import android.util.Log
import com.example.inventory.data.PlaylistItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import java.net.URL
import java.util.Collections

/**
 * Util class for playlist parsing
 */
class PlaylistParser {

    /**
     * Parse a string into a Playlist object
     */
    @OptIn(ExperimentalStdlibApi::class)
    suspend fun parsePlaylist(url: String): List<PlaylistItem> {
        val client = HttpClient(CIO)
        val response: HttpResponse
        try {
            response = client.get(url)
            Log.i("DEBUG", response.bodyAsText())

            val moshiBuilder: Moshi = Moshi.Builder().build()
            val playlistItemAdapter: JsonAdapter<List<PlaylistItem>> = moshiBuilder.adapter<List<PlaylistItem>>()

            return playlistItemAdapter.fromJson(response.bodyAsText())!!
        } catch(e: Exception) {
            e.message?.let { Log.i("DEBUG", it) }
        } finally {
            client.close()
        }

        return Collections.emptyList()
    }
}