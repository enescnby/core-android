package com.shade.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shade.app.ui.auth.AuthScreen
import com.shade.app.ui.chat.ChatScreen
import com.shade.app.ui.home.HomeScreen
import com.shade.app.ui.navigation.Screen
import com.shade.app.ui.theme.ShadeTheme
import com.shade.app.ui.user.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShadeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = hiltViewModel(),
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onChatClick = { chatId, chatName ->
                    navController.navigate(Screen.Chat.createRoute(chatId, chatName))
                },
                onNavigateToContacts = {
                    // Rehber ekranı eklendiğinde buraya gelecek
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) {
            ChatScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileClick = { shadeId ->
                    navController.navigate(Screen.Profile.createRoute(shadeId))
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("shadeId") { type = NavType.StringType })
        ) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
