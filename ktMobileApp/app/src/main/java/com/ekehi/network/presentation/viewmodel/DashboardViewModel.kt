package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.performance.PerformanceMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            performanceMonitor.measureExecutionTime({
                // In a real implementation, you would fetch the user profile from a repository
                // For now, we'll create a placeholder profile
                val profile = UserProfile(
                    id = "profile_id",
                    userId = userId,
                    username = "John Doe",
                    taskReward = 500.0f,
                    miningReward = 500.0f,
                    referralReward = 250.5f,
                    autoMiningRate = 0.0833f,
                    miningPower = 1.0f,
                    currentStreak = 12,
                    longestStreak = 15,
                    lastLoginDate = "2023-12-01T00:00:00Z",
                    referralCode = "REF123",
                    referredBy = null,
                    totalReferrals = 5,
                    lifetimeEarnings = 2500.0f,
                    dailyMiningRate = 0.0833f,
                    maxDailyEarnings = 100.0f,
                    todayEarnings = 12.5f,
                    lastMiningDate = "2023-12-01T00:00:00Z",
                    streakBonusClaimed = 1,
                    createdAt = "2023-01-01T00:00:00Z",
                    updatedAt = "2023-12-01T00:00:00Z"

                )
                _userProfile.value = Resource.Success(profile)
            }, "loadUserProfile")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any ongoing subscriptions
    }
}