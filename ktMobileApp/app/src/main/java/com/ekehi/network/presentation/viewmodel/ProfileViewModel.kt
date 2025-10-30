package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.data.repository.offline.OfflineUserRepository
import com.ekehi.network.data.sync.SyncManager
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val syncManager: SyncManager,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile

    private var currentUserId: String? = null

    init {
        // Listen for refresh events
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is Event.RefreshUserProfile -> {
                        currentUserId?.let { userId ->
                            loadUserProfile(userId)
                        }
                    }
                    else -> {
                        // Handle other events if needed
                    }
                }
            }
        }
    }

    fun loadUserProfile(userId: String) {
        currentUserId = userId
        
        viewModelScope.launch {
            // Try to get offline data first
            if (userRepository is OfflineUserRepository) {
                userRepository.getOfflineUserProfile(userId).collect { profile ->
                    if (profile != null) {
                        _userProfile.value = Resource.Success(profile)
                    }
                }
            }

            // Then try to sync
            val result = if (userRepository is OfflineUserRepository) {
                userRepository.syncUserProfile(userId)
            } else {
                userRepository.getUserProfile(userId)
            }

            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    _userProfile.value = Resource.Success(profile)
                    if (userRepository is OfflineUserRepository) {
                        userRepository.cacheUserProfile(profile)
                    }
                    subscribeToUserProfileUpdates(userId)
                }
            } else {
                _userProfile.value = Resource.Error("Failed to load user profile: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun syncUserProfile(userId: String) {
        viewModelScope.launch {
            val result = syncManager.syncAllData(userId)
            when (result) {
                is SyncManager.SyncResult.Success -> {
                    // Reload the profile after successful sync
                    loadUserProfile(userId)
                }
                is SyncManager.SyncResult.Failure -> {
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
            val result = userRepository.updateUserProfile(userId, updates)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    _userProfile.value = Resource.Success(profile)
                    // Cache the updated profile if we're using offline repository
                    if (userRepository is OfflineUserRepository) {
                        userRepository.cacheUserProfile(profile)
                    }
                }
            } else {
                _userProfile.value = Resource.Error("Failed to update user profile: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any ongoing subscriptions
    }
}