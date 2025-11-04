package com.youth.habittracker.utils.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.youth.habittracker.data.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "HABIT_REMINDER" -> {
                val habitId = intent.getStringExtra("habit_id") ?: return
                val habitName = intent.getStringExtra("habit_name") ?: return

                // Show the notification (it's already shown by the worker,
                // but we can handle any additional logic here)
                handleHabitReminder(context, habitId, habitName)
            }

            "COMPLETE_HABIT" -> {
                val habitId = intent.getStringExtra("habit_id") ?: return
                handleCompleteHabit(context, habitId)
            }

            "SKIP_HABIT" -> {
                val habitId = intent.getStringExtra("habit_id") ?: return
                handleSkipHabit(context, habitId)
            }

            "STREAK_ACHIEVEMENT" -> {
                val streakDays = intent.getIntExtra("streak_days", 0)
                val habitName = intent.getStringExtra("habit_name") ?: return
                handleStreakAchievement(context, streakDays, habitName)
            }

            "DAILY_SUMMARY" -> {
                handleDailySummary(context)
            }
        }
    }

    private fun handleHabitReminder(context: Context, habitId: String, habitName: String) {
        // Additional logic for habit reminders if needed
        // The notification is already shown by the HabitReminderWorker
    }

    private fun handleCompleteHabit(context: Context, habitId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Complete the habit
                habitRepository.createHabitEntry(
                    com.youth.habittracker.data.model.HabitEntry(
                        habitId = habitId,
                        date = com.google.firebase.Timestamp.now(),
                        completed = true,
                        completedAt = com.google.firebase.Timestamp.now()
                    )
                )

                // Cancel the notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(habitId.hashCode())

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun handleSkipHabit(context: Context, habitId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Skip the habit
                habitRepository.createHabitEntry(
                    com.youth.habittracker.data.model.HabitEntry(
                        habitId = habitId,
                        date = com.google.firebase.Timestamp.now(),
                        completed = false
                    )
                )

                // Cancel the notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(habitId.hashCode())

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun handleStreakAchievement(context: Context, streakDays: Int, habitName: String) {
        // Handle streak achievement - maybe show a celebration screen
        // or update some local state
    }

    private fun handleDailySummary(context: Context) {
        // Handle daily summary notification tap
        // Could open the app to the home screen
    }
}