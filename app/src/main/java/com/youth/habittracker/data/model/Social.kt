package com.youth.habittracker.data.model

import com.google.firebase.Timestamp

data class SocialData(
    val userId: String = "",
    val cheers: CheerData = CheerData(),
    val comments: List<Comment> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val leaderboardData: LeaderboardData = LeaderboardData(),
    val friends: List<String> = emptyList(), // User IDs
    val blockedUsers: List<String> = emptyList(), // User IDs
    val privacySettings: PrivacySettings = PrivacySettings()
)

data class CheerData(
    val sentTo: List<String> = emptyList(),
    val receivedFrom: List<CheerRecord> = emptyList(),
    val totalSent: Int = 0,
    val totalReceived: Int = 0
)

data class CheerRecord(
    val userId: String = "",
    val timestamp: Timestamp? = null,
    val message: String? = null
)

data class Comment(
    val commentId: String = "",
    val entryId: String = "",
    val userId: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null,
    val likes: List<String> = emptyList(), // User IDs who liked
    val replies: List<Comment> = emptyList(),
    val isReported: Boolean = false,
    val isDeleted: Boolean = false
)

data class Achievement(
    val achievementId: String = "",
    val type: AchievementType = AchievementType.STREAK,
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val unlockedAt: Timestamp? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val rarity: AchievementRarity = AchievementRarity.COMMON,
    val points: Int = 0
)

enum class AchievementType {
    STREAK,
    COMPLETION,
    CONSISTENCY,
    SOCIAL,
    MILESTONE,
    SPECIAL
}

enum class AchievementRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

data class LeaderboardData(
    val weeklyScore: Int = 0,
    val monthlyScore: Int = 0,
    val allTimeScore: Int = 0,
    val rank: RankingData = RankingData(),
    val level: Int = 1,
    val experiencePoints: Int = 0,
    val experienceToNextLevel: Int = 100
)

data class RankingData(
    val daily: Int = 0,
    val weekly: Int = 0,
    val monthly: Int = 0,
    val allTime: Int = 0
)

data class PrivacySettings(
    val profileVisibility: PrivacyLevel = PrivacyLevel.PUBLIC,
    val leaderboardOptIn: Boolean = true,
    val allowCheers: Boolean = true,
    val allowComments: Boolean = true,
    val showStreaks: Boolean = true,
    val showHabits: Boolean = false,
    val anonymousMode: Boolean = false
)

enum class PrivacyLevel {
    PUBLIC, FRIENDS, PRIVATE
}