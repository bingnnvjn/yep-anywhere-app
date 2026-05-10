package com.yepanywhere.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yepanywhere.app.data.SettingsStore
import com.yepanywhere.app.data.remote.ApiService
import com.yepanywhere.app.ui.screens.chat.ChatScreen
import com.yepanywhere.app.ui.screens.chat.ChatViewModel
import com.yepanywhere.app.ui.screens.files.FilesScreen
import com.yepanywhere.app.ui.screens.files.FilesViewModel
import com.yepanywhere.app.ui.screens.inbox.InboxScreen
import com.yepanywhere.app.ui.screens.inbox.InboxViewModel
import com.yepanywhere.app.ui.screens.settings.SettingsScreen
import com.yepanywhere.app.ui.screens.settings.SettingsViewModel

object Routes {
    const val INBOX = "inbox"
    const val CHAT = "chat/{sessionId}/{projectId}/{sessionTitle}"
    const val FILES = "files"
    const val SETTINGS = "settings"

    fun chat(sessionId: String, projectId: String, sessionTitle: String) =
        "chat/$sessionId/$projectId/${Uri.encode(sessionTitle)}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    api: ApiService,
    settingsStore: SettingsStore
) {
    NavHost(
        navController = navController,
        startDestination = Routes.INBOX,
        enterTransition = { slideInHorizontally(tween(300)) { it } },
        exitTransition = { slideOutHorizontally(tween(250)) { -it / 3 } },
        popEnterTransition = { slideInHorizontally(tween(250)) { -it / 3 } },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } }
    ) {
        composable(Routes.INBOX) {
            val vm: InboxViewModel = viewModel()
            InboxScreen(
                viewModel = vm,
                api = api,
                onSessionClick = { projectId, sessionId, sessionTitle ->
                    navController.navigate(Routes.chat(sessionId, projectId, sessionTitle))
                }
            )
        }
        composable(Routes.CHAT) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val sessionTitle = backStackEntry.arguments?.getString("sessionTitle") ?: "会话"
            val vm: ChatViewModel = viewModel()
            ChatScreen(
                viewModel = vm,
                api = api,
                projectId = projectId,
                sessionId = sessionId,
                sessionTitle = sessionTitle,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FILES) {
            val vm: FilesViewModel = viewModel()
            FilesScreen(
                viewModel = vm,
                api = api,
                projectId = "default",
                projectName = "项目",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel { SettingsViewModel(settingsStore) }
            SettingsScreen(viewModel = vm, api = api)
        }
    }
}
