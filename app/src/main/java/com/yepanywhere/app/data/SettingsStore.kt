package com.yepanywhere.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val PASSWORD = stringPreferencesKey("password")
        private val DARK_MODE = intPreferencesKey("dark_mode")
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { it[SERVER_URL] ?: "" }
    val password: Flow<String> = context.dataStore.data.map { it[PASSWORD] ?: "" }
    val darkMode: Flow<Int> = context.dataStore.data.map { it[DARK_MODE] ?: 0 }
    val isConfigured: Flow<Boolean> = context.dataStore.data.map { !it[SERVER_URL].isNullOrBlank() }

    suspend fun save(url: String, password: String) {
        context.dataStore.edit {
            it[SERVER_URL] = url
            it[PASSWORD] = password
        }
    }

    suspend fun setDarkMode(mode: Int) {
        context.dataStore.edit { it[DARK_MODE] = mode }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
