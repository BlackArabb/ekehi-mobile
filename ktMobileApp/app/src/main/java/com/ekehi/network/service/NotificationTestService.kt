package com.ekehi.network.service

import android.content.Context
import android.util.Log
import com.ekehi.network.security.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test service for verifying notification system functionality
 */
@Singleton
class NotificationTestService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences,
    private val pushNotificationService: PushNotificationService,
    private val notificationService: NotificationService
) {
    
    companion object {
        private const val TAG = "NotificationTestService"
    }
    
    /**
     * Test all notification types and verify they respect user preferences
     */
    fun testAllNotifications() {
        Log.d(TAG, "=== TESTING ALL NOTIFICATIONS ===")
        
        // Test mining notifications
        testMiningNotification()
        
        // Test social task notifications
        testSocialTaskNotification()
        
        // Test referral notifications
        testReferralNotification()
        
        // Test push notifications
        testPushNotification()
        
        Log.d(TAG, "=== NOTIFICATION TESTS COMPLETED ===")
    }
    
    /**
     * Test mining notification
     */
    fun testMiningNotification() {
        val enabled = securePreferences.getBoolean("mining_notifications_enabled", true)
        Log.d(TAG, "Mining notifications enabled: $enabled")
        
        if (enabled) {
            pushNotificationService.showMiningUpdateNotification(10.5, "test_session_123")
            notificationService.showMiningCompletedNotification(5.0)
            Log.d(TAG, "✅ Mining notifications sent")
        } else {
            Log.d(TAG, "⚠️ Mining notifications disabled - not sending")
        }
    }
    
    /**
     * Test social task notification
     */
    fun testSocialTaskNotification() {
        val enabled = securePreferences.getBoolean("social_task_notifications_enabled", true)
        Log.d(TAG, "Social task notifications enabled: $enabled")
        
        if (enabled) {
            pushNotificationService.showSocialTaskCompletedNotification("Test Task")
            notificationService.showSocialTaskCompletedNotification(2.5)
            Log.d(TAG, "✅ Social task notifications sent")
        } else {
            Log.d(TAG, "⚠️ Social task notifications disabled - not sending")
        }
    }
    
    /**
     * Test referral notification
     */
    fun testReferralNotification() {
        val enabled = securePreferences.getBoolean("referral_notifications_enabled", true)
        Log.d(TAG, "Referral notifications enabled: $enabled")
        
        if (enabled) {
            pushNotificationService.showReferralBonusNotification(50.0)
            notificationService.showReferralBonusNotification(25.0)
            Log.d(TAG, "✅ Referral notifications sent")
        } else {
            Log.d(TAG, "⚠️ Referral notifications disabled - not sending")
        }
    }
    
    /**
     * Test push notification
     */
    fun testPushNotification() {
        val enabled = securePreferences.getBoolean("push_notifications_enabled", true)
        Log.d(TAG, "Push notifications enabled: $enabled")
        
        if (enabled) {
            pushNotificationService.showNotification("Test Notification", "This is a test notification message")
            Log.d(TAG, "✅ Push notification sent")
        } else {
            Log.d(TAG, "⚠️ Push notifications disabled - not sending")
        }
    }
    
    /**
     * Test in-app notification
     */
    fun testInAppNotification() {
        val enabled = securePreferences.getBoolean("in_app_notifications_enabled", true)
        Log.d(TAG, "In-app notifications enabled: $enabled")
        
        if (enabled) {
            // In-app notifications would be handled by the UI layer
            Log.d(TAG, "ℹ️ In-app notifications would be displayed in UI")
        } else {
            Log.d(TAG, "⚠️ In-app notifications disabled")
        }
    }
    
    /**
     * Test email notification preference
     */
    fun testEmailNotificationPreference() {
        val enabled = securePreferences.getBoolean("email_notifications_enabled", true)
        Log.d(TAG, "Email notifications enabled: $enabled")
        
        if (enabled) {
            Log.d(TAG, "ℹ️ Email notifications would be sent via backend")
        } else {
            Log.d(TAG, "⚠️ Email notifications disabled")
        }
    }
    
    /**
     * Verify all notification preferences are properly stored
     */
    fun verifyNotificationPreferences(): Map<String, Boolean> {
        val preferences = mapOf(
            "mining_notifications_enabled" to securePreferences.getBoolean("mining_notifications_enabled", true),
            "social_task_notifications_enabled" to securePreferences.getBoolean("social_task_notifications_enabled", true),
            "referral_notifications_enabled" to securePreferences.getBoolean("referral_notifications_enabled", true),
            "push_notifications_enabled" to securePreferences.getBoolean("push_notifications_enabled", true),
            "email_notifications_enabled" to securePreferences.getBoolean("email_notifications_enabled", true),
            "in_app_notifications_enabled" to securePreferences.getBoolean("in_app_notifications_enabled", true),
            "analytics_enabled" to securePreferences.getBoolean("analytics_enabled", true)
        )
        
        Log.d(TAG, "=== NOTIFICATION PREFERENCES ===")
        preferences.forEach { (key, value) ->
            Log.d(TAG, "$key: $value")
        }
        
        return preferences
    }
}
