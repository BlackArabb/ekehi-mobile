package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ekehi.network.security.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages scheduled mining reminder notifications using WorkManager
 */
@Singleton
class MiningReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences,
    private val pushNotificationService: PushNotificationService
) {
    
    companion object {
        private const val TAG = "MiningReminderManager"
        private const val MINING_REMINDER_ENABLED = "mining_reminder_enabled"
        private const val LAST_MINING_STOP_TIME = "last_mining_stop_time"
        private const val REMINDER_SEQUENCE = "reminder_sequence"
        private const val FIRST_REMINDER_TIME = "first_reminder_time"
        private const val SECOND_REMINDER_TIME = "second_reminder_time"
        private const val WORK_NAME = "mining_reminder_check"
        private const val CHECK_INTERVAL_HOURS = 1L // Check every hour
        
        // Reminder intervals in hours
        private const val FIRST_REMINDER_INTERVAL = 1L      // 1 hour after stop
        private const val SECOND_REMINDER_INTERVAL = 5L     // 5 hours after first (6h total)
        private const val SUBSEQUENT_REMINDER_INTERVAL = 12L // 12 hours after second
    }
    
    /**
     * Enable or disable mining reminders and schedule/cancel work accordingly
     */
    fun setMiningReminderEnabled(enabled: Boolean) {
        securePreferences.putBoolean(MINING_REMINDER_ENABLED, enabled)
        Log.d(TAG, "Mining reminders ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            scheduleReminder()
        } else {
            cancelReminder()
        }
    }
    
    /**
     * Check if mining reminders are enabled
     */
    fun isMiningReminderEnabled(): Boolean {
        return securePreferences.getBoolean(MINING_REMINDER_ENABLED, true)
    }
    
    /**
     * Schedule periodic mining reminder checks using WorkManager
     */
    fun scheduleReminder() {
        Log.d(TAG, "Scheduling periodic mining reminder check")
        
        val workRequest = PeriodicWorkRequestBuilder<MiningReminderWorker>(
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
        
        Log.d(TAG, "Mining reminder scheduled - interval: ${CHECK_INTERVAL_HOURS}h")
    }
    
    /**
     * Cancel scheduled mining reminder checks
     */
    fun cancelReminder() {
        Log.d(TAG, "Canceling scheduled mining reminder")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    
    /**
     * Record when user stops mining (start reminder sequence)
     */
    fun recordMiningStop() {
        val timestamp = System.currentTimeMillis()
        securePreferences.putLong(LAST_MINING_STOP_TIME, timestamp)
        securePreferences.putInt(REMINDER_SEQUENCE, 0) // Reset sequence
        securePreferences.remove(FIRST_REMINDER_TIME)
        securePreferences.remove(SECOND_REMINDER_TIME)
        Log.d(TAG, "📍 recordMiningStop() called at timestamp: $timestamp (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))})")
    }
    
    /**
     * Reset all reminders (call when user resumes mining)
     */
    fun resetAllReminders() {
        securePreferences.remove(LAST_MINING_STOP_TIME)
        securePreferences.remove(REMINDER_SEQUENCE)
        securePreferences.remove(FIRST_REMINDER_TIME)
        securePreferences.remove(SECOND_REMINDER_TIME)
        Log.d(TAG, "All mining reminders cleared")
    }
    
    /**
     * Get hours since mining stopped
     */
    fun getHoursSinceMiningStopped(): Long {
        val stopTime = securePreferences.getLong(LAST_MINING_STOP_TIME, 0)
        if (stopTime == 0L) return 0L
        val currentTime = System.currentTimeMillis()
        return (currentTime - stopTime) / (1000 * 60 * 60)
    }
    
    /**
     * Send mining reminder with variable intervals based on reminder sequence
     * 1st reminder: 1 hour after stop
     * 2nd reminder: 5 hours after 1st (6h total)
     * Subsequent: Every 12 hours after 2nd
     */
    fun sendReminderIfAppropriate() {
        if (!isMiningReminderEnabled()) {
            Log.d(TAG, "Mining reminders disabled - skipping")
            return
        }
        
        val stopTime = securePreferences.getLong(LAST_MINING_STOP_TIME, 0)
        if (stopTime == 0L) {
            Log.d(TAG, "No mining stop time recorded - skipping")
            return
        }
        
        val hoursSinceStop = getHoursSinceMiningStopped()
        val reminderSequence = securePreferences.getInt(REMINDER_SEQUENCE, 0)
        
        when (reminderSequence) {
            0 -> {
                // First reminder: 1 hour after stop
                if (hoursSinceStop >= FIRST_REMINDER_INTERVAL) {
                    Log.d(TAG, "🔔 Sending FIRST reminder - ${hoursSinceStop}h since mining stopped")
                    
                    pushNotificationService.showNotification(
                        "Time to Mine! ⛏️",
                        "Your mining session ended 1 hour ago. Start mining again to earn EKEHI tokens!",
                        "mining_reminder_1".hashCode()
                    )
                    
                    // Record first reminder time and advance sequence
                    securePreferences.putLong(FIRST_REMINDER_TIME, System.currentTimeMillis())
                    securePreferences.putInt(REMINDER_SEQUENCE, 1)
                } else {
                    Log.d(TAG, "⏳ Waiting for first reminder - ${hoursSinceStop}h elapsed (need ${FIRST_REMINDER_INTERVAL}h)")
                }
            }
            1 -> {
                // Second reminder: 5 hours after first (6h total from stop)
                val firstReminderTime = securePreferences.getLong(FIRST_REMINDER_TIME, 0)
                val hoursSinceFirstReminder = if (firstReminderTime > 0) {
                    (System.currentTimeMillis() - firstReminderTime) / (1000 * 60 * 60)
                } else {
                    hoursSinceStop - FIRST_REMINDER_INTERVAL
                }
                
                if (hoursSinceFirstReminder >= SECOND_REMINDER_INTERVAL) {
                    Log.d(TAG, "🔔 Sending SECOND reminder - ${hoursSinceStop}h total, ${hoursSinceFirstReminder}h since first reminder")
                    
                    pushNotificationService.showNotification(
                        "Time to Mine! ⛏️",
                        "You haven't mined in 6 hours. Don't miss out on earning more EKEHI tokens!",
                        "mining_reminder_2".hashCode()
                    )
                    
                    // Record second reminder time and advance sequence
                    securePreferences.putLong(SECOND_REMINDER_TIME, System.currentTimeMillis())
                    securePreferences.putInt(REMINDER_SEQUENCE, 2)
                } else {
                    Log.d(TAG, "⏳ Waiting for second reminder - ${hoursSinceFirstReminder}h since first (need ${SECOND_REMINDER_INTERVAL}h)")
                }
            }
            else -> {
                // Subsequent reminders: Every 12 hours after second
                val secondReminderTime = securePreferences.getLong(SECOND_REMINDER_TIME, 0)
                val hoursSinceSecondReminder = if (secondReminderTime > 0) {
                    (System.currentTimeMillis() - secondReminderTime) / (1000 * 60 * 60)
                } else {
                    hoursSinceStop - FIRST_REMINDER_INTERVAL - SECOND_REMINDER_INTERVAL
                }
                
                if (hoursSinceSecondReminder >= SUBSEQUENT_REMINDER_INTERVAL) {
                    Log.d(TAG, "🔔 Sending SUBSEQUENT reminder - ${hoursSinceStop}h total, ${hoursSinceSecondReminder}h since second reminder")
                    
                    pushNotificationService.showNotification(
                        "Time to Mine! ⛏️",
                        "It's been a while since you last mined. Your EKEHI tokens are waiting!",
                        "mining_reminder_subsequent".hashCode()
                    )
                    
                    // Update second reminder time for next 12h cycle
                    securePreferences.putLong(SECOND_REMINDER_TIME, System.currentTimeMillis())
                } else {
                    Log.d(TAG, "⏳ Waiting for next reminder - ${hoursSinceSecondReminder}h since second (need ${SUBSEQUENT_REMINDER_INTERVAL}h)")
                }
            }
        }
    }
    
    /**
     * Test method - forces a reminder notification to be sent
     * Use this to verify notifications are working
     */
    fun testReminder() {
        Log.d(TAG, "🧪 TEST: Forcing reminder notification")
        pushNotificationService.showNotification(
            "🧪 TEST: Mining Reminder",
            "This is a test notification to verify reminders are working!",
            "mining_reminder_test".hashCode()
        )
    }
    
    /**
     * Debug method - logs current state of reminder system
     */
    fun logDebugState() {
        val stopTime = securePreferences.getLong(LAST_MINING_STOP_TIME, 0)
        val sequence = securePreferences.getInt(REMINDER_SEQUENCE, -1)
        val firstTime = securePreferences.getLong(FIRST_REMINDER_TIME, 0)
        val secondTime = securePreferences.getLong(SECOND_REMINDER_TIME, 0)
        val enabled = isMiningReminderEnabled()
        
        Log.d(TAG, "=== DEBUG STATE ===")
        Log.d(TAG, "Mining reminders enabled: $enabled")
        Log.d(TAG, "Last mining stop time: $stopTime (${if (stopTime > 0) "${getHoursSinceMiningStopped()}h ago" else "NOT SET"})")
        Log.d(TAG, "Reminder sequence: $sequence")
        Log.d(TAG, "First reminder time: $firstTime")
        Log.d(TAG, "Second reminder time: $secondTime")
        Log.d(TAG, "===================")
    }
}
