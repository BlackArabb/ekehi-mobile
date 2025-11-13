package com.ekehi.network.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ekehi.network.MainActivity
import com.ekehi.network.security.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences
) {
    private val channelId = "ekehi_push_notifications"
    
    companion object {
        // Notification settings keys
        private const val PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        private const val MINING_NOTIFICATIONS_ENABLED = "mining_notifications_enabled"
        private const val SOCIAL_TASK_NOTIFICATIONS_ENABLED = "social_task_notifications_enabled"
        private const val REFERRAL_NOTIFICATIONS_ENABLED = "referral_notifications_enabled"
        private const val STREAK_NOTIFICATIONS_ENABLED = "streak_notifications_enabled"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ekehi Notifications"
            val descriptionText = "Notifications for Ekehi app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showNotification(title: String, message: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        // Check if push notifications are enabled
        if (!securePreferences.getBoolean(PUSH_NOTIFICATIONS_ENABLED, true)) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
    
    fun showMiningUpdateNotification(coinsEarned: Double, sessionId: String) {
        // Check if mining notifications are enabled
        if (!securePreferences.getBoolean(MINING_NOTIFICATIONS_ENABLED, true)) {
            return
        }
        
        showNotification(
            "Mining Update",
            "You've earned $coinsEarned EKEHI coins!",
            sessionId.hashCode()
        )
    }
    
    fun showSocialTaskCompletedNotification(taskTitle: String) {
        // Check if social task notifications are enabled
        if (!securePreferences.getBoolean(SOCIAL_TASK_NOTIFICATIONS_ENABLED, true)) {
            return
        }
        
        showNotification(
            "Task Completed",
            "You've completed '$taskTitle' and earned rewards!",
            taskTitle.hashCode()
        )
    }
    
    fun showReferralBonusNotification(bonusAmount: Double) {
        // Check if referral notifications are enabled
        if (!securePreferences.getBoolean(REFERRAL_NOTIFICATIONS_ENABLED, true)) {
            return
        }
        
        showNotification(
            "Referral Bonus",
            "You've earned $bonusAmount EKEHI coins from your referral!",
            "referral_bonus".hashCode()
        )
    }
    
    fun showStreakBonusNotification(streakDays: Int, bonusAmount: Double) {
        // Check if streak notifications are enabled
        if (!securePreferences.getBoolean(STREAK_NOTIFICATIONS_ENABLED, true)) {
            return
        }
        
        showNotification(
            "Streak Bonus",
            "Congratulations! You've maintained a $streakDays-day streak and earned $bonusAmount EKEHI coins!",
            "streak_bonus".hashCode()
        )
    }
}