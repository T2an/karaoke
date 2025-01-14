package com.example.inventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.inventory.ui.home.HomeDestination
import com.example.inventory.ui.home.HomeScreen
import com.example.inventory.ui.karaoke.KaraokeScreen
import com.example.inventory.ui.karaoke.KaraokeDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@Composable
fun InventoryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                onNavigateToKaraoke = { musicPath ->
                    // Naviguer vers la destination Karaoke en injectant la valeur du musicPath
                    navController.navigate("karaoke_screen/$musicPath") // Utiliser la bonne syntaxe ici
                }
            )
        }

        composable(
            route = KaraokeDestination.route,
            arguments = listOf(
                navArgument(KaraokeDestination.musicPathArg) { type = NavType.StringType }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val musicPath = backStackEntry.arguments?.getString(KaraokeDestination.musicPathArg)
            val decodedPath = musicPath?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }

            if (decodedPath != null) {
                KaraokeScreen(musicPath = decodedPath)
            }
        }

    }
}
