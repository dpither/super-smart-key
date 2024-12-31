package com.example.supersmartkeyapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SETTINGS_NAME = "settings"
private val Context.dataStore by preferencesDataStore(SETTINGS_NAME)

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext context: Context) {
    private object Keys {
        val RSSI_THRESHOLD = intPreferencesKey("rssi_threshold")
    }

    private val settingsDataStore = context.dataStore

    val rssiThreshold: Flow<Int> = settingsDataStore.data.map { preferences ->
        preferences[Keys.RSSI_THRESHOLD] ?: DEFAULT_RSSI_THRESHOLD
    }

    suspend fun updateRSSIThreshold(rssiThreshold: Int) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.RSSI_THRESHOLD] = rssiThreshold
        }
    }
}