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

package com.dpither.supersmartkey.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dpither.supersmartkey.data.model.Settings
import com.dpither.supersmartkey.util.DEFAULT_GRACE_PERIOD
import com.dpither.supersmartkey.util.DEFAULT_POLLING_RATE
import com.dpither.supersmartkey.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SERVICE_REPO"
private const val SERVICE_NAME = "service"
private val Context.dataStore by preferencesDataStore(SERVICE_NAME)

@Singleton
class ServiceRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object PreferencesKeys {
        val RSSI_THRESHOLD = intPreferencesKey("rssi_threshold")
        val GRACE_PERIOD = intPreferencesKey("grace_period")
        val POLLING_RATE = intPreferencesKey("polling_rate")
        val IS_LOCK_SERVICE_RUNNING = booleanPreferencesKey("is_lock_service_running")
    }

    private val dataStore: DataStore<Preferences> = context.dataStore

    val settingsFlow: Flow<Settings> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error reading settings", exception)
        } else {
            throw exception
        }
    }.map { preferences ->
        mapSettings(preferences)
    }

    suspend fun updateRssiThreshold(rssiThreshold: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RSSI_THRESHOLD] = rssiThreshold
        }
    }

    suspend fun updateGracePeriod(gracePeriod: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRACE_PERIOD] = gracePeriod
        }
    }

    suspend fun updatePollingRate(pollingRate: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.POLLING_RATE] = pollingRate
        }
    }

    val isLockServiceRunningFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOCK_SERVICE_RUNNING] ?: false
    }

    suspend fun updateIsLockServiceRunning(isLockServiceRunning: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOCK_SERVICE_RUNNING] = isLockServiceRunning
        }
    }

    private fun mapSettings(preferences: Preferences): Settings {
        val rssiThreshold = preferences[PreferencesKeys.RSSI_THRESHOLD] ?: DEFAULT_RSSI_THRESHOLD
        val gracePeriod = preferences[PreferencesKeys.GRACE_PERIOD] ?: DEFAULT_GRACE_PERIOD
        val pollingRate = preferences[PreferencesKeys.POLLING_RATE] ?: DEFAULT_POLLING_RATE

        return Settings(
            rssiThreshold = rssiThreshold,
            gracePeriod = gracePeriod,
            pollingRateSeconds = pollingRate
        )
    }
}