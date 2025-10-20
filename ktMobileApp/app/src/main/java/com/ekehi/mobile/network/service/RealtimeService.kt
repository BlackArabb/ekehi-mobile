package com.ekehi.mobile.network.service

import io.appwrite.models.RealtimeResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeService @Inject constructor(
    private val appwriteService: AppwriteService,
    private val notificationHandler: RealtimeNotificationHandler
) {
    private val _realtimeEvents = MutableSharedFlow<RealtimeResponse>(replay = 0)
    val realtimeEvents: SharedFlow<RealtimeResponse> = _realtimeEvents

    private var subscription: ((RealtimeResponse) -> Unit)? = null

    fun subscribeToCollection(collectionId: String) {
        try {
            appwriteService.realtime.subscribe(
                channels = listOf("collections.$collectionId.documents")
            ) { response ->
                // Handle the notification
                notificationHandler.handleRealtimeEvent(response)
                
                // Emit the event to the shared flow
                GlobalScope.launch {
                    _realtimeEvents.emit(response)
                }
            }
        } catch (e: Exception) {
            // Handle subscription error
            e.printStackTrace()
        }
    }

    fun subscribeToUserDocuments(userId: String) {
        try {
            appwriteService.realtime.subscribe(
                channels = listOf(
                    "collections.${AppwriteService.USER_PROFILES_COLLECTION}.documents.$userId",
                    "collections.${AppwriteService.MINING_SESSIONS_COLLECTION}.documents",
                    "collections.${AppwriteService.SOCIAL_TASKS_COLLECTION}.documents"
                )
            ) { response ->
                // Handle the notification
                notificationHandler.handleRealtimeEvent(response)
                
                // Emit the event to the shared flow
                GlobalScope.launch {
                    _realtimeEvents.emit(response)
                }
            }
        } catch (e: Exception) {
            // Handle subscription error
            e.printStackTrace()
        }
    }

    fun unsubscribe() {
        // In the new SDK, we might not need to explicitly close the subscription
    }
}