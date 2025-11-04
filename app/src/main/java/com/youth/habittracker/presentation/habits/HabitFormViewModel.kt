package com.youth.habittracker.presentation.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youth.habittracker.data.model.*
import com.youth.habittracker.data.repository.HabitRepository
import com.youth.habittracker.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitFormViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _habitState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val habitState: StateFlow<UiState<String>> = _habitState.asStateFlow()

    private val _habitFormData = MutableStateFlow(HabitFormData())
    val habitFormData: StateFlow<HabitFormData> = _habitFormData.asStateFlow()

    fun updateHabitName(name: String) {
        _habitFormData.value = _habitFormData.value.copy(name = name)
    }

    fun updateHabitDescription(description: String) {
        _habitFormData.value = _habitFormData.value.copy(description = description)
    }

    fun updateHabitType(type: HabitType) {
        _habitFormData.value = _habitFormData.value.copy(type = type)
    }

    fun updateHabitCategory(category: String) {
        _habitFormData.value = _habitFormData.value.copy(category = category)
    }

    fun updateHabitColor(color: String) {
        _habitFormData.value = _habitFormData.value.copy(color = color)
    }

    fun updateHabitIcon(icon: String) {
        _habitFormData.value = _habitFormData.value.copy(icon = icon)
    }

    fun updateFrequency(frequency: Frequency) {
        _habitFormData.value = _habitFormData.value.copy(
            schedule = _habitFormData.value.schedule.copy(frequency = frequency)
        )
    }

    fun updateWeekdays(weekdays: List<Int>) {
        _habitFormData.value = _habitFormData.value.copy(
            schedule = _habitFormData.value.schedule.copy(weekdays = weekdays)
        )
    }

    fun updateTimeOfDay(time: String?) {
        _habitFormData.value = _habitFormData.value.copy(
            schedule = _habitFormData.value.schedule.copy(timeOfDay = time)
        )
    }

    fun updateTargetValue(value: Int?) {
        _habitFormData.value = _habitFormData.value.copy(
            schedule = _habitFormData.value.schedule.copy(targetValue = value)
        )
    }

    fun updateDailyGoal(goal: Int?) {
        _habitFormData.value = _habitFormData.value.copy(
            goals = _habitFormData.value.goals.copy(daily = goal)
        )
    }

    fun updateWeeklyGoal(goal: Int?) {
        _habitFormData.value = _habitFormData.value.copy(
            goals = _habitFormData.value.goals.copy(weekly = goal)
        )
    }

    fun updateMonthlyGoal(goal: Int?) {
        _habitFormData.value = _habitFormData.value.copy(
            goals = _habitFormData.value.goals.copy(monthly = goal)
        )
    }

    fun createHabit() {
        val formData = _habitFormData.value
        if (!isValidHabitData(formData)) {
            _habitState.value = UiState.Error("Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _habitState.value = UiState.Loading

            val habit = Habit(
                name = formData.name,
                description = formData.description,
                type = formData.type,
                category = formData.category,
                color = formData.color,
                icon = formData.icon,
                schedule = formData.schedule,
                goals = formData.goals,
                isActive = true
            )

            habitRepository.createHabit(habit)
                .onSuccess { habitId ->
                    _habitState.value = UiState.Success(habitId)
                }
                .onFailure { exception ->
                    _habitState.value = UiState.Error(exception.message ?: "Failed to create habit")
                }
        }
    }

    fun updateHabit(habitId: String) {
        val formData = _habitFormData.value
        if (!isValidHabitData(formData)) {
            _habitState.value = UiState.Error("Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _habitState.value = UiState.Loading

            val habit = Habit(
                habitId = habitId,
                name = formData.name,
                description = formData.description,
                type = formData.type,
                category = formData.category,
                color = formData.color,
                icon = formData.icon,
                schedule = formData.schedule,
                goals = formData.goals,
                isActive = true
            )

            habitRepository.updateHabit(habit)
                .onSuccess {
                    _habitState.value = UiState.Success(habitId)
                }
                .onFailure { exception ->
                    _habitState.value = UiState.Error(exception.message ?: "Failed to update habit")
                }
        }
    }

    fun loadHabitForEdit(habitId: String) {
        viewModelScope.launch {
            habitRepository.getHabitById(habitId)
                .onSuccess { habit ->
                    habit?.let {
                        _habitFormData.value = HabitFormData(
                            name = it.name,
                            description = it.description ?: "",
                            type = it.type,
                            category = it.category,
                            color = it.color,
                            icon = it.icon,
                            schedule = it.schedule,
                            goals = it.goals
                        )
                    }
                }
        }
    }

    fun resetForm() {
        _habitFormData.value = HabitFormData()
        _habitState.value = UiState.Idle
    }

    private fun isValidHabitData(formData: HabitFormData): Boolean {
        return formData.name.isNotBlank() &&
                formData.category.isNotBlank() &&
                formData.type != null
    }

    fun clearState() {
        _habitState.value = UiState.Idle
    }
}

data class HabitFormData(
    val name: String = "",
    val description: String = "",
    val type: HabitType = HabitType.YES_NO,
    val category: String = "",
    val color: String = "#6366f1",
    val icon: String = "✓",
    val schedule: HabitSchedule = HabitSchedule(),
    val goals: HabitGoals = HabitGoals()
)