package fr.enssat.singwithme.heyrendt_quintin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.enssat.singwithme.heyrendt_quintin.ui.home.HomeDestination
import fr.enssat.singwithme.heyrendt_quintin.ui.home.HomeScreen
import fr.enssat.singwithme.heyrendt_quintin.ui.karaoke.KaraokeScreen
import fr.enssat.singwithme.heyrendt_quintin.ui.karaoke.KaraokeDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@Composable
fun SingWithMeNavHost(
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
