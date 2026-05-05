package com.yepanywhere.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val PASSWORD = stringPreferencesKey("password")
        private val DARK_MODE = intPreferencesKey("dark_mode") // 0=system, 1=light, 2=dark
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SERVER_URL] ?: ""
    }

    val password: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PASSWORD] ?: ""
    }

    val isConfigured: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[SERVER_URL].isNullOrBlank()
    }

    val darkMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: 0
    }

    suspend fun save(url: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL] = url
            prefs[PASSWORD] = password
        }
    }

    suspend fun setDarkMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = mode
        }
    }

    suspend fun getServerUrl(): String = serverUrl.first()
    suspend fun getPassword(): String = password.first()

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
