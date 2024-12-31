package com.example.supersmartkeyapp.ui

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

    NavHost(navController = navController, startDestination = Screens.HOME_SCREEN) {
        composable(route = Screens.HOME_SCREEN) {
            HomeScreen(onSettings = { navController.navigate(Screens.SETTINGS_SCREEN) })
        }
        composable(route = Screens.SETTINGS_SCREEN) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

