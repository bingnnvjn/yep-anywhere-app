package com.yepanywhere.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yepanywhere.app.data.SettingsStore
import com.yepanywhere.app.data.remote.AuthInterceptor
import com.yepanywhere.app.data.remote.ApiService
import com.yepanywhere.app.navigation.AppNavGraph
import com.yepanywhere.app.navigation.Routes
import com.yepanywhere.app.ui.theme.YepAnywhereTheme
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settings = (application as YepApplication).settingsStore

        setContent {
            val darkModePref by settings.darkMode.collectAsState(initialValue = 0)
            val systemDark = isSystemInDarkTheme()
            val isDark = when (darkModePref) {
                0 -> systemDark
                1 -> false
                2 -> true
                else -> systemDark
            }

            YepAnywhereTheme(darkTheme = isDark) {
                MainScreen(settings)
            }
        }
    }
}

data class TabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun MainScreen(settings: SettingsStore) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val serverUrl by settings.serverUrl.collectAsState(initialValue = "")

    val api = remember(serverUrl) {
        if (serverUrl.isBlank()) return@remember null
        val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().addInterceptor(AuthInterceptor(settings)).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val tabs = listOf(
        TabItem(Routes.INBOX, "会话", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble),
        TabItem(Routes.FILES, "文件", Icons.Filled.Folder, Icons.Outlined.Folder),
        TabItem(Routes.SETTINGS, "设置", Icons.Filled.Settings, Icons.Outlined.Settings),
    )

    val showBottomBar = currentRoute in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(Routes.INBOX) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (api != null) {
            AppNavGraph(
                navController = navController,
                api = api,
                settingsStore = settings
            )
        } else {
            // Not configured yet — show settings directly
            com.yepanywhere.app.ui.screens.settings.SettingsScreen(
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    com.yepanywhere.app.ui.screens.settings.SettingsViewModel(settings)
                }
            )
        }
    }
}
