package com.example.supersmartkeyapp.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.supersmartkeyapp.ui.home.HomeScreen
import com.example.supersmartkeyapp.ui.settings.SettingsScreen

private object Screens {
    const val HOME_SCREEN = "home"
    const val SETTINGS_SCREEN = "settings"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val animationDuration = 300
    NavHost(navController = navController,
        startDestination = Screens.HOME_SCREEN,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }) {
        composable(route = Screens.HOME_SCREEN) {
            HomeScreen(onSettings = { navController.navigate(Screens.SETTINGS_SCREEN) })
        }
        composable(route = Screens.SETTINGS_SCREEN, enterTransition = {
            slideInHorizontally(animationSpec = tween(
                durationMillis = animationDuration,
                easing = EaseIn
            ),
                initialOffsetX = { it })
        }, exitTransition = {
            slideOutHorizontally(animationSpec = tween(
                durationMillis = animationDuration,
                easing = EaseOut
            ),
                targetOffsetX = { it })
        }) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

