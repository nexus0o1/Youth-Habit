package com.youth.habittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val habitId: String,
    val userId: String,
    val name: String,
    val description: String?,
    val type: String,
    val category: String,
    val color: String,
    val icon: String,
    val frequency: String,
    val weekdays: String, // JSON string
    val timeOfDay: String?,
    val targetValue: Int?,
    val dailyGoal: Int?,
    val weeklyGoal: Int?,
    val monthlyGoal: Int?,
    val isActive: Boolean,
    val createdAt: Long,
    val archivedAt: Long?,
    val lastModified: Long
)