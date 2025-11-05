package com.youth.habittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String?,
    val bio: String?,
    val timezone: String,
    val createdAt: Long,
    val lastActiveAt: Long,
    val subscriptionStatus: String,
    val coins: Int,
    val theme: String,
    val notifications: Boolean,
    val leaderboardOptIn: Boolean,
    val totalHabits: Int,
    val longestStreak: Int,
    val currentStreak: Int,
    val totalCompletions: Int,
    val weeklyCompletions: Int,
    val monthlyCompletions: Int
)