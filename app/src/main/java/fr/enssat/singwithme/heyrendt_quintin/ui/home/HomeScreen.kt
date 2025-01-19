package fr.enssat.singwithme.heyrendt_quintin.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.enssat.singwithme.heyrendt_quintin.R
import fr.enssat.singwithme.heyrendt_quintin.SingWithMeTopAppBar
import fr.enssat.singwithme.heyrendt_quintin.data.PlaylistItem
import fr.enssat.singwithme.heyrendt_quintin.ui.navigation.NavigationDestination
import fr.enssat.singwithme.heyrendt_quintin.ui.theme.SingWithMeTheme
import fr.enssat.singwithme.heyrendt_quintin.util.PlaylistUtil
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToKaraoke: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Définit le chemin de la playlist
    val playlistUrl = "${stringResource(R.string.base_url)}/${stringResource(R.string.playlist_file)}"

    // Instancie le view model
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            PlaylistUtil(playlistUrl),
            PreferencesManager(context)
        )
    )

    val playlistItems by viewModel.playlistItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SingWithMeTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refreshPlaylist() },
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_playlist)
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.loading),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        } else {
            HomeBody(
                playlistItems = playlistItems,
                onItemClick = { songPath -> onNavigateToKaraoke(songPath) },
                modifier = modifier.fillMaxSize(),
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HomeBody(
    playlistItems: List<PlaylistItem>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (playlistItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_playlist_description),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        } else {
            val context = LocalContext.current

            PlaylistItems(
                itemList = playlistItems,
                onItemClick = { item ->
                    if (item.locked || item.path.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.song_not_available),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (item.path.isNotBlank()) {
                        onItemClick(URLEncoder.encode(item.path, StandardCharsets.UTF_8.toString()))
                    }
                },
                contentPadding = contentPadding,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Composable
private fun PlaylistItems(
    itemList: List<PlaylistItem>,
    onItemClick: (PlaylistItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(contentPadding)) {
        items(items = itemList, key = { it.name }) { item ->
            PlaylistItem(
                item = item,
                modifier = Modifier.padding(8.dp),
                onClick = onItemClick
            )
        }
    }
}


@Composable
private fun PlaylistItem(
    item: PlaylistItem,
    modifier: Modifier = Modifier,
    onClick: (PlaylistItem) -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick(item) },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = item.artist,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (!item.locked) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = stringResource(R.string.available)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    SingWithMeTheme {
        HomeBody(listOf(
            PlaylistItem("Bohemian Rhapsody", "Queen", false, path = ""),
            PlaylistItem("Creep", "Radiohead", true, path = ""),
        ), onItemClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyEmptyListPreview() {
    SingWithMeTheme {
        HomeBody(listOf(), onItemClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistItemPreview() {
    SingWithMeTheme {
        PlaylistItem(
            item = PlaylistItem("Bohemian Rhapsody", "Queen", false, path = ""),
            onClick = { clickedItem ->
                println("Clicked on: ${clickedItem.name} by ${clickedItem.artist}")
            }
        )
    }
}

