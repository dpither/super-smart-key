package com.dpither.supersmartkey

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.dpither.supersmartkey.ui.Navigation
import com.dpither.supersmartkey.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuperSmartKeyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparentStyle = SystemBarStyle.light(scrim = TRANSPARENT, darkScrim = TRANSPARENT)
        enableEdgeToEdge(navigationBarStyle = transparentStyle)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            false

        setContent {
            AppTheme {
                Navigation()
            }
        }
    }
}