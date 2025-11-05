package com.youth.habittracker.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // Reschedule all notifications when device reboots
                rescheduleAllNotifications(context)
            }
        }
    }

    private fun rescheduleAllNotifications(context: Context) {
        // Cancel any existing work to avoid duplicates
        WorkManager.getInstance(context).cancelAllWorkByTag(NotificationScheduler.HABIT_REMINDER_WORK_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(NotificationScheduler.DAILY_SUMMARY_WORK_TAG)

        // Reschedule daily summary
        notificationScheduler.scheduleDailySummary()

        // Note: Individual habit reminders would be rescheduled when the app loads
        // and retrieves the user's habits from the database
    }
}