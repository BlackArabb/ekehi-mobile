package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.analytics.AnalyticsManager
import com.ekehi.mobile.data.model.MiningSession
import com.ekehi.mobile.data.repository.MiningRepository
import com.ekehi.mobile.data.repository.offline.OfflineMiningRepository
import com.ekehi.mobile.data.sync.SyncManager
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.domain.usecase.MiningUseCase
import com.ekehi.mobile.domain.usecase.RealtimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.models.RealtimeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiningViewModel @Inject constructor(
    private val miningUseCase: MiningUseCase,
    private val miningRepository: MiningRepository,
    private val realtimeUseCase: RealtimeUseCase,
    private val syncManager: SyncManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _miningSession = MutableStateFlow<Resource<MiningSession>>(Resource.Loading)
    val miningSession: StateFlow<Resource<MiningSession>> = _miningSession

    private val _miningProgress = MutableStateFlow(0.0)
    val miningProgress: StateFlow<Double> = _miningProgress

    private val _remainingTime = MutableStateFlow(24 * 60 * 60) // 24 hours in seconds
    val remainingTime: StateFlow<Int> = _remainingTime

    fun startMiningSession(userId: String) {
        viewModelScope.launch {
            miningUseCase.startMiningSession(userId).collect { resource ->
                // Handle the result of starting the mining session
                if (resource is Resource.Success) {
                    // We need to get the actual mining session, not just Unit
                    // In a real implementation, you would get the session from the repository
                    // For now, we'll create a placeholder
                    val placeholderSession = MiningSession(
                        id = "session_id",
                        userId = userId,
                        coinsEarned = 0.0,
                        clicksMade = 0,
                        sessionDuration = 0,
                        createdAt = "",
                        updatedAt = ""
                    )
                    _miningSession.value = Resource.Success(placeholderSession)
                    // Cache the mining session if we're using offline repository
                    if (miningRepository is OfflineMiningRepository) {
                        miningRepository.cacheMiningSession(placeholderSession)
                    }
                    // Track mining session start
                    analyticsManager.trackMiningSessionStart(userId, placeholderSession.id)
                } else {
                    _miningSession.value = resource as Resource<MiningSession>
                }
            }
        }
    }

    fun endMiningSession(userId: String, sessionId: String, coinsEarned: Double, duration: Int) {
        viewModelScope.launch {
            // Track mining session end
            analyticsManager.trackMiningSessionEnd(userId, sessionId, coinsEarned, duration)
        }
    }

    fun loadMiningSessions(userId: String) {
        viewModelScope.launch {
            // Try to get offline data first for immediate UI
            if (miningRepository is OfflineMiningRepository) {
                miningRepository.getOfflineMiningSessions(userId).collect { sessions ->
                    if (sessions.isNotEmpty()) {
                        // Show the most recent session
                        sessions.firstOrNull()?.let { session ->
                            _miningSession.value = Resource.Success(session)
                        }
                    }
                }
            }
            
            // Try to get the current mining session from server
            // In a real implementation, you would have logic to determine the current session
        }
    }

    fun syncMiningData(userId: String) {
        viewModelScope.launch {
            val result = syncManager.syncAllData(userId)
            when (result) {
                is SyncManager.SyncResult.Success -> {
                    // Reload the mining sessions after successful sync
                    loadMiningSessions(userId)
                }
                is SyncManager.SyncResult.Failure -> {
                    // Handle sync failure
                }
            }
        }
    }

    private fun subscribeToMiningUpdates(userId: String) {
        viewModelScope.launch {
            realtimeUseCase.subscribeToUserUpdates(userId).collect { event ->
                handleRealtimeEvent(event)
            }
        }
    }

    private fun handleRealtimeEvent(event: RealtimeResponse) {
        // Handle real-time updates to mining sessions
        // In a real implementation, you would update the UI based on the event
    }

    private fun getUserIdFromCurrentState(): String? {
        // In a real implementation, you would get the user ID from auth state
        return "user_id_placeholder"
    }

    fun updateMiningProgress(progress: Double) {
        _miningProgress.value = progress
    }

    fun updateRemainingTime(time: Int) {
        _remainingTime.value = time
    }

    fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any ongoing subscriptions
    }
}