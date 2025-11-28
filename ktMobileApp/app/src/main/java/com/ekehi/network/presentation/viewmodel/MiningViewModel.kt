package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiningViewModel @Inject constructor(
        private val miningRepository: MiningRepository,
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _is24HourMiningActive = MutableStateFlow(false)
    val is24HourMiningActive: StateFlow<Boolean> = _is24HourMiningActive

    private val _remainingTime = MutableStateFlow(24 * 60 * 60) // 24 hours in seconds
    val remainingTime: StateFlow<Int> = _remainingTime

    private val _progressPercentage = MutableStateFlow(0.0)
    val progressPercentage: StateFlow<Double> = _progressPercentage

    private val _sessionReward = MutableStateFlow(2.0)
    val sessionReward: StateFlow<Double> = _sessionReward

    // New state flow for real-time session earnings
    private val _sessionEarnings = MutableStateFlow(0.0)
    val sessionEarnings: StateFlow<Double> = _sessionEarnings

    // New state flow for user profile
    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile

    private val _finalRewardClaimed = MutableStateFlow(false)
    val finalRewardClaimed: StateFlow<Boolean> = _finalRewardClaimed

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentUserId: String? = null
    private var updateJob: Job? = null
    private var sessionStartTime: Long = 0 // Track when the session started

    init {
        // Get current user ID and check for ongoing mining session
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUser()
                result.onSuccess { user ->
                    currentUserId = user.id
                    // Load user profile
                    loadUserProfile(user.id)
                    checkOngoingMiningSession()
                }
            } catch (e: Exception) {
                // User not logged in yet
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserProfile(userId)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    _userProfile.value = Resource.Success(profile)
                } else {
                    _userProfile.value = Resource.Error("User profile not found")
                }
            } else {
                _userProfile.value = Resource.Error("Failed to load user profile: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Checks if there's an ongoing mining session
     * (Like checkOngoingMiningSession in React Native)
     */
    private fun checkOngoingMiningSession() {
        viewModelScope.launch {
            val result = miningRepository.checkOngoingMiningSession()

            result.onSuccess { status -> 
                if (status != null) {
                    if (!status.finalRewardClaimed) {
                        if (status.isComplete) {
                            // Session completed but reward not claimed
                            _is24HourMiningActive.value = true
                            _remainingTime.value = 0
                            _progressPercentage.value = 100.0
                            _sessionReward.value = status.reward
                            _sessionEarnings.value = status.reward // Set to full reward when complete
                            _finalRewardClaimed.value = false
                        } else {
                            // Session still in progress
                            _is24HourMiningActive.value = true
                            _remainingTime.value = status.remainingSeconds
                            _progressPercentage.value = status.progress * 100
                            _sessionReward.value = status.reward
                            sessionStartTime = status.startTime
                            // Calculate current earnings based on elapsed time
                            _sessionEarnings.value = calculateCurrentEarnings(status.startTime, status.duration, status.reward)
                            _finalRewardClaimed.value = false

                            // Start UI update loop
                            startUIUpdateLoop()
                        }
                    } else {
                        // Reward already claimed, clear session and reset UI
                        miningRepository.clearMiningSession()
                        resetMiningState()
                    }
                } else {
                    // No ongoing session
                    resetMiningState()
                }
            }
        }
    }

    /**
     * Calculates current earnings based on elapsed time
     */
    private fun calculateCurrentEarnings(startTime: Long, duration: Long, totalReward: Double): Double {
        val now = System.currentTimeMillis()
        val elapsed = now - startTime
        val progress = (elapsed.toDouble() / duration.toDouble()).coerceIn(0.0, 1.0)
        return progress * totalReward
    }

    /**
     * Handles mining button press
     * (Like handleMine in React Native)
     */
    fun handleMine() {
        viewModelScope.launch {
            val userId = currentUserId

            if (userId == null) {
                _errorMessage.value = "User not logged in"
                return@launch
            }

            // If mining is complete and reward not claimed, claim it
            if (_is24HourMiningActive.value && _remainingTime.value <= 0 && !_finalRewardClaimed.value) {
                claimFinalReward(userId)
                return@launch
            }

            // If mining is already active and not complete, stop it
            if (_is24HourMiningActive.value && _remainingTime.value > 0) {
                stopMining()
                return@launch
            }

            // Start a new mining session
            startMining(userId)
        }
    }

    /**
     * Stops the current mining session
     */
    private fun stopMining() {
        viewModelScope.launch {
            _is24HourMiningActive.value = false
            updateJob?.cancel()
            
            // Clear the mining session
            miningRepository.clearMiningSession()
            
            // Reset UI state
            resetMiningState()
        }
    }

    /**
     * Starts a new 24-hour mining session
     */
    private fun startMining(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = miningRepository.startMining(userId)

            result.onSuccess { sessionData -> 
                _is24HourMiningActive.value = true
                _remainingTime.value = 24 * 60 * 60
                _progressPercentage.value = 0.0
                _sessionReward.value = sessionData.reward
                _sessionEarnings.value = 0.0 // Start with zero earnings
                sessionStartTime = sessionData.startTime
                _finalRewardClaimed.value = false

                // Track analytics
                analyticsManager.trackMiningSessionStart(userId, "24hour_session")

                // Start UI update loop
                startUIUpdateLoop()

                _isLoading.value = false
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to start mining"
                _isLoading.value = false
            }
        }
    }

    /**
     * Claims the final 2 EKH reward
     * (Like claimFinalReward in React Native)
     */
    private fun claimFinalReward(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = miningRepository.claimFinalReward(userId, _sessionReward.value)

            result.onSuccess {
                _finalRewardClaimed.value = true
                _is24HourMiningActive.value = false
                _sessionEarnings.value = _sessionReward.value // Set to full reward when claimed

                // Track analytics
                analyticsManager.trackMiningSessionEnd(
                        userId,
                        "24hour_session",
                        _sessionReward.value,
                        24 * 60 * 60
                )

                // Clear session after successful claim
                miningRepository.clearMiningSession()

                // Update user profile immediately
                val currentProfile = _userProfile.value
                if (currentProfile is Resource.Success) {
                    val newBalance = currentProfile.data.totalCoins + _sessionReward.value.toFloat()
                    updateUserProfileBalance(newBalance)
                }
                
                // Also send event to refresh user profile from server
                viewModelScope.launch {
                    EventBus.sendEvent(Event.RefreshUserProfile)
                }

                // Reset UI state after a short delay
                viewModelScope.launch {
                    delay(2000) // Wait 2 seconds to show success
                    resetMiningState()
                }

                _isLoading.value = false
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to claim reward"
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates UI every second while mining is active
     * Recalculates remaining time, progress, and earnings
     */
    private fun startUIUpdateLoop() {
        updateJob?.cancel()

        updateJob = viewModelScope.launch {
            while (_is24HourMiningActive.value && _remainingTime.value > 0) {
                // Check session status from repository
                val result = miningRepository.checkOngoingMiningSession()

                result.onSuccess { status ->
                    if (status != null) {
                        _remainingTime.value = status.remainingSeconds
                        _progressPercentage.value = status.progress * 100
                        // Update real-time earnings
                        _sessionEarnings.value = calculateCurrentEarnings(status.startTime, status.duration, status.reward)

                        // If completed, stop the loop and update UI
                        if (status.isComplete) {
                            _remainingTime.value = 0
                            _progressPercentage.value = 100.0
                            _sessionEarnings.value = status.reward // Set to full reward when complete
                            updateJob?.cancel()
                        }
                    }
                }

                // Update every second
                delay(1000)
            }
        }
    }

    /**
     * Resets mining state to initial values
     */
    private fun resetMiningState() {
        _is24HourMiningActive.value = false
        _remainingTime.value = 24 * 60 * 60
        _progressPercentage.value = 0.0
        _sessionReward.value = 2.0
        _sessionEarnings.value = 0.0 // Reset earnings
        _finalRewardClaimed.value = false
        sessionStartTime = 0
    }

    /**
     * Formats time in HH:MM:SS format
     */
    fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    /**
     * Manually refreshes mining status
     */
    fun refreshMiningStatus() {
        viewModelScope.launch {
            val result = miningRepository.checkOngoingMiningSession()

            result.onSuccess { status ->
                if (status != null) {
                    _remainingTime.value = status.remainingSeconds
                    _progressPercentage.value = status.progress * 100
                    _finalRewardClaimed.value = status.finalRewardClaimed
                    
                    // Update real-time earnings
                    if (!status.finalRewardClaimed && !status.isComplete) {
                        _sessionEarnings.value = calculateCurrentEarnings(status.startTime, status.duration, status.reward)
                    } else if (status.isComplete) {
                        _sessionEarnings.value = status.reward // Set to full reward when complete
                    }
                    
                    // If session is complete, update UI accordingly
                    if (status.isComplete) {
                        _remainingTime.value = 0
                        _progressPercentage.value = 100.0
                    }
                    
                    // If session is active, start UI update loop
                    if (status.remainingSeconds > 0 && !status.isComplete) {
                        _is24HourMiningActive.value = true
                        sessionStartTime = status.startTime
                        startUIUpdateLoop()
                    }
                } else {
                    // No session, reset UI
                    resetMiningState()
                }
            }
        }
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Refreshes the user profile
     */
    fun refreshUserProfile() {
        viewModelScope.launch {
            val userId = currentUserId
            if (userId != null) {
                loadUserProfile(userId)
            }
        }
    }
    
    /**
     * Updates the user profile balance in the UI (does not persist to database)
     * @param newBalance The new balance to display
     */
    fun updateUserProfileBalance(newBalance: Float) {
        val currentProfile = _userProfile.value
        if (currentProfile is Resource.Success) {
            val profile = currentProfile.data
            val updatedProfile = profile.copy(
                totalCoins = newBalance
            )
            _userProfile.value = Resource.Success(updatedProfile)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop UI updates when ViewModel is destroyed
        updateJob?.cancel()
        // Note: Mining continues - session data is in SharedPreferences!
    }
}