package com.youth.habittracker.presentation.habits

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youth.habittracker.data.model.Habit
import com.youth.habittracker.data.model.User
import com.youth.habittracker.presentation.common.UiState
import com.youth.habittracker.presentation.habits.components.HabitCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    currentUser: User,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val activeHabits by viewModel.activeHabits.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val deleteState by viewModel.deleteHabitState

    // State for navigation
    var showCreateHabit by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    // Handle delete state
    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) {
            // Show success message or handle as needed
            viewModel.clearDeleteHabitState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Habits",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Stats or filter options could go here
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateHabit = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (activeHabits.isEmpty()) {
                // Empty state
                EmptyHabitsState(
                    onCreateHabit = { showCreateHabit = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                // Habits list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Today's Habits",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(activeHabits) { habit ->
                        val todayEntry = viewModel.getTodayEntryForHabit(habit.habitId)

                        HabitCard(
                            habit = habit,
                            todayEntry = todayEntry,
                            onComplete = viewModel::completeHabit,
                            onSkip = viewModel::skipHabit,
                            onEdit = { editingHabit = it },
                            onDelete = viewModel::deleteHabit,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    // Create habit dialog
    if (showCreateHabit) {
        HabitFormDialog(
            habitId = null,
            onDismiss = { showCreateHabit = false },
            onSaveComplete = {
                showCreateHabit = false
            }
        )
    }

    // Edit habit dialog
    editingHabit?.let { habit ->
        HabitFormDialog(
            habitId = habit.habitId,
            onDismiss = { editingHabit = null },
            onSaveComplete = {
                editingHabit = null
            }
        )
    }
}

@Composable
private fun EmptyHabitsState(
    onCreateHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No habits yet!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first habit to start building better routines",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateHabit
        ) {
            Text("Create Your First Habit")
        }
    }
}

@Composable
private fun HabitFormDialog(
    habitId: String?,
    onDismiss: () -> Unit,
    onSaveComplete: () -> Unit
) {
    // This would typically use Navigation Component to navigate to a new screen
    // For now, we'll show a placeholder
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (habitId == null) "Create Habit" else "Edit Habit") },
        text = {
            Text("Habit form would appear here. This will be implemented with proper navigation.")
        },
        confirmButton = {
            TextButton(onClick = onSaveComplete) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}