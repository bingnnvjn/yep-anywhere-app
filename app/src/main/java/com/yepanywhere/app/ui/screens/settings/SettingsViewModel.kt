package com.yepanywhere.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.SettingsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsStore) : ViewModel() {

    val serverUrl = settings.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val password = settings.password.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val darkMode = settings.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val isConfigured = settings.isConfigured.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun save(url: String, password: String) {
        viewModelScope.launch { settings.save(url, password) }
    }

    fun setDarkMode(mode: Int) {
        viewModelScope.launch { settings.setDarkMode(mode) }
    }
}
