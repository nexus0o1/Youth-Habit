package com.youth.habittracker.data.model

import com.google.firebase.Timestamp

data class HabitEntry(
    val entryId: String = "",
    val habitId: String = "",
    val userId: String = "",
    val date: Timestamp? = null,
    val completed: Boolean = false,
    val value: Int? = null, // For count/measurement habits
    val duration: Int? = null, // For duration habits (minutes)
    val completedAt: Timestamp? = null,
    val notes: String? = null,
    val attachments: List<String> = emptyList(), // Storage URLs
    val mood: String? = null, // Mood emoji
    val difficulty: Int? = null, // 1-5 scale
    val satisfaction: Int? = null // 1-5 scale
)

data class DiaryEntry(
    val entryId: String = "",
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val title: String? = null,
    val content: String = "",
    val mood: MoodData = MoodData(),
    val tags: List<String> = emptyList(),
    val photos: List<String> = emptyList(), // Storage URLs
    val voiceMemoUrl: String? = null,
    val transcription: String? = null,
    val templateId: String? = null,
    val linkedHabitIds: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val weather: String? = null,
    val location: String? = null
)

data class MoodData(
    val primary: String = "", // emoji
    val intensity: Int = 5, // 1-10
    val secondary: List<String> = emptyList(),
    val note: String? = null
)

data class Template(
    val templateId: String = "",
    val name: String = "",
    val description: String = "",
    val prompts: List<String> = emptyList(),
    val category: String = "",
    val isBuiltIn: Boolean = false,
    val createdBy: String? = null,
    val usageCount: Int = 0,
    val isPublic: Boolean = false,
    val createdAt: Timestamp? = null
)