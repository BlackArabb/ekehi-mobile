package com.ekehi.network.service

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ekehi.network.data.model.AdContent
import com.ekehi.network.data.repository.AdsRepository
import com.ekehi.network.security.SecurePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker for checking and sending notifications about new banner ads
 * Notifies users when NEW ads are uploaded to Appwrite storage bucket
 */
@HiltWorker
class BannerAdNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val adsRepository: AdsRepository,
    private val pushNotificationService: PushNotificationService,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "BannerAdNotificationWorker"
        private const val WORK_NAME = "banner_ad_notification_check"
        private const val CHECK_INTERVAL_MINUTES = 30L // Check every 30 minutes
        
        // Notification IDs - using fixed IDs to avoid stacking
        private const val NOTIFICATION_ID_NEW_ADS = 1001
        private const val NOTIFICATION_ID_REMINDER = 1002
        
        // Keys for tracking last known ads - stored in SecurePreferences for consistency
        private const val LAST_AD_IDS = "last_ad_ids" // Comma-separated list of ad IDs
        private const val LAST_AD_CHECK_TIME = "last_ad_check_time" // Timestamp of last reminder

        /**
         * Schedule periodic banner ad notification checks
         */
        fun schedulePeriodicCheck(context: Context) {
            Log.d(TAG, "Scheduling periodic banner ad notification check")
            
            val workRequest = PeriodicWorkRequestBuilder<BannerAdNotificationWorker>(
                CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
            
            Log.d(TAG, "Banner ad notification check scheduled - interval: ${CHECK_INTERVAL_MINUTES}min")
        }

        /**
         * Cancel scheduled banner ad notification checks
         */
        fun cancelScheduledCheck(context: Context) {
            Log.d(TAG, "Canceling scheduled banner ad notification check")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Reset tracking - call this when user views the ads carousel
         * This marks current ads as "seen" so future new ads will trigger notification
         * Uses SecurePreferences for consistency with doWork()
         */
        fun markAdsAsSeen(securePreferences: SecurePreferences, adIds: List<String>) {
            securePreferences.putString(LAST_AD_IDS, adIds.joinToString(","))
            securePreferences.putLong(LAST_AD_CHECK_TIME, System.currentTimeMillis())
            Log.d(TAG, "Marked ${adIds.size} ads as seen")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting banner ad notification check")

            // Check if push notifications are enabled
            if (!securePreferences.getBoolean("push_notifications_enabled", true)) {
                Log.d(TAG, "Push notifications disabled - skipping")
                return@withContext Result.success()
            }

            // Check if banner ad notifications are enabled
            if (!securePreferences.getBoolean("banner_ad_notifications_enabled", true)) {
                Log.d(TAG, "Banner ad notifications disabled - skipping")
                return@withContext Result.success()
            }

            // Get current user ID
            val userId = securePreferences.getString("user_id", null)
            if (userId.isNullOrEmpty()) {
                Log.d(TAG, "No user logged in - skipping notification check")
                return@withContext Result.success()
            }

            // Get active ads from repository
            try {
                val currentAds = adsRepository.getActiveAds()
                
                if (currentAds.isEmpty()) {
                    Log.d(TAG, "No banner ads found")
                    return@withContext Result.success()
                }

                // Get current ad IDs
                val currentAdIds = currentAds.map { it.id }.toSet()
                
                // Get last known ad IDs from storage
                val lastAdIdsString = securePreferences.getString(LAST_AD_IDS, "")
                val lastAdIds = if (lastAdIdsString.isNullOrEmpty()) {
                    emptySet()
                } else {
                    lastAdIdsString.split(",").toSet()
                }
                
                // Find NEW ads (ads in current but not in last known)
                val newAdIds = currentAdIds - lastAdIds
                
                if (newAdIds.isNotEmpty()) {
                    // Get the new ads details
                    val newAds = currentAds.filter { it.id in newAdIds }
                    
                    Log.d(TAG, "🎉 Found ${newAdIds.size} NEW banner ads: $newAdIds")
                    
                    // Send notification about new ads - use fixed ID to avoid stacking
                    pushNotificationService.showNotification(
                        if (newAdIds.size == 1) "New Ad Available! 🖼️" else "New Ads Available! 🖼️",
                        if (newAdIds.size == 1) "Check out the new advertising content on the mining page!" else "${newAdIds.size} new ads available! Check them out on the mining page.",
                        NOTIFICATION_ID_NEW_ADS
                    )
                    
                    // Update last known ads to current (mark them as seen)
                    securePreferences.putString(LAST_AD_IDS, currentAdIds.joinToString(","))
                    Log.d(TAG, "Updated tracked ad IDs")
                } else {
                    // No new ads, but remind about existing ads periodically
                    val lastReminderTime = securePreferences.getLong(LAST_AD_CHECK_TIME, 0)
                    val currentTime = System.currentTimeMillis()
                    val hoursSinceLastReminder = (currentTime - lastReminderTime) / (1000 * 60 * 60)
                    
                    // Only remind every 6 hours about existing ads
                    if (hoursSinceLastReminder >= 6) {
                        Log.d(TAG, "Reminding about existing ${currentAds.size} banner ads")
                        
                        // Use fixed ID to avoid stacking reminders
                        pushNotificationService.showNotification(
                            "View Ads & Earn! 🖼️",
                            "You have ${currentAds.size} ads waiting to be viewed on the mining page.",
                            NOTIFICATION_ID_REMINDER
                        )
                        
                        // Update last reminder time
                        securePreferences.putLong(LAST_AD_CHECK_TIME, currentTime)
                    } else {
                        Log.d(TAG, "Skipping reminder - only been ${hoursSinceLastReminder}h since last reminder")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get banner ads: ${e.message}", e)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in banner ad notification check: ${e.message}", e)
            Result.retry()
        }
    }
}
