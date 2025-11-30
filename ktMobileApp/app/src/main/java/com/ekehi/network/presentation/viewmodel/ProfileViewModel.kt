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
import com.ekehi.network.domain.usecase.LeaderboardUseCase
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
        private val leaderboardUseCase: LeaderboardUseCase,
        private val syncManager: SyncManager,
        private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile

    // Add a state flow for user rank
    private val _userRank = MutableStateFlow<Resource<Int>>(Resource.Loading)
    val userRank: StateFlow<Resource<Int>> = _userRank

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
            if (userId.isNullOrEmpty() || userId == "user_id_placeholder") {
                Log.e("ProfileViewModel", "Cannot refresh profile: userId is invalid")
                // Try to get user ID again
                try {
                    val result = authRepository.getCurrentUser()
                    result.onSuccess { user ->
                        currentUserId = user.id
                        loadUserProfile(user.id)
                    }.onFailure { error ->
                        _userProfile.value = Resource.Error("Failed to get current user: ${error.message}")
                    }
                } catch (e: Exception) {
                    _userProfile.value = Resource.Error("User not logged in")
                }
                return@launch
            }

            Log.d("ProfileViewModel", "Refreshing profile for user: $userId")
            // Get current user email and pass it to loadUserProfile
            try {
                val result = authRepository.getCurrentUser()
                result.onSuccess { user ->
                    loadUserProfile(userId)
                }.onFailure { error ->
                    // If we can't get the user, still refresh with just the userId
                    loadUserProfile(userId)
                }
            } catch (e: Exception) {
                // If we can't get the user, still refresh with just the userId
                loadUserProfile(userId)
            }
        }
    }

    fun loadUserProfile(userId: String) {
        if (userId.isNullOrEmpty() || userId == "user_id_placeholder") {
            Log.e("ProfileViewModel", "❌ Cannot load profile: userId is invalid")
            _userProfile.value = Resource.Error("User ID is invalid")
            return
        }
        
        currentUserId = userId

        viewModelScope.launch {
            Log.d("ProfileViewModel", "=== LOADING PROFILE FOR USER: $userId ===")
            _userProfile.value = Resource.Loading
            _userRank.value = Resource.Loading

            // Try to get offline data first
            if (userRepository is OfflineUserRepository) {
                userRepository.getOfflineUserProfile(userId).collect { profile ->
                    if (profile != null) {
                        Log.d("ProfileViewModel", "✅ Loaded offline profile: ${profile.username}")
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
                        Log.d("ProfileViewModel", "✅ Profile loaded successfully: ${profile.username}, Coins: ${profile.totalCoins}")
                        
                        // Update streak when profile is loaded
                        Log.d("ProfileViewModel", "=== UPDATING STREAK ===")
                        userUseCase.updateStreak(userId, profile).collect { streakResource ->
                            when (streakResource) {
                                is Resource.Success -> {
                                    Log.d("ProfileViewModel", "✅ Streak updated: current=${streakResource.data.currentStreak}, longest=${streakResource.data.longestStreak}")
                                    // Update the profile with the new streak data
                                    _userProfile.value = Resource.Success(streakResource.data)
                                    // Load user rank after profile is loaded
                                    loadUserRank(userId)
                                }
                                is Resource.Error -> {
                                    Log.e("ProfileViewModel", "❌ Failed to update streak: ${streakResource.message}")
                                    // Still show the original profile even if streak update fails
                                    _userProfile.value = Resource.Success(profile)
                                    // Load user rank after profile is loaded
                                    loadUserRank(userId)
                                }
                                else -> {
                                    // For Loading or Idle states, show the original profile
                                    Log.d("ProfileViewModel", "Streak update state: ${streakResource.javaClass.simpleName}")
                                    _userProfile.value = Resource.Success(profile)
                                    // Load user rank after profile is loaded
                                    loadUserRank(userId)
                                }
                            }
                        }
                        
                        if (userRepository is OfflineUserRepository) {
                            userRepository.cacheUserProfile(profile)
                        }
                        subscribeToUserProfileUpdates(userId)
                    } else {
                        Log.e("ProfileViewModel", "❌ Profile is null despite success")
                        Log.d("ProfileViewModel", "🔧 Creating new profile")
                        createNewUserProfile(userId)
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("ProfileViewModel", "❌ Failed to load profile: ${error?.message}")
                    // Create profile for ANY error - this ensures profile is created even if it failed during registration
                    Log.d("ProfileViewModel", "🔧 Creating new profile for user: $userId")
                    createNewUserProfile(userId)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Exception loading profile: ${e.message}", e)
                // Also create profile on exception
                Log.d("ProfileViewModel", "🔧 Creating new profile after exception")
                createNewUserProfile(userId)
            }
        }
    }

    private fun loadUserRank(userId: String) {
        viewModelScope.launch {
            leaderboardUseCase.getUserRank(userId).collect { resource ->
                _userRank.value = resource
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
                    // Get current user email and pass it to loadUserProfile
                    try {
                        val result = authRepository.getCurrentUser()
                        result.onSuccess { user ->
                            loadUserProfile(userId)
                        }.onFailure { error ->
                            // If we can't get the user, still refresh with just the userId
                            loadUserProfile(userId)
                        }
                    } catch (e: Exception) {
                        // If we can't get the user, still refresh with just the userId
                        loadUserProfile(userId)
                    }
                }
                is SyncManager.SyncResult.Failure -> {
                    Log.e("ProfileViewModel", "Sync failed: ${result.message}")
                    _userProfile.value = Resource.Error("Sync failed: ${result.message}")
                }
            }
        }
    }

    /**
     * Creates a new user profile for the given user ID
     */
    private fun createNewUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "🔧 Creating new profile for user: $userId")
                // Get the user's name from the auth repository
                val userResult = authRepository.getCurrentUser()
                userResult.onSuccess { user ->
                    val username = user.name.ifEmpty { "User${userId.take(6)}" }
                    Log.d("ProfileViewModel", "Creating profile with username: $username")
                    
                    val createResult = userRepository.createUserProfile(userId, username)
                    if (createResult.isSuccess) {
                        val profile = createResult.getOrNull()
                        if (profile != null) {
                            Log.d("ProfileViewModel", "✅ New profile created successfully: ${profile.username}")
                            _userProfile.value = Resource.Success(profile)
                            
                            // Update streak for new profile
                            Log.d("ProfileViewModel", "=== UPDATING STREAK FOR NEW PROFILE ===")
                            userUseCase.updateStreak(userId, profile).collect { streakResource ->
                                when (streakResource) {
                                    is Resource.Success -> {
                                        Log.d("ProfileViewModel", "✅ Streak updated for new profile: current=${streakResource.data.currentStreak}")
                                        _userProfile.value = Resource.Success(streakResource.data)
                                    }
                                    is Resource.Error -> {
                                        Log.e("ProfileViewModel", "❌ Failed to update streak for new profile: ${streakResource.message}")
                                        _userProfile.value = Resource.Success(profile)
                                    }
                                    else -> {
                                        Log.d("ProfileViewModel", "Streak update state for new profile: ${streakResource.javaClass.simpleName}")
                                        _userProfile.value = Resource.Success(profile)
                                    }
                                }
                            }
                        } else {
                            Log.e("ProfileViewModel", "❌ Created profile is null")
                            _userProfile.value = Resource.Error("Failed to create user profile: profile is null")
                        }
                    } else {
                        val error = createResult.exceptionOrNull()
                        Log.e("ProfileViewModel", "❌ Failed to create profile: ${error?.message}")
                        _userProfile.value = Resource.Error("Failed to create user profile: ${error?.message}")
                    }
                }.onFailure { error ->
                    Log.e("ProfileViewModel", "❌ Failed to get user info for profile creation: ${error.message}")
                    _userProfile.value = Resource.Error("Failed to get user info: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Exception creating new profile: ${e.message}", e)
                _userProfile.value = Resource.Error("Exception creating profile: ${e.message}")
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
            
            // Validate userId
            if (userId.isEmpty()) {
                val errorMessage = "User ID is required to update profile"
                Log.e("ProfileViewModel", errorMessage)
                _userProfile.value = Resource.Error(errorMessage)
                return@launch
            }
            
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
                val errorMessage = "Failed to update user profile: ${error?.message ?: "Unknown error"}"
                Log.e("ProfileViewModel", errorMessage)
                _userProfile.value = Resource.Error(errorMessage)
            }
        }
    }

    fun updateUserProfileByDocumentId(documentId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Updating profile by document ID: $documentId")
            
            // Validate documentId
            if (documentId.isEmpty()) {
                val errorMessage = "Document ID is required to update profile"
                Log.e("ProfileViewModel", errorMessage)
                _userProfile.value = Resource.Error(errorMessage)
                return@launch
            }
            
            val result = userRepository.updateUserProfileByDocumentId(documentId, updates)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    Log.d("ProfileViewModel", "Profile updated successfully by document ID")
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
                val errorMessage = "Failed to update user profile: ${error?.message ?: "Unknown error"}"
                Log.e("ProfileViewModel", errorMessage)
                _userProfile.value = Resource.Error(errorMessage)
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Updating password")
            val result = authRepository.updatePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                Log.d("ProfileViewModel", "Password updated successfully")
                // Password update was successful
            } else {
                val error = result.exceptionOrNull()
                Log.e("ProfileViewModel", "Failed to update password: ${error?.message}")
                // Handle error - in a real implementation, you might want to emit this to the UI
            }
        }
    }
    
    /**
     * Claims a referral code for the current user
     * @param userId The ID of the user claiming the referral
     * @param referralCode The referral code to claim
     */
    fun claimReferral(userId: String, referralCode: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Claiming referral code: $referralCode for user: $userId")
            
            // Validate input
            if (userId.isEmpty()) {
                Log.e("ProfileViewModel", "User ID is required to claim referral")
                return@launch
            }
            
            if (referralCode.isBlank()) {
                Log.e("ProfileViewModel", "Referral code cannot be empty")
                return@launch
            }
            
            val result = userRepository.claimReferral(userId, referralCode.trim())
            if (result.isSuccess) {
                val message = result.getOrNull()
                Log.d("ProfileViewModel", "Referral claimed successfully: $message")
                // Refresh user profile to reflect changes
                refreshUserProfile()
            } else {
                val error = result.exceptionOrNull()
                Log.e("ProfileViewModel", "Failed to claim referral: ${error?.message}")
                // Handle error - in a real implementation, you might want to emit this to the UI
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d("ProfileViewModel", "ViewModel cleared")
    }
}