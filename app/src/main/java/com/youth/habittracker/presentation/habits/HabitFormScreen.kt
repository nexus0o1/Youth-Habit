package com.youth.habittracker.presentation.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youth.habittracker.data.model.*
import com.youth.habittracker.presentation.common.UiState
import com.youth.habittracker.presentation.habits.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormScreen(
    habitId: String? = null,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel()
) {
    val formData by viewModel.habitFormData.collectAsState()
    val habitState by viewModel.habitState
    val scrollState = rememberScrollState()

    LaunchedEffect(habitId) {
        habitId?.let {
            viewModel.loadHabitForEdit(it)
        }
    }

    LaunchedEffect(habitState) {
        if (habitState is UiState.Success) {
            onSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == null) "Create Habit" else "Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (habitId == null) {
                                viewModel.createHabit()
                            } else {
                                viewModel.updateHabit(habitId)
                            }
                        },
                        enabled = habitState !is UiState.Loading
                    ) {
                        if (habitState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Basic Information Section
            Text(
                text = "Basic Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = formData.name,
                onValueChange = viewModel::updateHabitName,
                label = { Text("Habit Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formData.name.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formData.description,
                onValueChange = viewModel::updateHabitDescription,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Habit Type Section
            Text(
                text = "Habit Type",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(modifier = Modifier.selectableGroup()) {
                HabitType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = formData.type == type,
                            onClick = { viewModel.updateHabitType(type) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getHabitTypeDisplayName(type),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Section
            Text(
                text = "Category",
                fontSize = 20.sp,
                FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HabitCategorySelector(
                selectedCategory = formData.category,
                onCategorySelected = viewModel::updateHabitCategory
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color and Icon Section
            Text(
                text = "Appearance",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HabitColorSelector(
                selectedColor = formData.color,
                onColorSelected = viewModel::updateHabitColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            HabitIconSelector(
                selectedIcon = formData.icon,
                selectedColor = Color(android.graphics.Color.parseColor(formData.color)),
                onIconSelected = viewModel::updateHabitIcon
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Schedule Section
            Text(
                text = "Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FrequencySelector(
                selectedFrequency = formData.schedule.frequency,
                selectedWeekdays = formData.schedule.weekdays,
                onFrequencySelected = { frequency ->
                    viewModel.updateFrequency(frequency)
                },
                onWeekdaysSelected = viewModel::updateWeekdays
            )

            Spacer(modifier = Modifier.height(16.dp))

            TimeSelector(
                selectedTime = formData.schedule.timeOfDay,
                onTimeSelected = viewModel::updateTimeOfDay
            )

            // Show target value for relevant habit types
            if (formData.type in listOf(HabitType.COUNT, HabitType.MEASUREMENT)) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = formData.schedule.targetValue?.toString() ?: "",
                    onValueChange = { value ->
                        viewModel.updateTargetValue(value.toIntOrNull())
                    },
                    label = { Text("Target Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goals Section
            Text(
                text = "Goals (Optional)",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formData.goals.daily?.toString() ?: "",
                    onValueChange = { value ->
                        viewModel.updateDailyGoal(value.toIntOrNull())
                    },
                    label = { Text("Daily") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formData.goals.weekly?.toString() ?: "",
                    onValueChange = { value ->
                        viewModel.updateWeeklyGoal(value.toIntOrNull())
                    },
                    label = { Text("Weekly") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formData.goals.monthly?.toString() ?: "",
                    onValueChange = { value ->
                        viewModel.updateMonthlyGoal(value.toIntOrNull())
                    },
                    label = { Text("Monthly") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            if (habitState is UiState.Error) {
                Text(
                    text = habitState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Save Button
            Button(
                onClick = {
                    if (habitId == null) {
                        viewModel.createHabit()
                    } else {
                        viewModel.updateHabit(habitId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = habitState !is UiState.Loading && formData.name.isNotBlank()
            ) {
                if (habitState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (habitId == null) "Create Habit" else "Update Habit")
                }
            }
        }
    }
}

@Composable
private fun getHabitTypeDisplayName(type: HabitType): String {
    return when (type) {
        HabitType.YES_NO -> "Yes/No (Simple completion)"
        HabitType.DURATION -> "Duration (Time-based)"
        HabitType.COUNT -> "Count (Number-based)"
        HabitType.TIMED -> "Timed (Scheduled)"
        HabitType.MEASUREMENT -> "Measurement (Track metrics)"
    }
}