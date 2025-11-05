package com.youth.habittracker.utils.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.youth.habittracker.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val hour = inputData.getInt(NotificationScheduler.HOUR, 20)
            val minute = inputData.getInt(NotificationScheduler.MINUTE, 0)

            // Get today's habit statistics
            val userId = getCurrentUserId()
            val todayEntries = habitRepository.getTodayEntries().getOrNull() ?: emptyList()
            val activeHabits = habitRepository.getActiveHabits().first()

            val completedCount = todayEntries.count { it.completed }
            val totalCount = activeHabits.size

            // Calculate current streak (simplified version)
            val currentStreak = calculateCurrentStreak(todayEntries)

            // Show daily summary notification
            notificationScheduler.showDailySummaryNotification(
                completedHabits = completedCount,
                totalHabits = totalCount,
                streak = currentStreak
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateCurrentStreak(entries: List<com.youth.habittracker.data.model.HabitEntry>): Int {
        // Simplified streak calculation
        // In a real implementation, you'd check consecutive days
        return if (entries.isNotEmpty()) 1 else 0
    }

    private suspend fun getCurrentUserId(): String {
        // This would typically come from your authentication system
        return "current_user_id"
    }
}