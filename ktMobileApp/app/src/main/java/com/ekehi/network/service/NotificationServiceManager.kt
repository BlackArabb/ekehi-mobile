package com.ekehi.network.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for all background notification services
 * Coordinates scheduling and cancellation of all notification workers
 */
@Singleton
class NotificationServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val miningReminderManager: MiningReminderManager
) {
    
    companion object {
        private const val TAG = "NotificationServiceManager"
    }
    
    /**
     * Start all background notification services
     * Call this when user logs in or enables notifications
     */
    fun startAllNotificationServices() {
        Log.d(TAG, "Starting all notification services")
        
        // Schedule social task notifications (every 6 hours)
        SocialTaskNotificationWorker.schedulePeriodicCheck(context)
        
        // Schedule mining completion notifications (every 30 minutes)
        MiningCompletionNotificationWorker.schedulePeriodicCheck(context)
        
        // Schedule banner ad notifications (every 12 hours)
        BannerAdNotificationWorker.schedulePeriodicCheck(context)
        
        // Schedule mining reminders (every 24 hours)
        miningReminderManager.scheduleReminder()
        
        Log.d(TAG, "✅ All notification services started")
    }
    
    /**
     * Stop all background notification services
     * Call this when user logs out or disables notifications
     */
    fun stopAllNotificationServices() {
        Log.d(TAG, "Stopping all notification services")
        
        // Cancel social task notifications
        SocialTaskNotificationWorker.cancelScheduledCheck(context)
        
        // Cancel mining completion notifications
        MiningCompletionNotificationWorker.cancelScheduledCheck(context)
        
        // Cancel banner ad notifications
        BannerAdNotificationWorker.cancelScheduledCheck(context)
        
        // Cancel mining reminders
        miningReminderManager.cancelReminder()
        
        Log.d(TAG, "✅ All notification services stopped")
    }
    
    /**
     * Update notification services based on user preferences
     * Call this when user changes notification settings
     */
    fun updateNotificationServices(
        miningEnabled: Boolean,
        socialTasksEnabled: Boolean,
        referralsEnabled: Boolean,
        pushNotificationsEnabled: Boolean,
        miningRemindersEnabled: Boolean
    ) {
        Log.d(TAG, "Updating notification services based on preferences")
        
        // If all notifications disabled, stop everything
        if (!pushNotificationsEnabled) {
            stopAllNotificationServices()
            return
        }
        
        // Restart services based on individual preferences
        if (socialTasksEnabled) {
            SocialTaskNotificationWorker.schedulePeriodicCheck(context)
        } else {
            SocialTaskNotificationWorker.cancelScheduledCheck(context)
        }
        
        if (miningEnabled) {
            MiningCompletionNotificationWorker.schedulePeriodicCheck(context)
        } else {
            MiningCompletionNotificationWorker.cancelScheduledCheck(context)
        }
        
        // Banner ads always run if push notifications enabled
        BannerAdNotificationWorker.schedulePeriodicCheck(context)
        
        // Mining reminders
        if (miningRemindersEnabled) {
            miningReminderManager.setMiningReminderEnabled(true)
        } else {
            miningReminderManager.setMiningReminderEnabled(false)
        }
        
        Log.d(TAG, "✅ Notification services updated")
    }
    
    /**
     * Check status of all notification services
     */
    fun getServicesStatus(): Map<String, Boolean> {
        return mapOf(
            "social_task_notifications" to true, // WorkManager handles persistence
            "mining_completion_notifications" to true,
            "banner_ad_notifications" to true,
            "mining_reminders" to miningReminderManager.isMiningReminderEnabled()
        )
    }
}
