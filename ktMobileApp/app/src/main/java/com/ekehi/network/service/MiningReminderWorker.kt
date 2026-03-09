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
 * Delegates to MiningReminderManager for all reminder logic
 */
@HiltWorker
class MiningReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val securePreferences: SecurePreferences,
    private val miningReminderManager: MiningReminderManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "MiningReminderWorker"
        private const val MINING_REMINDER_ENABLED = "mining_reminder_enabled"
        private const val LAST_REMINDER_TIME = "last_reminder_time"
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

            // Check if user has stopped mining (check if stop time exists)
            val lastMiningStopTime = securePreferences.getLong("last_mining_stop_time", 0)
            if (lastMiningStopTime == 0L) {
                Log.d(TAG, "No mining stop recorded - user may not have mined yet or is currently mining")
                return@withContext Result.success()
            }

            // Call MiningReminderManager to send tiered reminders
            Log.d(TAG, "Calling MiningReminderManager.sendReminderIfAppropriate()")
            miningReminderManager.sendReminderIfAppropriate()
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in mining reminder check: ${e.message}", e)
            Result.retry()
        }
    }
}
