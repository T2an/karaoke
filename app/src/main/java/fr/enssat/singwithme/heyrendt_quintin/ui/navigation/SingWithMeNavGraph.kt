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
                onNavigateToKaraoke = { songPath ->
                    // Naviguer vers la destination Karaoke en injectant la valeur du songPath
                    navController.navigate("karaoke_screen/$songPath")
                }
            )
        }

        // TODO : Faire le retour arrière
        composable(
            route = KaraokeDestination.route,
            arguments = listOf(
                navArgument(KaraokeDestination.songPathArg) { type = NavType.StringType }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val songPath = backStackEntry.arguments?.getString(KaraokeDestination.songPathArg)
            val decodedPath = songPath?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }

            if (decodedPath != null) {
                KaraokeScreen(songPath = decodedPath)
            }
        }

    }
}
