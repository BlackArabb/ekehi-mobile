package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.data.model.UserProfile
import com.ekehi.mobile.data.repository.UserRepository
import com.ekehi.mobile.data.repository.offline.OfflineUserRepository
import com.ekehi.mobile.data.sync.SyncManager
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.domain.usecase.RealtimeUseCase
import com.ekehi.mobile.performance.PerformanceMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.models.RealtimeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val realtimeUseCase: RealtimeUseCase,
    private val syncManager: SyncManager,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            performanceMonitor.measureExecutionTime({
                // Try to get offline data first for immediate UI
                if (userRepository is OfflineUserRepository) {
                    userRepository.getOfflineUserProfile(userId).collect { profile ->
                        if (profile != null) {
                            _userProfile.value = Resource.Success(profile)
                        }
                    }
                }
                
                // Then try to sync with server
                val result = if (userRepository is OfflineUserRepository) {
                    userRepository.syncUserProfile(userId)
                } else {
                    userRepository.getUserProfile(userId)
                }
                
                if (result.isSuccess) {
                    val profile = result.getOrNull()
                    if (profile != null) {
                        _userProfile.value = Resource.Success(profile)
                        // Cache the profile if we're using offline repository
                        if (userRepository is OfflineUserRepository) {
                            userRepository.cacheUserProfile(profile)
                        }
                        // Subscribe to real-time updates for this user's profile
                        subscribeToUserProfileUpdates(userId)
                    }
                } else {
                    // If online fails, try to show cached data
                    if (userRepository is OfflineUserRepository) {
                        userRepository.getOfflineUserProfile(userId).collect { profile ->
                            if (profile != null) {
                                _userProfile.value = Resource.Success(profile)
                            } else {
                                _userProfile.value = Resource.Error("Failed to load user profile: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    } else {
                        _userProfile.value = Resource.Error("Failed to load user profile: ${result.exceptionOrNull()?.message}")
                    }
                }
            }, "loadUserProfile")
        }
    }

    fun syncUserProfile(userId: String) {
        viewModelScope.launch {
            performanceMonitor.measureExecutionTime({
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
            }, "syncUserProfile")
        }
    }

    private fun subscribeToUserProfileUpdates(userId: String) {
        viewModelScope.launch {
            realtimeUseCase.subscribeToUserUpdates(userId).collect { event ->
                handleRealtimeEvent(event)
            }
        }
    }

    private fun handleRealtimeEvent(event: RealtimeResponse) {
        // Handle real-time updates to the user profile
        // In a real implementation, you would update the UI based on the event
    }

    fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            performanceMonitor.measureExecutionTime({
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
            }, "updateUserProfile")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any ongoing subscriptions
    }
}