package com.youth.habittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_entries")
data class HabitEntryEntity(
    @PrimaryKey
    val entryId: String,
    val habitId: String,
    val userId: String,
    val date: Long,
    val completed: Boolean,
    val value: Int?,
    val duration: Int?,
    val completedAt: Long?,
    val notes: String?,
    val attachments: String, // JSON string
    val mood: String?,
    val difficulty: Int?,
    val satisfaction: Int?,
    val isSynced: Boolean,
    val lastModified: Long
)