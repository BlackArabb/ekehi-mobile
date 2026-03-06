package com.ekehi.network.service

import android.content.Context
import android.util.Log
import com.ekehi.network.security.SecurePreferences

/**
 * Test helper to verify all notification systems are working
 * Usage: Call methods from Logcat or a test button
 * 
 * How to use:
 * 1. In MainActivity or any Activity:
 *    val testHelper = NotificationTestHelper(this, pushNotificationService, securePreferences, miningReminderManager)
 *    testHelper.testAllNotifications()
 * 
 * 2. Or trigger from Logcat by calling static methods
 */
class NotificationTestHelper(
    private val context: Context,
    private val pushNotificationService: PushNotificationService,
    private val securePreferences: SecurePreferences,
    private val miningReminderManager: MiningReminderManager
) {
    companion object {
        private const val TAG = "NotificationTest"
        
        /**
         * Quick test all notifications - call from anywhere
         */
        @JvmStatic
        fun logTestAll() {
            Log.d(TAG, "=== TO TEST NOTIFICATIONS, ADD TO MAINACTIVITY: ===")
            Log.d(TAG, "val testHelper = NotificationTestHelper(")
            Log.d(TAG, "    this,")
            Log.d(TAG, "    pushNotificationService,")
            Log.d(TAG, "    securePreferences,")
            Log.d(TAG, "    miningReminderManager")
            Log.d(TAG, ")")
            Log.d(TAG, "testHelper.testAllNotifications()")
        }
    }

    /**
     * Test ALL notifications at once
     */
    fun testAllNotifications() {
        Log.d(TAG, "=== TESTING ALL NOTIFICATIONS ===")
        
        testPushNotification()
        testMiningNotification()
        testSocialTaskNotification()
        testReferralNotification()
        testStreakNotification()
        
        Log.d(TAG, "=== ALL NOTIFICATION TESTS COMPLETED ===")
    }

    /**
     * Test basic push notification
     */
    fun testPushNotification() {
        Log.d(TAG, "Testing: Basic Push Notification")
        
        pushNotificationService.showNotification(
            title = "Test Notification",
            message = "This is a test notification from KtMobileApp!",
            notificationId = "test_push".hashCode()
        )
        
        Log.d(TAG, "✅ Push notification sent")
    }

    /**
     * Test mining completion notification
     */
    fun testMiningNotification() {
        Log.d(TAG, "Testing: Mining Completion Notification")
        
        pushNotificationService.showMiningUpdateNotification(
            coinsEarned = 2.0,
            sessionId = "test_session_${System.currentTimeMillis()}"
        )
        
        Log.d(TAG, "✅ Mining notification sent")
    }

    /**
     * Test social task notification
     */
    fun testSocialTaskNotification() {
        Log.d(TAG, "Testing: Social Task Notification")
        
        pushNotificationService.showSocialTaskCompletedNotification(
            taskTitle = "Follow on Twitter"
        )
        
        Log.d(TAG, "✅ Social task notification sent")
    }

    /**
     * Test referral bonus notification
     */
    fun testReferralNotification() {
        Log.d(TAG, "Testing: Referral Bonus Notification")
        
        pushNotificationService.showReferralBonusNotification(
            bonusAmount = 10.0
        )
        
        Log.d(TAG, "✅ Referral bonus notification sent")
    }

    /**
     * Test streak bonus notification
     */
    fun testStreakNotification() {
        Log.d(TAG, "Testing: Streak Bonus Notification")
        
        pushNotificationService.showStreakBonusNotification(
            streakDays = 7,
            bonusAmount = 5.0
        )
        
        Log.d(TAG, "✅ Streak bonus notification sent")
    }

    /**
     * Test mining reminder worker manually
     */
    fun testMiningReminderWorker() {
        Log.d(TAG, "Testing: Mining Reminder Worker")
        
        // Simulate mining stopped 2 hours ago to trigger first reminder
        val stopTime = System.currentTimeMillis() - (2L * 60 * 60 * 1000)
        securePreferences.putLong("last_mining_stop_time", stopTime)
        securePreferences.putInt("reminder_sequence", 0)
        
        // Enable mining reminders
        miningReminderManager.setMiningReminderEnabled(true)
        
        // Trigger reminder check
        miningReminderManager.sendReminderIfAppropriate()
        
        Log.d(TAG, "✅ Mining reminder test triggered")
    }

    /**
     * Check notification settings status
     */
    fun checkNotificationSettings() {
        Log.d(TAG, "=== NOTIFICATION SETTINGS STATUS ===")
        Log.d(TAG, "Push Notifications: ${securePreferences.getBoolean("push_notifications_enabled", true)}")
        Log.d(TAG, "Mining Notifications: ${securePreferences.getBoolean("mining_notifications_enabled", true)}")
        Log.d(TAG, "Social Task Notifications: ${securePreferences.getBoolean("social_task_notifications_enabled", true)}")
        Log.d(TAG, "Referral Notifications: ${securePreferences.getBoolean("referral_notifications_enabled", true)}")
        Log.d(TAG, "Streak Notifications: ${securePreferences.getBoolean("streak_notifications_enabled", true)}")
        Log.d(TAG, "Mining Reminders Enabled: ${miningReminderManager.isMiningReminderEnabled()}")
        Log.d(TAG, "Hours since mining stopped: ${miningReminderManager.getHoursSinceMiningStopped()}")
        Log.d(TAG, "=== END SETTINGS STATUS ===")
    }
}
