package fr.enssat.singwithme.heyrendt_quintin.data

import androidx.compose.runtime.Immutable

/**
 * Classe représentant un segment d'une ligne de parole
 *
 * @property startTime, le temps de démarrage du segment en secondes
 * @property text, le texte du segment
 * @property duration, la durée du segment en secondes
 */
@Immutable
data class LyricSegment(
    var startTime: Float,
    val text: String,
    var duration: Float
)
