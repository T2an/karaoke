package fr.enssat.singwithme.heyrendt_quintin.ui.karaoke

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import fr.enssat.singwithme.heyrendt_quintin.R
import fr.enssat.singwithme.heyrendt_quintin.SingWithMeTopAppBar
import fr.enssat.singwithme.heyrendt_quintin.data.Song
import fr.enssat.singwithme.heyrendt_quintin.ui.navigation.NavigationDestination
import fr.enssat.singwithme.heyrendt_quintin.ui.theme.SingWithMeTheme
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager
import fr.enssat.singwithme.heyrendt_quintin.util.SongUtil
import kotlinx.coroutines.delay

object KaraokeDestination : NavigationDestination {
    override val route: String = "karaoke_screen/{songPath}"
    override val titleRes: Int = R.string.karaoke_screen_title

    const val songPathArg = "songPath"
}

// TODO : Tester en light theme
@androidx.annotation.OptIn(UnstableApi::class) @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraokeScreen(
    songPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Définit le chemin de la playlist
    val songUrl = "${stringResource(R.string.base_url)}/${songPath}"

    // Instancie le view model
    val viewModel: KaraokeViewModel = viewModel(
        factory = KaraokeViewModelFactory(
            context,
            songPath,
            SongUtil(songUrl),
            PreferencesManager(context)
        )
    )

    val song by viewModel.song.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLine by viewModel.currentLine.collectAsState()
    val audioPlayer by viewModel.audioPlayer.collectAsState()
    val isPlayerPlaying by viewModel.isPlayerPlaying.collectAsState()
    val karaokeAnimation by viewModel.karaokeAnimation.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    if (song != null) {
        val soundtrackUrl = "${stringResource(R.string.base_url)}/${songPath.split("/")[0]}/${song?.soundtrack}"
        LaunchedEffect(soundtrackUrl) {
            viewModel.initializePlayer(soundtrackUrl)
        }

        if (isPlayerPlaying) {
            LaunchedEffect(currentLine) {
                val currentSegments = song!!.lyricSegments[currentLine]

                // Calcul du délai avant de commencer la ligne
                val startTime = currentSegments[0].startTime.toLong()
                val delay = if (currentLine == 0) {
                    startTime
                } else {
                    song!!.lyricSegments[currentLine + 1][0].startTime.toLong() - (startTime + currentSegments[0].duration.toLong())
                }
                delay(delay * 1000) // Attente avant le début de l'animation

                var elapsedTime = 0f // Suivi du temps écoulé pour la ligne actuelle

                // Calcul de la durée totale de la ligne
                val totalLineDuration = currentSegments.sumOf { it.duration.toLong() }

                // Animation de chaque segment
                for (segment in currentSegments) {
                    // Réinitialiser l'animation
                    karaokeAnimation.snapTo(0f)

                    val segmentEndProgress = (elapsedTime + segment.duration) / totalLineDuration

                    // Lancer l'animation avec progression fluide
                    karaokeAnimation.animateTo(
                        segmentEndProgress,
                        tween(
                            durationMillis = (segment.duration * 1000).toInt(),
                            easing = LinearEasing
                        )
                    )

                    // Mettre à jour le temps écoulé pour le prochain segment
                    elapsedTime += segment.duration

                    // Attendre la durée du segment avant de passer au suivant
                    delay((segment.duration * 1000).toLong())
                }

                // Passer à la ligne suivante après avoir animé tous les segments
                viewModel.incrementCurrentLine()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.releasePlayer()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            SingWithMeTopAppBar(
                title = "Karaoké Player",
                canNavigateBack = true,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.refreshSong(songPath)
                    // TODO : Mettre plus de Toast sur HomeScreen
                    Toast.makeText(context, context.getString(R.string.refresh), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh)
                )
            }
        },
    ) { innerPadding ->
        if (isLoading) {
            Text(
                text = stringResource(R.string.loading),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(innerPadding)
            )
        } else {
            KaraokeBody(
                song = song,
                karaokeAnimation = karaokeAnimation,
                currentLine = currentLine,
                modifier = modifier.fillMaxSize(),
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
fun KaraokeBody(
    song: Song?,
    karaokeAnimation: Animatable<Float, AnimationVector1D>,
    currentLine: Int,
    modifier: Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(contentPadding),
    ) {
        if (song == null) {
            Text(
                text = stringResource(R.string.no_song_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding)
            )
        } else {
            KaraokeText(
                list = song.lyrics,
                current = currentLine,
                progress = karaokeAnimation.value
            )
        }
    }
}

// TODO : Voir si temps
//@Composable
//fun KaraokeActionButtons() {
//    val scope = rememberCoroutineScope()
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(PaddingValues(20.dp)),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        OutlinedIconButton(
//            onClick = { goBack() },
//            modifier = Modifier.size(50.dp),
//            shape = CircleShape,
//            border = BorderStroke(1.dp, Color.Black),
//        ) {
//            Icon(
//                imageVector = Filled.ArrowBack,
//                contentDescription = stringResource(R.string.back_button)
//            )
//        }
//        OutlinedIconButton(
//            onClick = { playOrPause(scope) },
//            modifier = Modifier.size(50.dp),
//            shape = CircleShape,
//            border = BorderStroke(1.dp, Color.Black),
//        ) {
//            if (viewModel.a.isPlaying) {
//                Icon(
//                    imageVector = Filled.Pause,
//                    contentDescription = stringResource(R.string.back_button)
//                )
//            } else {
//                Icon(
//                    imageVector = Filled.PlayArrow,
//                    contentDescription = stringResource(R.string.back_button)
//                )
//            }
//        }
//        OutlinedIconButton(
//            onClick = { replay() },
//            modifier = Modifier.size(50.dp),
//            shape = CircleShape,
//            border = BorderStroke(1.dp, Color.Black),
//        ) {
//            Icon(
//                imageVector = Filled.Refresh,
//                contentDescription = stringResource(R.string.back_button)
//            )
//        }
//    }
//}

//fun goBack() {
//    audioPlayer.release()
//    // TODO : Navigate to HomeScreen
//}
//
//fun playOrPause(scope: CoroutineScope) {
//    if (audioPlayer.isPlaying) {
//        audioPlayer.pause()
//        scope.launch {
//            karaokeAnimation.stop()
//        }
//    } else {
//        audioPlayer.play()
//        scope.launch {
//            karaokeAnimation.snapTo(karaokeAnimation.value)
//            karaokeAnimation.animateTo(1f, tween(3000, easing = LinearEasing))
//        }
//    }
//}
//
//fun replay() {
//    audioPlayer.seekTo(0)
//    audioPlayer.playWhenReady = true
//}

@Composable
fun KaraokeText(list: List<String>, current: Int, progress: Float) {
    LazyColumn(
        contentPadding = PaddingValues(5.dp)
    ) {
        itemsIndexed(items = list, key = { index, _ -> index }) { index, text ->
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
            color = Color.Gray,
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

@Preview(showBackground = true)
@Composable
fun KaraokeSimpleTextPreview() {
    SingWithMeTheme {
        KaraokeSimpleText(
            text = "I'm a creep, I'm a weirdo",
            progress = 0.40f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KaraokeTextPreview() {
    SingWithMeTheme {
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
