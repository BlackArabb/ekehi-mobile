package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.usecase.UserUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreakViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "StreakViewModel"
    }

    private val _streakUpdateState = MutableStateFlow<Resource<UserProfile>>(Resource.Idle)
    val streakUpdateState: StateFlow<Resource<UserProfile>> = _streakUpdateState

    /**
     * Updates the user's streak when they log in
     * @param userId The user's ID
     * @param userProfile The current user profile
     */
    fun updateStreakOnLogin(userId: String, userProfile: UserProfile) {
        Log.d(TAG, "Updating streak for user: $userId")
        
        viewModelScope.launch {
            try {
                _streakUpdateState.value = Resource.Loading
                
                userUseCase.updateStreak(userId, userProfile).collect { resource ->
                    _streakUpdateState.value = resource
                    
                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "✅ Streak updated successfully for user: $userId")
                            Log.d(TAG, "Current streak: ${resource.data.currentStreak}, Longest streak: ${resource.data.longestStreak}")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "❌ Failed to update streak for user: $userId, error: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Streak update in progress for user: $userId")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Exception updating streak: ${e.message ?: "Unknown error"}"
                Log.e(TAG, errorMessage, e)
                _streakUpdateState.value = Resource.Error(errorMessage)
            }
        }
    }
}