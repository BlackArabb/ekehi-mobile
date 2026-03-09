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
     * 
     * NOTE: Banner Ads runs on schedule (every 30 min)
     * Social Tasks and Mining Completion are EVENT-BASED (called from ViewModels)
     * Mining Reminder runs on schedule (hourly check)
     */
    fun startAllNotificationServices() {
        Log.d(TAG, "Starting all notification services")
        
        // Schedule Banner Ads notification worker (every 30 min - works in background)
        BannerAdNotificationWorker.schedulePeriodicCheck(context)
        
        // Schedule mining reminders (hourly check for tiered reminders)
        miningReminderManager.scheduleReminder()
        
        Log.d(TAG, "✅ Notification services started")
    }
    
    /**
     * Stop all background notification services
     * Call this when user logs out or disables all notifications
     */
    fun stopAllNotificationServices() {
        Log.d(TAG, "Stopping all notification services")
        
        // Cancel banner ads worker
        BannerAdNotificationWorker.cancelScheduledCheck(context)
        
        // Cancel mining reminders
        miningReminderManager.cancelReminder()
        
        Log.d(TAG, "✅ Notification services stopped")
    }
    
    /**
     * Update notification services based on user preferences
     * Call this when user changes notification settings
     * 
     * NOTE: Banner Ads, Social Tasks, Mining Completion are EVENT-BASED
     * Only mining reminders are controlled here
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
        
        // Banner Ads, Social Tasks, Mining Completion are EVENT-BASED
        // No periodic workers to schedule/cancel
        
        // Mining reminders - enable/disable
        if (miningRemindersEnabled) {
            miningReminderManager.setMiningReminderEnabled(true)
            miningReminderManager.scheduleReminder()
        } else {
            miningReminderManager.setMiningReminderEnabled(false)
            miningReminderManager.cancelReminder()
        }
        
        Log.d(TAG, "✅ Notification services updated (event-based notifications always active)")
    }
    
    /**
     * Check status of all notification services
     * NOTE: Banner Ads, Social Tasks, Mining Completion are EVENT-BASED (always ready)
     */
    fun getServicesStatus(): Map<String, Boolean> {
        return mapOf(
            "social_task_notifications" to true, // Event-based
            "mining_completion_notifications" to true, // Event-based
            "banner_ad_notifications" to true, // Event-based
            "mining_reminders" to miningReminderManager.isMiningReminderEnabled() // Scheduled
        )
    }
}
