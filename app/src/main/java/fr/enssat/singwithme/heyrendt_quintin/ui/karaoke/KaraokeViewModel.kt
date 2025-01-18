package fr.enssat.singwithme.heyrendt_quintin.ui.karaoke

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import fr.enssat.singwithme.heyrendt_quintin.data.Song
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager
import fr.enssat.singwithme.heyrendt_quintin.util.SongUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * View model pour la page de karaoké de l'application
 *
 * @param songUtil, l'instance de la classe utile des musiques
 * @param preferencesManager, l'instance de la classe de stockage des données
 */
class KaraokeViewModel(
    private val context: Context,
    private val songPath: String,
    private val songUtil: SongUtil,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _song = MutableStateFlow<Song?>(null)
    val song: StateFlow<Song?> = _song

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isPlayerInitialized = MutableStateFlow(false)
    val isPlayerInitialized: StateFlow<Boolean> get() = _isPlayerInitialized

    private val _currentLine = MutableStateFlow(0)
    val currentLine: StateFlow<Int> get() = _currentLine

    private val _isPlayerPlaying = MutableStateFlow(false)
    val isPlayerPlaying: StateFlow<Boolean> get() = _isPlayerPlaying

    val karaokeAnimation = Animatable(0f)

    private val _audioPlayer = MutableStateFlow<ExoPlayer?>(null)
    val audioPlayer: StateFlow<ExoPlayer?> = _audioPlayer

    fun initializePlayer(soundtrackUrl: String) {
        val player = ExoPlayer.Builder(context).build()
        // TODO : Cache mp3 marche pas
        // val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
        // .createMediaSource(mediaItem)
        //  setMediaSource(mediaSource)
        player.setMediaItem(MediaItem.fromUri(soundtrackUrl))
        player.prepare()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlayerPlaying.value = isPlaying
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "Playback error: ${error.message}")
            }
        })
        _audioPlayer.value = player
    }

    fun releasePlayer() {
        _audioPlayer.value?.release()
    }

    init {
        initSong()
    }

    /**
     * Initialise la musique
     */
    private fun initSong() {
        val songString: String? = preferencesManager.getData(songPath)
        if (songString.isNullOrBlank()) {
            // Si la musique n'est pas stockée ou si la valeur stockée est incorrecte
            refreshSong(songPath)
        } else {
            // Récupère la donnée stockée et la déserialize
            _song.value = songUtil.fromJson(songString)
        }
    }

    /**
     * Met à jour la musique
     */
    fun refreshSong(songPath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _song.value = try {
                val newSong = downloadAndSaveSong(songPath)
                newSong
            } catch (e: Exception) {
                null
            }
            _isLoading.value = false
            _isPlayerInitialized.value = true
        }
    }

    /**
     * Télécharge et stocke en cache la musique
     *
     * @return la musique
     */
    private suspend fun downloadAndSaveSong(songPath: String): Song? {
        if (_audioPlayer.value!!.isPlaying) _audioPlayer.value!!.stop()

        return try {
            val body: String = songUtil.downloadSong()
            val song: Song = songUtil.parseSong(body)
            preferencesManager.saveData(songPath, songUtil.toJson(song))
            song
        } catch (e: Exception) {
            null
        }
    }

    fun incrementCurrentLine() {
        _currentLine.value++
    }
}
