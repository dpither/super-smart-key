/*
    Copyright (C) 2025  Dylan Pither

    This file is part of Super Smart Key.

    Super Smart Key is free software: you can redistribute it and/or modify it under the terms of
    the GNU General Public License as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    Super Smart Key is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with Super Smart Key.
    If not, see <https://www.gnu.org/licenses/>.
 */

package com.dpither.supersmartkey.ui

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
import com.dpither.supersmartkey.ui.home.HomeScreen
import com.dpither.supersmartkey.ui.settings.SettingsScreen
import com.dpither.supersmartkey.util.DEFAULT_ANIMATION_DURATION

private object Screens {
    const val HOME_SCREEN = "home"
    const val SETTINGS_SCREEN = "settings"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = Screens.HOME_SCREEN,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }) {
        composable(route = Screens.HOME_SCREEN) {
            HomeScreen(onSettings = { navController.navigate(Screens.SETTINGS_SCREEN) })
        }
        composable(route = Screens.SETTINGS_SCREEN, enterTransition = {
            slideInHorizontally(animationSpec = tween(
                durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseIn
            ), initialOffsetX = { it })
        }, exitTransition = {
            slideOutHorizontally(animationSpec = tween(
                durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseOut
            ), targetOffsetX = { it })
        }) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}