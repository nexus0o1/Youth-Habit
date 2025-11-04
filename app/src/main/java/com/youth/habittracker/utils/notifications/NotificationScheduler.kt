package com.youth.habittracker.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.youth.habittracker.R
import com.youth.habittracker.data.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) {

    companion object {
        const val HABIT_REMINDER_CHANNEL_ID = "habit_reminders"
        const val HABIT_REMINDER_CHANNEL_NAME = "Habit Reminders"
        const val HABIT_REMINDER_CHANNEL_DESCRIPTION = "Notifications for your habit reminders"

        const val STREAK_ACHIEVEMENT_CHANNEL_ID = "streak_achievements"
        const val STREAK_ACHIEVEMENT_CHANNEL_NAME = "Streak Achievements"
        const val STREAK_ACHIEVEMENT_CHANNEL_DESCRIPTION = "Notifications for streak milestones"

        const val DAILY_SUMMARY_CHANNEL_ID = "daily_summary"
        const val DAILY_SUMMARY_CHANNEL_NAME = "Daily Summary"
        const val DAILY_SUMMARY_CHANNEL_DESCRIPTION = "Daily habit summary notifications"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Habit reminders channel
            val habitChannel = NotificationChannel(
                HABIT_REMINDER_CHANNEL_ID,
                HABIT_REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = HABIT_REMINDER_CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            // Streak achievements channel
            val streakChannel = NotificationChannel(
                STREAK_ACHIEVEMENT_CHANNEL_ID,
                STREAK_ACHIEVEMENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = STREAK_ACHIEVEMENT_CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            // Daily summary channel
            val summaryChannel = NotificationChannel(
                DAILY_SUMMARY_CHANNEL_ID,
                DAILY_SUMMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = DAILY_SUMMARY_CHANNEL_DESCRIPTION
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(listOf(habitChannel, streakChannel, summaryChannel))
        }
    }

    fun scheduleHabitReminder(habit: Habit) {
        if (habit.schedule.timeOfDay == null) return

        val (hour, minute) = parseTime(habit.schedule.timeOfDay!!)

        // Create daily work request for each day the habit should trigger
        habit.schedule.weekdays.forEach { dayOfWeek ->
            val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
                1, TimeUnit.DAYS
            ).apply {
                setInputData(
                    workDataOf(
                        HABIT_ID to habit.habitId,
                        HABIT_NAME to habit.name,
                        HOUR to hour,
                        MINUTE to minute,
                        DAY_OF_WEEK to dayOfWeek
                    )
                )
                addTag(HABIT_REMINDER_WORK_TAG)
                addTag("habit_${habit.habitId}")
                setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
            }.build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "habit_reminder_${habit.habitId}_$dayOfWeek",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    fun cancelHabitReminder(habitId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_$habitId")
    }

    fun scheduleDailySummary() {
        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            1, TimeUnit.DAYS
        ).apply {
            setInputData(
                workDataOf(
                    HOUR to 20, // 8 PM
                    MINUTE to 0
                )
            )
            addTag(DAILY_SUMMARY_WORK_TAG)
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
        }.build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_summary",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun showStreakAchievementNotification(streakDays: Int, habitName: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "STREAK_ACHIEVEMENT"
            putExtra("streak_days", streakDays)
            putExtra("habit_name", habitName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            streakDays,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, STREAK_ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎉 Streak Milestone!")
            .setContentText("Congratulations! You've reached a $streakDays-day streak for $habitName!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Amazing work! You've maintained your $habitName habit for $streakDays consecutive days. Keep up the great work!"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification,
                "Share",
                createShareIntent(streakDays, habitName)
            )
            .build()

        notificationManager.notify(streakDays, notification)
    }

    fun showHabitReminderNotification(habitId: String, habitName: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "HABIT_REMINDER"
            putExtra("habit_id", habitId)
            putExtra("habit_name", habitName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HABIT_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Habit Reminder")
            .setContentText("Time to complete: $habitName")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Don't forget to complete your habit: $habitName. Tap to mark it as complete!"
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification,
                "Complete",
                createCompleteHabitIntent(habitId)
            )
            .addAction(
                R.drawable.ic_notification,
                "Skip",
                createSkipHabitIntent(habitId)
            )
            .build()

        notificationManager.notify(habitId.hashCode(), notification)
    }

    fun showDailySummaryNotification(completedHabits: Int, totalHabits: Int, streak: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "DAILY_SUMMARY"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0

        val notification = NotificationCompat.Builder(context, DAILY_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Habit Summary")
            .setContentText("You completed $completedHabits of $totalHabits habits today")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Great job today! You completed $completedHabits out of $totalHabits habits ($percentage%). " +
                "Current streak: $streak days. Keep up the momentum!"
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(999, notification)
    }

    private fun createShareIntent(streakDays: Int, habitName: String): PendingIntent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT,
                "I just reached a $streakDays-day streak for $habitName using Youth Habit Tracker! 🎯")
            putExtra(Intent.EXTRA_SUBJECT, "Habit Streak Achievement!")
        }

        val chooser = Intent.createChooser(shareIntent, "Share your achievement")
        return PendingIntent.getActivity(
            context,
            0,
            chooser,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createCompleteHabitIntent(habitId: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "COMPLETE_HABIT"
            putExtra("habit_id", habitId)
        }

        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSkipHabitIntent(habitId: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "SKIP_HABIT"
            putExtra("habit_id", habitId)
        }

        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode() + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }

    companion object {
        const val HABIT_REMINDER_WORK_TAG = "habit_reminder"
        const val DAILY_SUMMARY_WORK_TAG = "daily_summary"

        const val HABIT_ID = "habit_id"
        const val HABIT_NAME = "habit_name"
        const val HOUR = "hour"
        const val MINUTE = "minute"
        const val DAY_OF_WEEK = "day_of_week"
    }
}