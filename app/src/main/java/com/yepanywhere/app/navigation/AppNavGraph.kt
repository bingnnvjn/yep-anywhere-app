package com.yepanywhere.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Routes {
    const val INBOX = "inbox"
    const val CHAT = "chat/{sessionId}/{projectId}"
    const val FILES = "files"
    const val SETTINGS = "settings"

    fun chat(sessionId: String, projectId: String) = "chat/$sessionId/$projectId"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.INBOX,
        enterTransition = { slideInHorizontally(tween(300)) { it } },
        exitTransition = { slideOutHorizontally(tween(250)) { -it / 3 } },
        popEnterTransition = { slideInHorizontally(tween(250)) { -it / 3 } },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } }
    ) {
        composable(Routes.INBOX) {
            // Placeholder — will be wired in Task 9
        }
        composable(Routes.CHAT) {
            // Placeholder — will be wired in Task 10
        }
        composable(Routes.FILES) {
            // Placeholder — will be wired in Task 11
        }
        composable(Routes.SETTINGS) {
            // Placeholder — will be wired in Task 8
        }
    }
}
