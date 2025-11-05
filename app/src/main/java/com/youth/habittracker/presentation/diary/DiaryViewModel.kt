package com.youth.habittracker.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youth.habittracker.data.model.DiaryEntry
import com.youth.habittracker.data.model.MoodData
import com.youth.habittracker.data.model.Template
import com.youth.habittracker.data.repository.DiaryRepository
import com.youth.habittracker.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = _diaryEntries.asStateFlow()

    private val _templates = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = _templates.asStateFlow()

    private val _createEntryState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val createEntryState: StateFlow<UiState<String>> = _createEntryState.asStateFlow()

    private val _updateEntryState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateEntryState: StateFlow<UiState<Unit>> = _updateEntryState.asStateFlow()

    private val _deleteEntryState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteEntryState: StateFlow<UiState<Unit>> = _deleteEntryState.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<DiaryEntry>>>(UiState.Idle)
    val searchState: StateFlow<UiState<List<DiaryEntry>>> = _searchState.asStateFlow()

    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry.asStateFlow()

    init {
        viewModelScope.launch {
            diaryRepository.getAllDiaryEntries().collect { entries ->
                _diaryEntries.value = entries
            }
        }

        viewModelScope.launch {
            diaryRepository.getBuiltInTemplates().collect { builtInTemplates ->
                // Combine with user templates
                diaryRepository.getUserTemplates().collect { userTemplates ->
                    _templates.value = builtInTemplates + userTemplates
                }
            }
        }
    }

    fun createDiaryEntry(
        title: String?,
        content: String,
        mood: MoodData,
        tags: List<String>,
        isPrivate: Boolean,
        linkedHabitIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _createEntryState.value = UiState.Loading

            val now = Instant.now()
            val entry = DiaryEntry(
                title = title,
                content = content,
                mood = mood,
                tags = tags,
                isPrivate = isPrivate,
                linkedHabitIds = linkedHabitIds,
                createdAt = com.google.firebase.Timestamp(now),
                updatedAt = com.google.firebase.Timestamp(now)
            )

            diaryRepository.createDiaryEntry(entry)
                .onSuccess { entryId ->
                    _createEntryState.value = UiState.Success(entryId)
                }
                .onFailure { exception ->
                    _createEntryState.value = UiState.Error(exception.message ?: "Failed to create entry")
                }
        }
    }

    fun updateDiaryEntry(
        entryId: String,
        title: String?,
        content: String,
        mood: MoodData,
        tags: List<String>,
        isPrivate: Boolean,
        linkedHabitIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _updateEntryState.value = UiState.Loading

            val entry = DiaryEntry(
                entryId = entryId,
                title = title,
                content = content,
                mood = mood,
                tags = tags,
                isPrivate = isPrivate,
                linkedHabitIds = linkedHabitIds,
                updatedAt = com.google.firebase.Timestamp.now()
            )

            diaryRepository.updateDiaryEntry(entry)
                .onSuccess {
                    _updateEntryState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _updateEntryState.value = UiState.Error(exception.message ?: "Failed to update entry")
                }
        }
    }

    fun deleteDiaryEntry(entryId: String) {
        viewModelScope.launch {
            _deleteEntryState.value = UiState.Loading

            diaryRepository.deleteDiaryEntry(entryId)
                .onSuccess {
                    _deleteEntryState.value = UiState.Success(Unit)
                    if (_selectedEntry.value?.entryId == entryId) {
                        _selectedEntry.value = null
                    }
                }
                .onFailure { exception ->
                    _deleteEntryState.value = UiState.Error(exception.message ?: "Failed to delete entry")
                }
        }
    }

    fun loadDiaryEntry(entryId: String) {
        viewModelScope.launch {
            diaryRepository.getDiaryEntryById(entryId)
                .onSuccess { entry ->
                    _selectedEntry.value = entry
                }
                .onFailure { exception ->
                    // Handle error
                }
        }
    }

    fun searchEntries(query: String) {
        if (query.isBlank()) {
            _searchState.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            _searchState.value = UiState.Loading

            diaryRepository.searchEntries(query)
                .onSuccess { entries ->
                    _searchState.value = UiState.Success(entries)
                }
                .onFailure { exception ->
                    _searchState.value = UiState.Error(exception.message ?: "Search failed")
                }
        }
    }

    fun uploadPhoto(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            diaryRepository.uploadPhoto(imageBytes, fileName)
                .onSuccess { url ->
                    // Handle success - update entry with photo URL
                }
                .onFailure { exception ->
                    // Handle error
                }
        }
    }

    fun uploadVoiceRecording(audioBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            diaryRepository.uploadVoiceRecording(audioBytes, fileName)
                .onSuccess { url ->
                    // Handle success - update entry with voice memo URL
                }
                .onFailure { exception ->
                    // Handle error
                }
        }
    }

    fun getEntriesForDate(date: LocalDate): List<DiaryEntry> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        return _diaryEntries.value.filter { entry ->
            val entryTime = entry.createdAt?.toDate()?.toInstant()
            entryTime != null && entryTime >= startOfDay && entryTime < endOfDay
        }
    }

    fun getEntriesByMood(mood: String): List<DiaryEntry> {
        return _diaryEntries.value.filter { entry ->
            entry.mood.primary == mood
        }
    }

    fun getEntriesByTag(tag: String): List<DiaryEntry> {
        return _diaryEntries.value.filter { entry ->
            entry.tags.contains(tag)
        }
    }

    fun getAllTags(): List<String> {
        return _diaryEntries.value
            .flatMap { entry -> entry.tags }
            .distinct()
    }

    fun getAllMoods(): List<String> {
        return _diaryEntries.value
            .map { entry -> entry.mood.primary }
            .distinct()
    }

    fun getEntryCount(): Int {
        return _diaryEntries.value.size
    }

    fun getCurrentStreak(): Int {
        val sortedEntries = _diaryEntries.value
            .sortedByDescending { it.createdAt?.toDate()?.toInstant() }
            .mapNotNull { entry -> entry.createdAt?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() }
            .distinct()

        if (sortedEntries.isEmpty()) return 0

        var streak = 0
        var currentDate = LocalDate.now()

        for (entryDate in sortedEntries) {
            if (entryDate == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (entryDate.isAfter(currentDate)) {
                continue // Skip future dates (shouldn't happen but just in case)
            } else {
                break
            }
        }

        return streak
    }

    fun clearCreateEntryState() {
        _createEntryState.value = UiState.Idle
    }

    fun clearUpdateEntryState() {
        _updateEntryState.value = UiState.Idle
    }

    fun clearDeleteEntryState() {
        _deleteEntryState.value = UiState.Idle
    }

    fun clearSearchState() {
        _searchState.value = UiState.Idle
    }

    fun clearSelectedEntry() {
        _selectedEntry.value = null
    }
}