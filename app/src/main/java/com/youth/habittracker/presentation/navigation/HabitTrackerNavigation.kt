package com.youth.habittracker.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.youth.habittracker.data.model.User
import com.youth.habittracker.presentation.auth.LoginScreen
import com.youth.habittracker.presentation.auth.RegisterScreen
import com.youth.habittracker.presentation.auth.SplashScreen
import com.youth.habittracker.presentation.main.MainScreen

@Composable
fun HabitTrackerNavigation(
    navController: NavHostController = rememberNavController(),
    currentUser: User?,
    onSignOut: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = when {
            currentUser == null -> Screen.Splash.route
            currentUser.displayName.isEmpty() -> Screen.Onboarding.route
            else -> Screen.Main.route
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                currentUser = currentUser
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Onboarding.route) {
            // Onboarding screen for profile setup
            // TODO: Implement onboarding screen
        }

        composable(Screen.Main.route) {
            MainScreen(
                currentUser = currentUser ?: return@composable,
                onSignOut = {
                    onSignOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")

    object Home : Screen("home")
    object Habits : Screen("habits")
    object Calendar : Screen("calendar")
    object Diary : Screen("diary")
    object Rank : Screen("rank")
    object Profile : Screen("profile")
}