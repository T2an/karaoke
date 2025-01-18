/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.enssat.singwithme.heyrendt_quintin.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.enssat.singwithme.heyrendt_quintin.R
import fr.enssat.singwithme.heyrendt_quintin.SingWithMeTopAppBar
import fr.enssat.singwithme.heyrendt_quintin.data.PlaylistItem
import fr.enssat.singwithme.heyrendt_quintin.ui.navigation.NavigationDestination
import fr.enssat.singwithme.heyrendt_quintin.ui.theme.SingWithMeTheme
import fr.enssat.singwithme.heyrendt_quintin.util.PlaylistUtil
import fr.enssat.singwithme.heyrendt_quintin.util.PreferencesManager
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

/**
 * Entry route for Home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToKaraoke: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    val playlistUtil = PlaylistUtil()
    var playlistItems: List<PlaylistItem> by remember { mutableStateOf(emptyList()) }

    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val url = "${stringResource(R.string.base_url)}/${stringResource(R.string.playlist_file)}"
    val playlistItemsString: String? = preferencesManager.getData("playlistItems")

    if (playlistItemsString.isNullOrBlank()) {
        LaunchedEffect(Unit) {
            isLoading = true
            scope.launch {
                playlistItems = downloadAndSavePlaylist(url, context, playlistUtil, preferencesManager)
                isLoading = false
            }
        }
    } else {
        playlistItems = playlistUtil.fromJson(playlistItemsString)
    }

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
                onClick = {
                    isLoading = true
                    playlistItems = emptyList()

                    scope.launch {
                        playlistItems = downloadAndSavePlaylist(url, context, playlistUtil, preferencesManager)
                        if (playlistItems.isNotEmpty())
                            Toast.makeText(context, "Actualisation finie !", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh)
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Text(
                text = stringResource(R.string.loading),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(innerPadding)
            )
        } else {
            HomeBody(
                playlistItems = playlistItems,
                onItemClick = { musicPath -> onNavigateToKaraoke(musicPath) },
                modifier = modifier.fillMaxSize(),
                contentPadding = innerPadding,
            )
        }
    }
}

suspend fun downloadAndSavePlaylist(
    url: String,
    context: Context,
    playlistUtil: PlaylistUtil,
    preferencesManager: PreferencesManager
): List<PlaylistItem> {
    return try {
        val body: String = playlistUtil.downloadPlaylist(url)
        val playlistItems: List<PlaylistItem> = playlistUtil.fromJson(body)
        preferencesManager.saveData("playlistItems", playlistUtil.toJson(playlistItems))
        playlistItems
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        emptyList()
    }
}

// TODO : Tester en light theme
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
            Text(
                text = stringResource(R.string.no_playlist_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding),
            )
        } else {
            val context = LocalContext.current

            PlaylistItems(
                itemList = playlistItems,
                onItemClick = { item ->
                    if (item.locked || item.path.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            "Cette musique n'est pas disponible pour le karaoké",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Utilisation de 'item.path' au lieu de 'item.name'
                        onItemClick(item.path.let {
                            URLEncoder.encode(
                                it,
                                StandardCharsets.UTF_8.toString()
                            )
                        } ?: "")
                    }
                },
                contentPadding = contentPadding,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
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
    Column(modifier = modifier.padding(contentPadding)) {
        Text(
            text = stringResource(R.string.subtitle),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        LazyColumn {
            items(items = itemList, key = { it.name }) { item ->
                PlaylistItem(
                    item = item,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_small)),
                    onClick = onItemClick
                )
            }
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
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
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
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    SingWithMeTheme {
        HomeBody(listOf(
            PlaylistItem("Bohemian Rhapsody", "Queen", false, path = ""),
            PlaylistItem("Creep", "Radiohead", false, path = ""),
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

