package com.youth.habittracker.data.model

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val timezone: String = "UTC",
    val createdAt: Timestamp? = null,
    val lastActiveAt: Timestamp? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.FREE,
    val coins: Int = 0,
    val preferences: UserPreferences = UserPreferences(),
    val statistics: UserStatistics = UserStatistics()
)

data class UserPreferences(
    val theme: Theme = Theme.LIGHT,
    val notifications: Boolean = true,
    val leaderboardOptIn: Boolean = true,
    val reminderTime: String = "09:00",
    val weekendReminders: Boolean = true
)

data class UserStatistics(
    val totalHabits: Int = 0,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0,
    val totalCompletions: Int = 0,
    val weeklyCompletions: Int = 0,
    val monthlyCompletions: Int = 0
)

enum class SubscriptionStatus {
    FREE, PREMIUM
}

enum class Theme {
    LIGHT, DARK, AUTO
}