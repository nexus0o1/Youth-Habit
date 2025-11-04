package com.youth.habittracker.data.model

import com.google.firebase.Timestamp

data class Habit(
    val habitId: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String? = null,
    val type: HabitType = HabitType.YES_NO,
    val category: String = "",
    val color: String = "#6366f1",
    val icon: String = "✓",
    val schedule: HabitSchedule = HabitSchedule(),
    val goals: HabitGoals = HabitGoals(),
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val archivedAt: Timestamp? = null
)

data class HabitSchedule(
    val frequency: Frequency = Frequency.DAILY,
    val weekdays: List<Int> = emptyList(), // 0-6 (Sunday-Saturday)
    val timeOfDay: String? = null, // HH:mm format
    val targetValue: Int? = null,
    val customDays: List<String> = emptyList() // For custom schedules
)

data class HabitGoals(
    val daily: Int? = null,
    val weekly: Int? = null,
    val monthly: Int? = null
)

enum class HabitType {
    YES_NO,        // Simple completion tracking
    DURATION,      // Time-based tracking
    COUNT,         // Numeric targets
    TIMED,         // Scheduled at specific times
    MEASUREMENT    // Track metrics
}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}