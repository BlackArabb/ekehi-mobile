package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.security.SecurePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker for checking and sending notifications about new social tasks
 */
@HiltWorker
class SocialTaskNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val socialTaskRepository: SocialTaskRepository,
    private val pushNotificationService: PushNotificationService,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SocialTaskNotificationWorker"
        private const val WORK_NAME = "social_task_notification_check"
        private const val CHECK_INTERVAL_HOURS = 6L // Check every 6 hours

        /**
         * Schedule periodic social task notification checks
         */
        fun schedulePeriodicCheck(context: Context) {
            Log.d(TAG, "Scheduling periodic social task notification check")
            
            val workRequest = PeriodicWorkRequestBuilder<SocialTaskNotificationWorker>(
                CHECK_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d(TAG, "Social task notification check scheduled - interval: ${CHECK_INTERVAL_HOURS}h")
        }

        /**
         * Cancel scheduled social task notification checks
         */
        fun cancelScheduledCheck(context: Context) {
            Log.d(TAG, "Canceling scheduled social task notification check")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting social task notification check")

            // Check if social task notifications are enabled
            if (!securePreferences.getBoolean("social_task_notifications_enabled", true)) {
                Log.d(TAG, "Social task notifications disabled - skipping")
                return@withContext Result.success()
            }

            // Get current user ID
            val userId = securePreferences.getString("user_id", null)
            if (userId.isNullOrEmpty()) {
                Log.d(TAG, "No user logged in - skipping notification check")
                return@withContext Result.success()
            }

            // Check for new available tasks
            try {
                val tasks = socialTaskRepository.getAvailableSocialTasks(userId).getOrNull() ?: emptyList()
                if (tasks.isNotEmpty()) {
                    Log.d(TAG, "Found ${tasks.size} new social tasks - sending notification")
                    
                    // Send notification for new tasks
                    pushNotificationService.showNotification(
                        "New Tasks Available! 📋",
                        "${tasks.size} new social tasks waiting for you. Complete them to earn rewards!",
                        "new_tasks_available".hashCode()
                    )
                } else {
                    Log.d(TAG, "No new social tasks found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get social tasks: ${e.message}", e)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in social task notification check: ${e.message}", e)
            Result.retry()
        }
    }
}
