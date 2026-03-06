package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.security.SecurePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker for checking mining session completion and sending notifications
 */
@HiltWorker
class MiningCompletionNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val miningRepository: MiningRepository,
    private val pushNotificationService: PushNotificationService,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "MiningCompletionNotificationWorker"
        private const val WORK_NAME = "mining_completion_notification_check"
        private const val CHECK_INTERVAL_MINUTES = 30L // Check every 30 minutes

        /**
         * Schedule periodic mining completion checks
         */
        fun schedulePeriodicCheck(context: Context) {
            Log.d(TAG, "Scheduling periodic mining completion check")
            
            val workRequest = PeriodicWorkRequestBuilder<MiningCompletionNotificationWorker>(
                CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    5, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d(TAG, "Mining completion check scheduled - interval: ${CHECK_INTERVAL_MINUTES}m")
        }

        /**
         * Cancel scheduled mining completion checks
         */
        fun cancelScheduledCheck(context: Context) {
            Log.d(TAG, "Canceling scheduled mining completion check")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting mining completion check")

            // Check if mining notifications are enabled
            if (!securePreferences.getBoolean("mining_notifications_enabled", true)) {
                Log.d(TAG, "Mining notifications disabled - skipping")
                return@withContext Result.success()
            }

            // Get current user ID
            val userId = securePreferences.getString("user_id", null)
            if (userId.isNullOrEmpty()) {
                Log.d(TAG, "No user logged in - skipping notification check")
                return@withContext Result.success()
            }

            // Check for completed mining sessions
            val result = miningRepository.checkOngoingMiningSession()
            result.onSuccess { status ->
                if (status != null && status.isComplete && !status.finalRewardClaimed) {
                    Log.d(TAG, "Mining session complete - sending notification")
                    
                    // Send mining completion notification
                    pushNotificationService.showMiningUpdateNotification(
                        status.reward,
                        "session_complete_${status.startTime}"
                    )
                    
                    Log.d(TAG, "Mining completion notification sent for reward: ${status.reward}")
                } else {
                    Log.d(TAG, "No completed mining sessions found")
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to check mining session: ${error.message}", error)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in mining completion check: ${e.message}", e)
            Result.retry()
        }
    }
}
