package com.example.inventory.data

import com.squareup.moshi.JsonClass

/**
 *
 */
@JsonClass(generateAdapter = true)
data class PlaylistItem(
    val name: String,
    val artist: String,
    val locked: Boolean?,
    val path: String?
)