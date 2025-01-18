package fr.enssat.singwithme.heyrendt_quintin.data

import androidx.compose.runtime.Immutable

/**
 * Classe représentant une musique
 *
 * @property title, le titre de la musique
 * @property author, le nom de l'artiste
 * @property soundtrack, le chemin du fichier MP3
 * @property lyrics, la liste des paroles
 * @property lyricSegments, la liste des segments de paroles
 */
@Immutable
data class Song(
    val title: String,
    val author: String,
    val soundtrack: String,
    val lyrics: List<String>,
    val lyricSegments: List<List<LyricSegment>>
)