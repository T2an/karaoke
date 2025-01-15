package com.example.inventory.ui.karaoke

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Song
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import com.example.inventory.util.MusicParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object KaraokeDestination : NavigationDestination {
    override val route: String = "karaoke_screen/{musicPath}"
    override val titleRes: Int = R.string.karaoke_screen_title

    const val musicPathArg = "musicPath"
}

lateinit var audioPlayer: ExoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraokeScreen(
    musicPath: String,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val musicUrl = "${stringResource(R.string.base_url)}/${musicPath}"
    val musicParser = MusicParser()

    val coroutineScope = rememberCoroutineScope()

    // État pour gérer le temps courant et la durée
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    val context = LocalContext.current

    var song: Song? by remember { mutableStateOf(null) }

    audioPlayer = ExoPlayer.Builder(context).build()

    var isPlayerInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            song = musicParser.parseSong(musicUrl)
            isPlayerInitialized = true
        }
    }

    if (isPlayerInitialized && song != null) {
        val soundtrackUrl = "${stringResource(R.string.base_url)}/${musicPath.split("/")[0]}/${song?.soundtrack}"
        // Init ExoPlayer
        audioPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(soundtrackUrl))
                prepare()
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            totalDuration = duration
                        }
                    }
                })
            }
        }
        audioPlayer.play()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = audioPlayer.currentPosition
            delay(500) // Mise à jour toutes les 500ms
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InventoryTopAppBar(
                title = "Karaoké Player",
                canNavigateBack = true,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            KaraokeActionButtons()
        }
    ) { innerPadding ->
        KaraokeBody(
            song = song,
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding,
        )
    }
}

@Composable
fun KaraokeBody(
    song: Song?,
    modifier: Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (song == null) {
            Text(
                // TODO : Utiliser des ressources partout
                text = "Chargement de la musique en cours...",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding),
            )
        } else {
            KaraokeText(
                list = song.lyrics,
                current = 0,
                progress = 0.0f
            )
        }
    }
}

@Composable
fun KaraokeActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(PaddingValues(20.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedIconButton(
            onClick = { goBack() },
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.Black),
        ) {
            Icon(
                imageVector = Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_button)
            )
        }
        OutlinedIconButton(
            onClick = { playOrPause() },
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.Black),
        ) {
            if (audioPlayer.isPlaying) {
                Icon(
                    imageVector = Filled.Pause,
                    contentDescription = stringResource(R.string.back_button)
                )
            } else {
                Icon(
                    imageVector = Filled.PlayArrow,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
        }
        OutlinedIconButton(
            onClick = { replay() },
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.Black),
        ) {
            Icon(
                imageVector = Filled.Refresh,
                contentDescription = stringResource(R.string.back_button)
            )
        }
    }
}

fun goBack() {
    audioPlayer.release()
    // TODO : Navigate to HomeScreen
}

fun playOrPause() {
    if (audioPlayer.isPlaying) {
        audioPlayer.pause()
    } else {
        audioPlayer.play()
    }
}

fun replay() {
    audioPlayer.seekTo(0)
    audioPlayer.playWhenReady = true
}

@Composable
fun KaraokeText(list: List<String>, current: Int, progress: Float) {
    Column {
        list.forEachIndexed { index, text ->
            if (index != current) {
                Text(
                    text = text,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                KaraokeSimpleText(
                    text = text,
                    progress = progress
                )
            }
        }
    }
}

@Composable
fun KaraokeSimpleText(text: String, progress: Float) {
    var textWidth by remember { mutableIntStateOf(0) }
    var visibleChars by remember { mutableIntStateOf(text.length) }

    Box {
        Text(
            text = text,
            color = Color.Red,
            modifier = Modifier
                .onSizeChanged { size ->
                    textWidth = size.width
                    visibleChars = (text.length * progress).toInt()
                }
        )

        Text(
            text = text.take(visibleChars),
            color = Color.Black,
            maxLines = 1,
        )

        Canvas(
            modifier = Modifier
                .height(24.dp)
                .width(2.dp)
                .offset(x = (textWidth * progress).pxToDp())
        ) {
            drawRect(color = Color.Gray)
        }
    }
}

@Composable
fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@Composable
fun KaraokeSimpleTextAnimate(duration: Int, text: String) {
    val karaokeAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        karaokeAnimation.animateTo(1f, tween(duration, easing = LinearEasing))
    }
    KaraokeSimpleText(text, karaokeAnimation.value)
}

@Preview(showBackground = true)
@Composable
fun KaraokeSimpleTextPreview() {
    InventoryTheme {
        KaraokeSimpleText(
            text = "I'm a creep, I'm a weirdo",
            progress = 0.40f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KaraokeTextPreview() {
    InventoryTheme {
        KaraokeText(
            list = listOf(
                "You're so fuckin' special",
                "I wish I was special",
                "I'm a creep, I'm a weirdo",
                "What the hell am I doing here?"
            ),
            current = 2,
            progress = 0.5f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KaraokeBodyPreview() {
    InventoryTheme {
        KaraokeBody(
            song = Song(
                "Creep",
                "Radiohead",
                "creep.mp3",
                lyrics = listOf(
                    "You're so fuckin' special",
                    "I wish I was special",
                    "I'm a creep, I'm a weirdo",
                    "What the hell am I doing here?"
                )
            ),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = 2.dp),
        )
    }
}
