package fr.enssat.singwithme.heyrendt_quintin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.enssat.singwithme.heyrendt_quintin.util.PlaylistUtil
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager

/**
 * Factory pour la création du ViewModel de HomeScreen
 */
class HomeViewModelFactory(
    private val playlistUtil: PlaylistUtil,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(playlistUtil, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
