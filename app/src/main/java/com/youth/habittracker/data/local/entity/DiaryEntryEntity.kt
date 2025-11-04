package com.youth.habittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey
    val entryId: String,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val title: String?,
    val content: String,
    val primaryMood: String,
    val moodIntensity: Int,
    val secondaryMoods: String, // JSON string
    val moodNote: String?,
    val tags: String, // JSON string
    val photos: String, // JSON string
    val voiceMemoUrl: String?,
    val transcription: String?,
    val templateId: String?,
    val linkedHabitIds: String, // JSON string
    val isPrivate: Boolean,
    val weather: String?,
    val location: String?,
    val isSynced: Boolean,
    val lastModified: Long
)