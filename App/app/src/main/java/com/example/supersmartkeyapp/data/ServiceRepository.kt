package com.example.supersmartkeyapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.supersmartkeyapp.util.DEFAULT_GRACE_PERIOD
import com.example.supersmartkeyapp.util.DEFAULT_POLLING_RATE
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SERVICE_NAME = "service"
private val Context.dataStore by preferencesDataStore(SERVICE_NAME)

@Singleton
class ServiceRepository @Inject constructor(@ApplicationContext context: Context) {

    private object Keys {
        val RSSI_THRESHOLD = intPreferencesKey("rssi_threshold")
        val GRACE_PERIOD = intPreferencesKey("grace_period")
        val POLLING_RATE = intPreferencesKey("polling_rate")
        val IS_SERVICE_RUNNING = booleanPreferencesKey("is_service_running")
        val IS_KEY_LINKED = booleanPreferencesKey("is_key_linked")
        val DEVICE_ADDRESS = stringPreferencesKey("device_address")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val RSSI = intPreferencesKey("rssi")
    }

    private val dataStore = context.dataStore

    /**
     * Get the rssiThreshold flow.
     */
    val rssiThreshold: Flow<Int> = dataStore.data.map { preferences ->
        preferences[Keys.RSSI_THRESHOLD] ?: DEFAULT_RSSI_THRESHOLD
    }

    suspend fun updateRssiThreshold(rssiThreshold: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.RSSI_THRESHOLD] = rssiThreshold
        }
    }

    /**
     * Get the gracePeriod flow.
     */
    val gracePeriod: Flow<Int> = dataStore.data.map { preferences ->
        preferences[Keys.GRACE_PERIOD] ?: DEFAULT_GRACE_PERIOD
    }

    suspend fun updateGracePeriod(gracePeriod: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.GRACE_PERIOD] = gracePeriod
        }
    }

    /**
     * Get the pollingRate flow.
     */
    val pollingRate: Flow<Int> = dataStore.data.map { preferences ->
        preferences[Keys.POLLING_RATE] ?: DEFAULT_POLLING_RATE
    }

    suspend fun updatePollingRate(pollingRate: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.POLLING_RATE] = pollingRate
        }
    }

    /**
     * Get the isServiceRunning flow.
     */
    val isServiceRunning: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.IS_SERVICE_RUNNING] ?: false
    }

    suspend fun updateIsServiceRunning(isServiceRunning: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_SERVICE_RUNNING] = isServiceRunning
        }
    }

    /**
     * Get the isKeyLinked flow.
     */
    val isKeyLinked: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.IS_KEY_LINKED] ?: false
    }

    suspend fun updateIsKeyLinked(isKeyLinked: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_KEY_LINKED] = isKeyLinked
        }
    }

    /**
     * Get the deviceAddress flow.
     */
    val deviceAddress: Flow<String> = dataStore.data.map { preferences ->
        preferences[Keys.DEVICE_ADDRESS] ?: ""
    }

    suspend fun updateDeviceAddress(deviceAddress: String) {
        dataStore.edit { preferences ->
            preferences[Keys.DEVICE_ADDRESS] = deviceAddress
        }
    }

    /**
     * Get the deviceName flow.
     */
    val deviceName: Flow<String> = dataStore.data.map { preferences ->
        preferences[Keys.DEVICE_NAME] ?: ""
    }

    suspend fun updateDeviceName(deviceName: String) {
        dataStore.edit { preferences ->
            preferences[Keys.DEVICE_NAME] = deviceName
        }
    }

    /**
     * Get the rssi flow.
     */
    val rssi: Flow<Int> = dataStore.data.map { preferences ->
        preferences[Keys.RSSI] ?: 1
    }

    suspend fun updateRssi(rssi: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.RSSI] = rssi
        }
    }
}