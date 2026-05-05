package com.yepanywhere.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.yepanywhere.app.data.SettingsDataStore
import com.yepanywhere.app.ui.screens.ChatScreen
import com.yepanywhere.app.ui.screens.ConfigScreen
import com.yepanywhere.app.ui.theme.YepAnywhereTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var settings: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settings = (application as YepApplication).settingsDataStore

        setContent {
            val scope = rememberCoroutineScope()
            var showConfig by remember { mutableStateOf(false) }
            val isConfigured by settings.isConfigured.collectAsState(initial = null)
            val savedUrl by settings.serverUrl.collectAsState(initial = "")
            val savedPassword by settings.password.collectAsState(initial = "")
            val darkModePref by settings.darkMode.collectAsState(initial = 0)

            // Determine actual dark mode: 0=system, 1=light, 2=dark
            val systemDark = isSystemInDarkTheme()
            val isDark = remember(darkModePref, systemDark) {
                when (darkModePref) {
                    0 -> systemDark  // follow system
                    1 -> false       // force light
                    2 -> true        // force dark
                    else -> systemDark
                }
            }

            YepAnywhereTheme(darkTheme = isDark) {
                val configured = isConfigured
                if (configured == null) return@YepAnywhereTheme

                if (!configured || showConfig) {
                    ConfigScreen(
                        initialUrl = savedUrl,
                        initialPassword = savedPassword,
                        initialDarkMode = darkModePref,
                        onSave = { url, password ->
                            scope.launch {
                                settings.save(url, password)
                                showConfig = false
                            }
                        },
                        onDarkModeChange = { mode ->
                            scope.launch { settings.setDarkMode(mode) }
                        }
                    )
                } else {
                    ChatScreen(
                        serverUrl = savedUrl,
                        password = savedPassword,
                        isDarkMode = isDark,
                        onBackToConfig = { showConfig = true }
                    )
                }
            }
        }
    }
}
