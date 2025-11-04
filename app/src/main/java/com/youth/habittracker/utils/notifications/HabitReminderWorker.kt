package com.youth.habittracker.utils.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.youth.habittracker.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler,
    private val habitRepository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val habitId = inputData.getString(NotificationScheduler.HABIT_ID) ?: return Result.failure()
            val habitName = inputData.getString(NotificationScheduler.HABIT_NAME) ?: return Result.failure()
            val hour = inputData.getInt(NotificationScheduler.HOUR, 0)
            val minute = inputData.getInt(NotificationScheduler.MINUTE, 0)
            val dayOfWeek = inputData.getInt(NotificationScheduler.DAY_OF_WEEK, 0)

            // Check if today is the scheduled day
            val today = LocalDateTime.now()
            val todayDayOfWeek = today.dayOfWeek.value % 7 // Convert to 0-6 (Sunday-Saturday)

            if (todayDayOfWeek == dayOfWeek) {
                // Check if the current time matches or is close to the scheduled time
                val scheduledTime = today.withHour(hour).withMinute(minute)
                val currentTime = today

                // Allow a 15-minute window for the notification
                val timeDifference = kotlin.math.abs(currentTime.minute - scheduledTime.minute)

                if (currentTime.hour == scheduledTime.hour && timeDifference <= 15) {
                    // Check if habit is already completed today
                    val todayStart = today.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000

                    val isCompletedToday = habitRepository.getEntriesInDateRange(
                        userId = getCurrentUserId(),
                        startDate = todayStart,
                        endDate = todayStart + 24 * 60 * 60 // Next day
                    ).getOrNull()?.any { entry ->
                        entry.habitId == habitId && entry.completed
                    } ?: false

                    if (!isCompletedToday) {
                        // Show notification
                        notificationScheduler.showHabitReminderNotification(habitId, habitName)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCurrentUserId(): String {
        // This would typically come from your authentication system
        // For now, return a placeholder or get it from a preference
        return "current_user_id"
    }
}