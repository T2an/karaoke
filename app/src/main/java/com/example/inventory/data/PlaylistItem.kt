package com.example.inventory.data

/**
 *
 */
data class PlaylistItem(
    val name: String,
    val artist: String,
    val locked: Boolean = false,
    val path: String?
)