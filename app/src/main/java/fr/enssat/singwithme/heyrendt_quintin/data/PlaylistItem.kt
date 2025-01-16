package fr.enssat.singwithme.heyrendt_quintin.data

/**
 *
 */
data class PlaylistItem(
    val name: String,
    val artist: String,
    val locked: Boolean = false,
    val path: String?
)