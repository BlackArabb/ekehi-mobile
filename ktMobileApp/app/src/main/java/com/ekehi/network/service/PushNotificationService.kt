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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val channelId = "ekehi_push_notifications"
    
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
        showNotification(
            "Mining Update",
            "You've earned $coinsEarned EKEHI coins!",
            sessionId.hashCode()
        )
    }
    
    fun showSocialTaskCompletedNotification(taskTitle: String) {
        showNotification(
            "Task Completed",
            "You've completed '$taskTitle' and earned rewards!",
            taskTitle.hashCode()
        )
    }
    
    fun showReferralBonusNotification(bonusAmount: Double) {
        showNotification(
            "Referral Bonus",
            "You've earned $bonusAmount EKEHI coins from your referral!",
            "referral_bonus".hashCode()
        )
    }
    
    fun showStreakBonusNotification(streakDays: Int, bonusAmount: Double) {
        showNotification(
            "Streak Bonus",
            "Congratulations! You've maintained a $streakDays-day streak and earned $bonusAmount EKEHI coins!",
            "streak_bonus".hashCode()
        )
    }
}