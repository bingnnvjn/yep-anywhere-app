package com.yepanywhere.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.SettingsStore
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsStore) : ViewModel() {

    val serverUrl = settings.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val password = settings.password.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val darkMode = settings.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val isConfigured = settings.isConfigured.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _serverStatus = MutableStateFlow<Map<String, Any>?>(null)
    val serverStatus: StateFlow<Map<String, Any>?> = _serverStatus

    private val _statusLoading = MutableStateFlow(false)
    val statusLoading: StateFlow<Boolean> = _statusLoading

    fun save(url: String, password: String) {
        viewModelScope.launch { settings.save(url, password) }
    }

    fun setDarkMode(mode: Int) {
        viewModelScope.launch { settings.setDarkMode(mode) }
    }

    fun loadServerStatus(api: ApiService) {
        viewModelScope.launch {
            _statusLoading.value = true
            try {
                val version = api.getServerVersion()
                val auth = try { api.getAuthStatus() } catch (_: Exception) { null }
                _serverStatus.value = version + mapOf("auth" to (auth ?: emptyMap<String, Any>()))
            } catch (e: Exception) {
                _serverStatus.value = null
            } finally {
                _statusLoading.value = false
            }
        }
    }
}
