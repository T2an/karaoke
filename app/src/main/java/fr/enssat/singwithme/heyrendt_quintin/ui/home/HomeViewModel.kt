package fr.enssat.singwithme.heyrendt_quintin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.enssat.singwithme.heyrendt_quintin.data.PlaylistItem
import fr.enssat.singwithme.heyrendt_quintin.util.PlaylistUtil
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * View model pour la page d'accueil de l'application
 *
 * @param playlistUtil, l'instance de la classe utile des playlists
 * @param preferencesManager, l'instance de la classe de stockage des données
 */
class HomeViewModel(
    private var playlistUtil: PlaylistUtil,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _playlistItems = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val playlistItems: StateFlow<List<PlaylistItem>> = _playlistItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        initPlaylist()
    }

    /**
     * Initialise la playlist
     */
    private fun initPlaylist() {
        val playlistItemsString: String? = preferencesManager.getData("playlistItems")
        if (playlistItemsString.isNullOrBlank()) {
            // Si la playlist n'est pas stockée ou si la valeur stockée est incorrecte
            refreshPlaylist()
        } else {
            // Récupère la donnée stockée et la déserialize
            _playlistItems.value = playlistUtil.fromJson(playlistItemsString)
        }
    }

    /**
     * Met à jour la playlist
     */
    fun refreshPlaylist() {
        viewModelScope.launch {
            _isLoading.value = true
            _playlistItems.value = try {
                val newPlaylist = downloadAndSavePlaylist()
                newPlaylist
            } catch (e: Exception) {
                emptyList()
            }
            _isLoading.value = false
        }
    }

    /**
     * Télécharge et stocke en cache la playlist
     *
     * @return la liste des éléments de la playlist
     */
    private suspend fun downloadAndSavePlaylist(): List<PlaylistItem> {
        return try {
            val body: String = playlistUtil.downloadPlaylist()
            val playlistItems: List<PlaylistItem> = playlistUtil.fromJson(body)
            preferencesManager.saveData("playlistItems", playlistUtil.toJson(playlistItems))
            playlistItems
        } catch (e: Exception) {
            emptyList()
        }
    }
}
