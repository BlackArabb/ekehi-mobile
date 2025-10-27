package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.offline.OfflineMiningRepository
import com.ekehi.network.data.sync.SyncManager
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.domain.usecase.MiningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiningViewModel @Inject constructor(
    private val miningUseCase: MiningUseCase,
    private val miningRepository: MiningRepository,
    private val syncManager: SyncManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _miningSession = MutableStateFlow<Resource<MiningSession>>(Resource.Loading)
    val miningSession: StateFlow<Resource<MiningSession>> = _miningSession

    private val _miningProgress = MutableStateFlow(0.0)
    val miningProgress: StateFlow<Double> = _miningProgress

    private val _remainingTime = MutableStateFlow(24 * 60 * 60) // 24 hours in seconds
    val remainingTime: StateFlow<Int> = _remainingTime

    private val _isMining = MutableStateFlow(false)
    val isMining: StateFlow<Boolean> = _isMining

    private val _totalMined = MutableStateFlow(0.0)
    val totalMined: StateFlow<Double> = _totalMined

    private val _sessionEarnings = MutableStateFlow(0.0)
    val sessionEarnings: StateFlow<Double> = _sessionEarnings

    fun startMining() {
        if (_isMining.value) return
        
        _isMining.value = true
        _sessionEarnings.value = 0.0
        _miningProgress.value = 0.0
        _remainingTime.value = 24 * 60 * 60 // Reset to 24 hours
        
        viewModelScope.launch {
            // Simulate mining process
            val totalTime = 24 * 60 * 60 // 24 hours in seconds
            var elapsed = 0
            
            while (elapsed < totalTime && _isMining.value) {
                delay(1000) // Update every second
                elapsed++
                
                // Update progress (0.0 to 1.0)
                _miningProgress.value = elapsed.toDouble() / totalTime.toDouble()
                
                // Update remaining time
                _remainingTime.value = totalTime - elapsed
                
                // Update session earnings (2 EKH over 24 hours)
                _sessionEarnings.value = 2.0 * (elapsed.toDouble() / totalTime.toDouble())
            }
            
            if (_isMining.value) {
                // Mining completed
                _sessionEarnings.value = 2.0
                _totalMined.value += 2.0
                
                // In a real implementation, you would:
                // 1. Save the mining session to the database
                // 2. Update user balance in Appwrite
                // 3. Show completion notification
                
                // For now, we'll just stop mining
                _isMining.value = false
            }
        }
    }

    fun stopMining() {
        _isMining.value = false
    }

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
        // Removed realtime functionality for now
        // In a real implementation, you would subscribe to mining updates
    }

    private fun handleRealtimeEvent(event: Any) {
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