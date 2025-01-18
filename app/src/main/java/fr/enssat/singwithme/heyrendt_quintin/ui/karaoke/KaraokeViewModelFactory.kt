package fr.enssat.singwithme.heyrendt_quintin.ui.karaoke

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.enssat.singwithme.heyrendt_quintin.util.SongUtil
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager

/**
 * Factory pour la création du ViewModel de KaraokeScreen
 */
class KaraokeViewModelFactory(
    private var context: Context,
    private val songPath: String,
    private val songUtil: SongUtil,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KaraokeViewModel::class.java)) {
            return KaraokeViewModel(context, songPath, songUtil, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
