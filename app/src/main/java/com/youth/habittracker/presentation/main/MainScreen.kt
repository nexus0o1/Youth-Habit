package com.youth.habittracker.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.youth.habittracker.data.model.User
import com.youth.habittracker.presentation.calendar.CalendarScreen
import com.youth.habittracker.presentation.diary.DiaryScreen
import com.youth.habittracker.presentation.habits.HabitsScreen
import com.youth.habittracker.presentation.home.HomeScreen
import com.youth.habittracker.presentation.leaderboard.LeaderboardScreen
import com.youth.habittracker.presentation.navigation.Screen
import com.youth.habittracker.presentation.profile.ProfileScreen
import com.youth.habittracker.presentation.components.BottomNavigationBar

@Composable
fun MainScreen(
    currentUser: User,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    currentUser = currentUser,
                    onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                    onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(Screen.Habits.route) {
                HabitsScreen(
                    currentUser = currentUser
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    currentUser = currentUser
                )
            }

            composable(Screen.Diary.route) {
                DiaryScreen(
                    currentUser = currentUser
                )
            }

            composable(Screen.Rank.route) {
                LeaderboardScreen(
                    currentUser = currentUser
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    currentUser = currentUser,
                    onSignOut = onSignOut
                )
            }
        }
    }
}