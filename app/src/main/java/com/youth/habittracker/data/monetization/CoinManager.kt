package com.youth.habittracker.data.monetization

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.youth.habittracker.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val _userCoins = MutableStateFlow(0)
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    private val _transactionHistory = MutableStateFlow<List<CoinTransaction>>(emptyList())
    val transactionHistory: StateFlow<List<CoinTransaction>> = _transactionHistory.asStateFlow()

    init {
        // Listen for user coin updates
        auth.currentUser?.uid?.let { userId ->
            listenForCoinUpdates(userId)
            loadTransactionHistory(userId)
        }
    }

    private fun listenForCoinUpdates(userId: String) {
        firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return
                val user = snapshot?.toObject(User::class.java)
                user?.let {
                    _userCoins.value = it.coins
                }
            }
    }

    private fun loadTransactionHistory(userId: String) {
        firestore.collection("coinTransactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return
                val transactions = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(CoinTransaction::class.java)?.copy(
                        transactionId = document.id
                    )
                } ?: emptyList()
                _transactionHistory.value = transactions
            }
    }

    suspend fun addCoins(
        amount: Int,
        source: CoinSource,
        description: String,
        relatedId: String? = null
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Add transaction record
            val transaction = CoinTransaction(
                userId = currentUserId,
                amount = amount,
                type = TransactionType.EARNED,
                source = source,
                description = description,
                relatedId = relatedId,
                timestamp = com.google.firebase.Timestamp.now()
            )

            firestore.collection("coinTransactions")
                .add(transaction)
                .await()

            // Update user coin balance
            val userRef = firestore.collection("users").document(currentUserId)
            firestore.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val currentCoins = userDoc.getLong("coins")?.toInt() ?: 0
                transaction.update(userRef, "coins", currentCoins + amount)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun spendCoins(
        amount: Int,
        source: CoinSource,
        description: String,
        relatedId: String? = null
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Check if user has enough coins
            val userDoc = firestore.collection("users").document(currentUserId).get().await()
            val currentCoins = userDoc.getLong("coins")?.toInt() ?: 0

            if (currentCoins < amount) {
                return Result.failure(Exception("Insufficient coins"))
            }

            // Add transaction record
            val transaction = CoinTransaction(
                userId = currentUserId,
                amount = -amount,
                type = TransactionType.SPENT,
                source = source,
                description = description,
                relatedId = relatedId,
                timestamp = com.google.firebase.Timestamp.now()
            )

            firestore.collection("coinTransactions")
                .add(transaction)
                .await()

            // Update user coin balance
            val userRef = firestore.collection("users").document(currentUserId)
            firestore.runTransaction { transaction ->
                transaction.update(userRef, "coins", currentCoins - amount)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendCheer(
        fromUserId: String,
        toUserId: String,
        message: String? = null
    ): Result<Unit> {
        return try {
            val CHEER_COST = 1

            // Spend coins from sender
            spendCoins(
                amount = CHEER_COST,
                source = CoinSource.CHEER_SENT,
                description = "Sent cheer to user",
                relatedId = toUserId
            ).getOrThrow()

            // Add coins to receiver
            addCoins(
                amount = CHEER_COST,
                source = CoinSource.CHEER_RECEIVED,
                description = "Received cheer from user",
                relatedId = fromUserId
            ).getOrThrow()

            // Update social data
            updateSocialCheerData(fromUserId, toUserId, message)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateSocialCheerData(fromUserId: String, toUserId: String, message: String?) {
        val batch = firestore.batch()

        // Update sender's social data
        val senderRef = firestore.collection("socialData").document(fromUserId)
        batch.update(senderRef, "cheers.totalSent", com.google.firebase.firestore.FieldValue.increment(1))
        batch.update(senderRef, "cheers.sentTo", com.google.firebase.firestore.FieldValue.arrayUnion(toUserId))

        // Update receiver's social data
        val receiverRef = firestore.collection("socialData").document(toUserId)
        batch.update(receiverRef, "cheers.totalReceived", com.google.firebase.firestore.FieldValue.increment(1))

        val cheerRecord = mapOf(
            "userId" to fromUserId,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "message" to message
        )
        batch.update(receiverRef, "cheers.receivedFrom", com.google.firebase.firestore.FieldValue.arrayUnion(cheerRecord))

        batch.commit().await()
    }

    suspend fun grantDailyStreakBonus(streakDays: Int): Result<Unit> {
        val bonusCoins = when {
            streakDays >= 100 -> 50
            streakDays >= 50 -> 25
            streakDays >= 30 -> 15
            streakDays >= 14 -> 10
            streakDays >= 7 -> 5
            else -> 0
        }

        return if (bonusCoins > 0) {
            addCoins(
                amount = bonusCoins,
                source = CoinSource.STREAK_BONUS,
                description = "$streakDays-day streak bonus!"
            )
        } else {
            Result.success(Unit)
        }
    }

    suspend fun grantWeeklyGoalBonus(weeklyCompletions: Int): Result<Unit> {
        val bonusCoins = when {
            weeklyCompletions >= 50 -> 20
            weeklyCompletions >= 30 -> 15
            weeklyCompletions >= 20 -> 10
            weeklyCompletions >= 10 -> 5
            else -> 0
        }

        return if (bonusCoins > 0) {
            addCoins(
                amount = bonusCoins,
                source = CoinSource.WEEKLY_BONUS,
                description = "Weekly goal completed: $weeklyCompletions habits!"
            )
        } else {
            Result.success(Unit)
        }
    }

    suspend fun grantAchievementBonus(achievementId: String, coins: Int): Result<Unit> {
        return addCoins(
            amount = coins,
            source = CoinSource.ACHIEVEMENT,
            description = "Achievement unlocked!",
            relatedId = achievementId
        )
    }

    suspend fun purchasePremiumFeature(
        feature: PremiumFeature,
        cost: Int
    ): Result<Unit> {
        return spendCoins(
            amount = cost,
            source = CoinSource.PREMIUM_FEATURE,
            description = "Purchased ${feature.displayName}"
        )
    }

    fun canAfford(cost: Int): Boolean {
        return _userCoins.value >= cost
    }

    fun getPurchaseStatus(feature: PremiumFeature): PurchaseStatus {
        return when {
            _userCoins.value >= feature.cost -> PurchaseStatus.AFFORDABLE
            _userCoins.value >= feature.cost * 0.8 -> PurchaseStatus.NEARLY_AFFORDABLE
            else -> PurchaseStatus.NOT_AFFORDABLE
        }
    }
}

data class CoinTransaction(
    val transactionId: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val type: TransactionType = TransactionType.EARNED,
    val source: CoinSource = CoinSource.AD_WATCH,
    val description: String = "",
    val relatedId: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null
)

enum class TransactionType {
    EARNED, SPENT
}

enum class CoinSource {
    AD_WATCH,
    STREAK_BONUS,
    WEEKLY_BONUS,
    ACHIEVEMENT,
    CHEER_SENT,
    CHEER_RECEIVED,
    PREMIUM_FEATURE,
    REFERRAL_BONUS,
    DAILY_BONUS
}

data class PremiumFeature(
    val id: String,
    val displayName: String,
    val description: String,
    val cost: Int,
    val isPermanent: Boolean = false,
    val durationDays: Int? = null
) {
    companion object {
        val CUSTOM_THEME_PACK = PremiumFeature(
            id = "custom_theme_pack",
            displayName = "Custom Theme Pack",
            description = "Unlock premium themes and colors",
            cost = 50,
            isPermanent = true
        )

        val ADVANCED_ANALYTICS = PremiumFeature(
            id = "advanced_analytics",
            displayName = "Advanced Analytics",
            description = "Detailed progress insights and trends",
            cost = 100,
            isPermanent = true
        )

        val UNLIMITED_REMINDERS = PremiumFeature(
            id = "unlimited_reminders",
            displayName = "Unlimited Reminders",
            description = "Set unlimited habit reminders",
            cost = 75,
            isPermanent = true
        )

        val DATA_EXPORT = PremiumFeature(
            id = "data_export",
            displayName = "Data Export",
            description = "Export all your data as PDF or CSV",
            cost = 25,
            isPermanent = true
        )

        val EXTRA_HABIT_SLOTS = PremiumFeature(
            id = "extra_habit_slots",
            displayName = "10 Extra Habit Slots",
            description = "Increase habit limit by 10",
            cost = 30,
            isPermanent = true
        )

        val ANONYMOUS_MODE = PremiumFeature(
            id = "anonymous_mode",
            displayName = "Anonymous Leaderboard Mode",
            description = "Hide your identity on leaderboards",
            cost = 40,
            isPermanent = true
        )
    }
}

enum class PurchaseStatus {
    AFFORDABLE, NEARLY_AFFORDABLE, NOT_AFFORDABLE
}