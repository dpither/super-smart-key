package com.example.supersmartkeyapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
        val IS_SERVICE_RUNNING = booleanPreferencesKey("is_service_running")
        val IS_KEY_LINKED = booleanPreferencesKey("is_key_linked")
        val DEVICE_ADDRESS = stringPreferencesKey("device_address")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val RSSI = intPreferencesKey("rssi")
    }

    private val serviceDataStore = context.dataStore

    //  IS_SERVICE_RUNNING
    val isServiceRunning: Flow<Boolean> = serviceDataStore.data.map { preferences ->
        preferences[Keys.IS_SERVICE_RUNNING] ?: false
    }

    suspend fun updateIsServiceRunning(isServiceRunning: Boolean) {
        serviceDataStore.edit { preferences ->
            preferences[Keys.IS_SERVICE_RUNNING] = isServiceRunning
        }
    }

    //  IS_KEY_LINKED
    val isKeyLinked: Flow<Boolean> = serviceDataStore.data.map { preferences ->
        preferences[Keys.IS_KEY_LINKED] ?: false
    }

    suspend fun updateIsKeyLinked(isKeyLinked: Boolean) {
        serviceDataStore.edit { preferences ->
            preferences[Keys.IS_KEY_LINKED] = isKeyLinked
        }
    }

    //  DEVICE_ADDRESS
    val deviceAddress: Flow<String> = serviceDataStore.data.map { preferences ->
        preferences[Keys.DEVICE_ADDRESS] ?: ""
    }

    suspend fun updateDeviceAddress(deviceAddress: String) {
        serviceDataStore.edit {preferences ->
            preferences[Keys.DEVICE_ADDRESS] = deviceAddress
        }
    }

    //  DEVICE_Name
    val deviceName: Flow<String> = serviceDataStore.data.map { preferences ->
        preferences[Keys.DEVICE_NAME] ?: ""
    }

    suspend fun updateDeviceName(deviceName: String) {
        serviceDataStore.edit {preferences ->
            preferences[Keys.DEVICE_NAME] = deviceName
        }
    }

    //  DEVICE_Name
    val rssi: Flow<Int> = serviceDataStore.data.map { preferences ->
        preferences[Keys.RSSI] ?: 0
    }

    suspend fun updateRssi(rssi: Int) {
        serviceDataStore.edit {preferences ->
            preferences[Keys.RSSI] = rssi
        }
    }
}