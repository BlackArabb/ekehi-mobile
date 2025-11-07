package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.data.repository.offline.OfflineUserRepository
import com.ekehi.network.data.sync.SyncManager
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.domain.usecase.UserUseCase
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val authRepository: AuthRepository,
        private val userUseCase: UserUseCase,
        private val syncManager: SyncManager,
        private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile

    private var currentUserId: String? = null

    init {
        // Get current user and load profile automatically
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUser()
                result.onSuccess { user ->
                    currentUserId = user.id
                    Log.d("ProfileViewModel", "User ID from auth: ${user.id}")
                    loadUserProfile(user.id)
                }.onFailure { error ->
                    Log.e("ProfileViewModel", "Failed to get current user: ${error.message}")
                    _userProfile.value = Resource.Error("Failed to get current user: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception getting current user", e)
                _userProfile.value = Resource.Error("User not logged in")
            }
        }

        // Listen for refresh events
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is Event.RefreshUserProfile -> {
                        Log.d("ProfileViewModel", "Received RefreshUserProfile event")
                        refreshUserProfile()
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Refreshes the current user's profile
     */
    fun refreshUserProfile() {
        viewModelScope.launch {
            val userId = currentUserId
            if (userId == null) {
                Log.e("ProfileViewModel", "Cannot refresh profile: userId is null")
                // Try to get user ID again
                try {
                    val result = authRepository.getCurrentUser()
                    result.onSuccess { user ->
                        currentUserId = user.id
                        loadUserProfile(user.id)
                    }.onFailure { error ->
                        _userProfile.value = Resource.Error("User not logged in")
                    }
                } catch (e: Exception) {
                    _userProfile.value = Resource.Error("User not logged in")
                }
                return@launch
            }

            Log.d("ProfileViewModel", "Refreshing profile for user: $userId")
            loadUserProfile(userId)
        }
    }

    fun loadUserProfile(userId: String) {
        currentUserId = userId

        viewModelScope.launch {
            Log.d("ProfileViewModel", "Loading profile for user: $userId")
            _userProfile.value = Resource.Loading

            // Try to get offline data first
            if (userRepository is OfflineUserRepository) {
                userRepository.getOfflineUserProfile(userId).collect { profile ->
                    if (profile != null) {
                        Log.d("ProfileViewModel", "Loaded offline profile: ${profile.username}")
                        _userProfile.value = Resource.Success(profile)
                    }
                }
            }

            // Then try to sync with server
            try {
                val result = if (userRepository is OfflineUserRepository) {
                    userRepository.syncUserProfile(userId)
                } else {
                    userRepository.getUserProfile(userId)
                }

                if (result.isSuccess) {
                    val profile = result.getOrNull()
                    if (profile != null) {
                        Log.d("ProfileViewModel", "Profile synced successfully: ${profile.username}, Coins: ${profile.totalCoins}")
                        
                        // Update streak when profile is loaded
                        userUseCase.updateStreak(userId, profile).collect { streakResource ->
                            when (streakResource) {
                                is Resource.Success -> {
                                    Log.d("ProfileViewModel", "✅ Streak updated: current=${streakResource.data.currentStreak}, longest=${streakResource.data.longestStreak}")
                                    // Update the profile with the new streak data
                                    _userProfile.value = Resource.Success(streakResource.data)
                                }
                                is Resource.Error -> {
                                    Log.e("ProfileViewModel", "❌ Failed to update streak: ${streakResource.message}")
                                    // Still show the original profile even if streak update fails
                                    _userProfile.value = Resource.Success(profile)
                                }
                                else -> {
                                    // For Loading or Idle states, show the original profile
                                    _userProfile.value = Resource.Success(profile)
                                }
                            }
                        }
                        
                        if (userRepository is OfflineUserRepository) {
                            userRepository.cacheUserProfile(profile)
                        }
                        subscribeToUserProfileUpdates(userId)
                    } else {
                        Log.e("ProfileViewModel", "Profile is null despite success")
                        _userProfile.value = Resource.Error("User profile not found")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("ProfileViewModel", "Failed to load profile: ${error?.message}")
                    _userProfile.value = Resource.Error("Failed to load user profile: ${error?.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception loading profile", e)
                _userProfile.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    fun syncUserProfile(userId: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Syncing profile for user: $userId")
            val result = syncManager.syncAllData(userId)
            when (result) {
                is SyncManager.SyncResult.Success -> {
                    Log.d("ProfileViewModel", "Sync successful, reloading profile")
                    loadUserProfile(userId)
                }
                is SyncManager.SyncResult.Failure -> {
                    Log.e("ProfileViewModel", "Sync failed: ${result.message}")
                    _userProfile.value = Resource.Error("Sync failed: ${result.message}")
                }
            }
        }
    }

    private fun subscribeToUserProfileUpdates(userId: String) {
        // Removed realtime functionality for now
        // In a real implementation, you would subscribe to user profile updates
    }

    private fun handleRealtimeEvent(event: Any) {
        // Handle real-time updates to the user profile
        // In a real implementation, you would update the UI based on the event
    }

    fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Updating profile for user: $userId")
            val result = userRepository.updateUserProfile(userId, updates)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    Log.d("ProfileViewModel", "Profile updated successfully")
                    _userProfile.value = Resource.Success(profile)
                    // Cache the updated profile if we're using offline repository
                    if (userRepository is OfflineUserRepository) {
                        userRepository.cacheUserProfile(profile)
                    }
                } else {
                    Log.e("ProfileViewModel", "Updated profile is null")
                    _userProfile.value = Resource.Error("Failed to update profile")
                }
            } else {
                val error = result.exceptionOrNull()
                Log.e("ProfileViewModel", "Failed to update profile: ${error?.message}")
                _userProfile.value = Resource.Error("Failed to update user profile: ${error?.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ProfileViewModel", "ViewModel cleared")
    }
}