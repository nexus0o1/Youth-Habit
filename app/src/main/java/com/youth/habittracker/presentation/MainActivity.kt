package com.youth.habittracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.youth.habittracker.presentation.auth.AuthViewModel
import com.youth.habittracker.presentation.navigation.HabitTrackerNavigation
import com.youth.habittracker.presentation.theme.YouthHabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouthHabitTrackerApp()
        }
    }
}

@Composable
fun YouthHabitTrackerApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val darkTheme = isSystemInDarkTheme()

    YouthHabitTrackerTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Set system UI colors
            systemUiController.setSystemBarsColor(
                color = MaterialTheme.colorScheme.surface,
                darkIcons = !darkTheme
            )

            HabitTrackerNavigation(
                navController = navController,
                currentUser = currentUser,
                onSignOut = { authViewModel.signOut() }
            )
        }
    }
}