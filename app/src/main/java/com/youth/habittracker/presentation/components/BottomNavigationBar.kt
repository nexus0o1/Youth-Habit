package com.youth.habittracker.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.youth.habittracker.R
import com.youth.habittracker.presentation.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Home,
        Screen.Habits,
        Screen.Calendar,
        Screen.Diary,
        Screen.Rank,
        Screen.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        screens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            BottomNavigationItem(
                icon = { getIconForScreen(screen) },
                label = { Text(stringResource(getLabelForScreen(screen))) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun getIconForScreen(screen: Screen): @Composable (() -> Unit) {
    return when (screen) {
        Screen.Home -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Home, contentDescription = null) }
        }
        Screen.Habits -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle, contentDescription = null) }
        }
        Screen.Calendar -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.CalendarToday, contentDescription = null) }
        }
        Screen.Diary -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Book, contentDescription = null) }
        }
        Screen.Rank -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Leaderboard, contentDescription = null) }
        }
        Screen.Profile -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Person, contentDescription = null) }
        }
        else -> {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Home, contentDescription = null) }
        }
    }
}

private fun getLabelForScreen(screen: Screen): Int {
    return when (screen) {
        Screen.Home -> R.string.nav_home
        Screen.Habits -> R.string.nav_habits
        Screen.Calendar -> R.string.nav_calendar
        Screen.Diary -> R.string.nav_diary
        Screen.Rank -> R.string.nav_rank
        Screen.Profile -> R.string.nav_profile
        else -> R.string.nav_home
    }
}