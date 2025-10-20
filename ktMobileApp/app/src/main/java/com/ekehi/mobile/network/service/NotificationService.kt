package com.ekehi.mobile.network.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val context: Context
) {
    private val channelId = "ekehi_notifications"
    private val notificationId = 1001

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ekehi Notifications"
            val descriptionText = "Notifications for Ekehi Mobile app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a system icon for now
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun showMiningCompletedNotification(reward: Double) {
        showNotification(
            title = "Mining Session Completed!",
            message = "Your 24-hour mining session has finished. You earned $reward EKH tokens!"
        )
    }

    fun showSocialTaskCompletedNotification(reward: Double) {
        showNotification(
            title = "Social Task Completed!",
            message = "You earned $reward EKH tokens from completing a social task!"
        )
    }

    fun showReferralBonusNotification(reward: Double) {
        showNotification(
            title = "Referral Bonus!",
            message = "You earned $reward EKH tokens from a referral!"
        )
    }
}