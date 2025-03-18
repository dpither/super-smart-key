package com.example.supersmartkeyapp

import android.graphics.Color.TRANSPARENT
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.supersmartkeyapp.ui.Navigation
import com.example.supersmartkeyapp.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuperSmartKeyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparentStyle = SystemBarStyle.light(scrim = TRANSPARENT, darkScrim = TRANSPARENT)
        enableEdgeToEdge(navigationBarStyle = transparentStyle)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false

        setContent {
            AppTheme {
                Navigation()
            }
        }
    }
}