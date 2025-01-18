package fr.enssat.singwithme.heyrendt_quintin.data

import androidx.compose.runtime.Immutable

/**
 * Classe représentant un élément de la playlist
 *
 * @property name, le nom de la musique
 * @property artist, le nom de l'artiste
 * @property locked, si la musique est disponible ou non
 * @property path, le chemin du fichier karaoké
 */
@Immutable
data class PlaylistItem(
    val name: String,
    val artist: String,
    val locked: Boolean = false,
    val path: String?
)