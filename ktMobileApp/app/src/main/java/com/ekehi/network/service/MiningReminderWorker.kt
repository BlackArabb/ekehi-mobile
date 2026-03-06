package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ekehi.network.security.SecurePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for sending mining reminder notifications
 */
@HiltWorker
class MiningReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val securePreferences: SecurePreferences,
    private val pushNotificationService: PushNotificationService
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "MiningReminderWorker"
        private const val MINING_REMINDER_ENABLED = "mining_reminder_enabled"
        private const val LAST_REMINDER_TIME = "last_reminder_time"
        private const val REMINDER_INTERVAL_HOURS = 24L // Send reminder every 24 hours
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting mining reminder check")

            // Check if mining reminders are enabled
            if (!securePreferences.getBoolean(MINING_REMINDER_ENABLED, true)) {
                Log.d(TAG, "Mining reminders disabled - skipping")
                return@withContext Result.success()
            }

            // Get current user ID
            val userId = securePreferences.getString("user_id", null)
            if (userId.isNullOrEmpty()) {
                Log.d(TAG, "No user logged in - skipping reminder")
                return@withContext Result.success()
            }

            // Check if user has active mining session
            val isMiningActive = securePreferences.getBoolean("is_mining", false)
            if (isMiningActive) {
                Log.d(TAG, "User already mining - skipping reminder")
                return@withContext Result.success()
            }

            // Check time since last reminder
            val lastReminderTime = securePreferences.getLong(LAST_REMINDER_TIME, 0)
            val currentTime = System.currentTimeMillis()
            val hoursSinceLastReminder = (currentTime - lastReminderTime) / (1000 * 60 * 60)

            // Only send reminder if it's been at least 24 hours
            if (hoursSinceLastReminder >= REMINDER_INTERVAL_HOURS) {
                Log.d(TAG, "Sending mining reminder notification")
                
                pushNotificationService.showNotification(
                    "Time to Mine! ⛏️",
                    "Start your daily mining session and earn 2 EKH tokens. Tap to begin!"
                )
                
                // Update last reminder time
                securePreferences.putLong(LAST_REMINDER_TIME, currentTime)
                
                Log.d(TAG, "✅ Mining reminder sent successfully")
            } else {
                Log.d(TAG, "Reminder sent recently (${hoursSinceLastReminder}h ago) - skipping")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in mining reminder check: ${e.message}", e)
            Result.retry()
        }
    }
}
