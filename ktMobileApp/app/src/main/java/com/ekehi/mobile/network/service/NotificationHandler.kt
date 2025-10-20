package com.ekehi.mobile.network.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.models.RealtimeResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pushNotificationService: PushNotificationService
) {
    
    fun handleRealtimeEvent(event: RealtimeResponse) {
        try {
            val payload = event.payload ?: return
            val collectionId = payload["collectionId"]?.toString() ?: return
            
            when (collectionId) {
                "mining_sessions" -> {
                    handleMiningSessionEvent(payload)
                }
                "user_social_tasks" -> {
                    handleSocialTaskEvent(payload)
                }
                "user_profiles" -> {
                    handleUserProfileEvent(payload)
                }
                "presale_purchases" -> {
                    handlePresaleEvent(payload)
                }
                else -> {
                    // Handle other events or show a generic notification
                    Log.d("NotificationHandler", "Unhandled collection: $collectionId")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHandler", "Error handling realtime event", e)
        }
    }
    
    private fun handleMiningSessionEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["eventType"]?.toString() ?: return
            val document = payload["document"] as? Map<String, Any> ?: return
            
            when (eventType) {
                "create", "update" -> {
                    val coinsEarned = (document["coinsEarned"] as? Number)?.toDouble() ?: 0.0
                    val sessionId = document["\$id"]?.toString() ?: ""
                    
                    if (coinsEarned > 0) {
                        pushNotificationService.showMiningUpdateNotification(coinsEarned, sessionId)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHandler", "Error handling mining session event", e)
        }
    }
    
    private fun handleSocialTaskEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["eventType"]?.toString() ?: return
            val document = payload["document"] as? Map<String, Any> ?: return
            
            when (eventType) {
                "create" -> {
                    // Assuming this is when a task is completed
                    val taskId = document["taskId"]?.toString() ?: ""
                    // In a real implementation, you would fetch the task details to get the title
                    pushNotificationService.showSocialTaskCompletedNotification("Social Task Completed")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHandler", "Error handling social task event", e)
        }
    }
    
    private fun handleUserProfileEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["eventType"]?.toString() ?: return
            val document = payload["document"] as? Map<String, Any> ?: return
            
            when (eventType) {
                "update" -> {
                    // Check for specific updates like referral bonuses or streak bonuses
                    val totalCoins = (document["totalCoins"] as? Number)?.toDouble() ?: 0.0
                    val previousTotalCoins = (document["previousTotalCoins"] as? Number)?.toDouble() ?: 0.0
                    
                    val coinsEarned = totalCoins - previousTotalCoins
                    if (coinsEarned > 0) {
                        // This could be a referral bonus or streak bonus
                        // In a real implementation, you would check what type of bonus it is
                        pushNotificationService.showReferralBonusNotification(coinsEarned)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHandler", "Error handling user profile event", e)
        }
    }
    
    private fun handlePresaleEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["eventType"]?.toString() ?: return
            val document = payload["document"] as? Map<String, Any> ?: return
            
            when (eventType) {
                "create" -> {
                    // Handle presale purchase event
                    val amount = (document["amount"] as? Number)?.toDouble() ?: 0.0
                    pushNotificationService.showNotification(
                        "Presale Purchase",
                        "Thank you for your presale purchase of $amount EKEHI coins!"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHandler", "Error handling presale event", e)
        }
    }
    
    fun showGenericNotification(title: String, message: String) {
        pushNotificationService.showNotification(title, message)
    }
}