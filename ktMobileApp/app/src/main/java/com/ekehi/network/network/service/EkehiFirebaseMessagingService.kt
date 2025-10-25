package com.ekehi.network.network.service

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EkehiFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var pushNotificationService: PushNotificationService
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            handleNotificationMessage(it)
        }
    }
    
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to your server if needed
        sendRegistrationToServer(token)
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        // Handle data payload of FCM messages
        val messageType = data["type"]
        val title = data["title"] ?: "Ekehi Notification"
        val message = data["message"] ?: "You have a new notification"
        
        when (messageType) {
            "mining_update" -> {
                val coinsEarned = data["coins_earned"]?.toDoubleOrNull() ?: 0.0
                val sessionId = data["session_id"] ?: ""
                pushNotificationService.showMiningUpdateNotification(coinsEarned, sessionId)
            }
            "social_task_completed" -> {
                val taskTitle = data["task_title"] ?: "Social Task"
                pushNotificationService.showSocialTaskCompletedNotification(taskTitle)
            }
            "referral_bonus" -> {
                val bonusAmount = data["bonus_amount"]?.toDoubleOrNull() ?: 0.0
                pushNotificationService.showReferralBonusNotification(bonusAmount)
            }
            "streak_bonus" -> {
                val streakDays = data["streak_days"]?.toIntOrNull() ?: 0
                val bonusAmount = data["bonus_amount"]?.toDoubleOrNull() ?: 0.0
                pushNotificationService.showStreakBonusNotification(streakDays, bonusAmount)
            }
            else -> {
                pushNotificationService.showNotification(title, message)
            }
        }
    }
    
    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
        // Handle notification payload of FCM messages
        pushNotificationService.showNotification(
            notification.title ?: "Ekehi Notification",
            notification.body ?: "You have a new notification"
        )
    }
    
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server
        Log.d(TAG, "Sending token to server: $token")
    }
    
    companion object {
        private const val TAG = "EkehiFCM"
    }
}