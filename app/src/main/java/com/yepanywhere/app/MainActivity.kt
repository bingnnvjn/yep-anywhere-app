package com.yepanywhere.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
            YepAnywhereTheme {
                val scope = rememberCoroutineScope()
                var showConfig by remember { mutableStateOf(false) }
                val isConfigured by settings.isConfigured.collectAsState(initial = null)
                val savedUrl by settings.serverUrl.collectAsState(initial = "")
                val savedPassword by settings.password.collectAsState(initial = "")

                if (isConfigured == null) return@YepAnywhereTheme

                if (!isConfigured || showConfig) {
                    ConfigScreen(
                        initialUrl = savedUrl,
                        initialPassword = savedPassword,
                        onSave = { url, password ->
                            scope.launch {
                                settings.save(url, password)
                                showConfig = false
                            }
                        }
                    )
                } else {
                    ChatScreen(
                        serverUrl = savedUrl,
                        password = savedPassword,
                        onBackToConfig = { showConfig = true }
                    )
                }
            }
        }
    }
}
