package com.youth.habittracker.presentation.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youth.habittracker.data.model.Habit
import com.youth.habittracker.data.model.HabitEntry
import com.youth.habittracker.data.repository.HabitRepository
import com.youth.habittracker.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _activeHabits = MutableStateFlow<List<Habit>>(emptyList())
    val activeHabits: StateFlow<List<Habit>> = _activeHabits.asStateFlow()

    private val _archivedHabits = MutableStateFlow<List<Habit>>(emptyList())
    val archivedHabits: StateFlow<List<Habit>> = _archivedHabits.asStateFlow()

    private val _habitEntries = MutableStateFlow<Map<String, List<HabitEntry>>>(emptyMap())
    val habitEntries: StateFlow<Map<String, List<HabitEntry>>> = _habitEntries.asStateFlow()

    private val _createHabitState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val createHabitState: StateFlow<UiState<String>> = _createHabitState.asStateFlow()

    private val _updateHabitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateHabitState: StateFlow<UiState<Unit>> = _updateHabitState.asStateFlow()

    private val _deleteHabitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteHabitState: StateFlow<UiState<Unit>> = _deleteHabitState.asStateFlow()

    private val _todayEntries = MutableStateFlow<List<HabitEntry>>(emptyList())
    val todayEntries: StateFlow<List<HabitEntry>> = _todayEntries.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine active habits with today's entries
            combine(
                habitRepository.getActiveHabits(),
                habitRepository.getTodayEntries()
            ) { habits, result ->
                _activeHabits.value = habits
                if (result.isSuccess) {
                    _todayEntries.value = result.getOrNull() ?: emptyList()
                }
            }
        }

        viewModelScope.launch {
            habitRepository.getArchivedHabits().collect { habits ->
                _archivedHabits.value = habits
            }
        }
    }

    fun createHabit(habit: Habit) {
        viewModelScope.launch {
            _createHabitState.value = UiState.Loading
            habitRepository.createHabit(habit)
                .onSuccess { habitId ->
                    _createHabitState.value = UiState.Success(habitId)
                }
                .onFailure { exception ->
                    _createHabitState.value = UiState.Error(exception.message ?: "Failed to create habit")
                }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            _updateHabitState.value = UiState.Loading
            habitRepository.updateHabit(habit)
                .onSuccess {
                    _updateHabitState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _updateHabitState.value = UiState.Error(exception.message ?: "Failed to update habit")
                }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            _deleteHabitState.value = UiState.Loading
            habitRepository.deleteHabit(habitId)
                .onSuccess {
                    _deleteHabitState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _deleteHabitState.value = UiState.Error(exception.message ?: "Failed to delete habit")
                }
        }
    }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.archiveHabit(habitId)
        }
    }

    fun unarchiveHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.unarchiveHabit(habitId)
        }
    }

    fun completeHabit(habitId: String, notes: String? = null, value: Int? = null, duration: Int? = null) {
        viewModelScope.launch {
            val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val entry = HabitEntry(
                habitId = habitId,
                date = com.google.firebase.Timestamp(today / 1000, 0),
                completed = true,
                completedAt = com.google.firebase.Timestamp.now(),
                notes = notes,
                value = value,
                duration = duration
            )

            habitRepository.createHabitEntry(entry)
                .onSuccess {
                    // Refresh today's entries
                    refreshTodayEntries()
                }
        }
    }

    fun skipHabit(habitId: String) {
        viewModelScope.launch {
            val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val entry = HabitEntry(
                habitId = habitId,
                date = com.google.firebase.Timestamp(today / 1000, 0),
                completed = false
            )

            habitRepository.createHabitEntry(entry)
                .onSuccess {
                    // Refresh today's entries
                    refreshTodayEntries()
                }
        }
    }

    fun updateHabitEntry(entry: HabitEntry) {
        viewModelScope.launch {
            habitRepository.updateHabitEntry(entry)
        }
    }

    private fun refreshTodayEntries() {
        viewModelScope.launch {
            habitRepository.getTodayEntries()
                .onSuccess { entries ->
                    _todayEntries.value = entries
                }
        }
    }

    fun getTodayEntryForHabit(habitId: String): HabitEntry? {
        return _todayEntries.value.find { it.habitId == habitId }
    }

    fun clearCreateHabitState() {
        _createHabitState.value = UiState.Idle
    }

    fun clearUpdateHabitState() {
        _updateHabitState.value = UiState.Idle
    }

    fun clearDeleteHabitState() {
        _deleteHabitState.value = UiState.Idle
    }
}