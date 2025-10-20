package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.network.service.RealtimeService
import io.appwrite.models.RealtimeResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RealtimeUseCase @Inject constructor(
    private val realtimeService: RealtimeService
) {
    fun subscribeToUserUpdates(userId: String): Flow<RealtimeResponse> = callbackFlow {
        realtimeService.subscribeToUserDocuments(userId)
        
        realtimeService.realtimeEvents.collect { event ->
            trySend(event)
        }
        
        // Unsubscribe when the flow is cancelled
        invokeOnClose {
            realtimeService.unsubscribe()
        }
    }

    fun subscribeToCollectionUpdates(collectionId: String): Flow<RealtimeResponse> = callbackFlow {
        realtimeService.subscribeToCollection(collectionId)
        
        realtimeService.realtimeEvents.collect { event ->
            trySend(event)
        }
        
        // Unsubscribe when the flow is cancelled
        invokeOnClose {
            realtimeService.unsubscribe()
        }
    }
}