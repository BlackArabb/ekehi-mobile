package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.data.model.UserProfile
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.performance.PerformanceMonitor
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
                    totalCoins = 1250.5,
                    coinsPerSecond = 0.0833,
                    autoMiningRate = 0.0833,
                    miningPower = 1.0,
                    referralBonusRate = 0.1,
                    currentStreak = 12,
                    longestStreak = 15,
                    lastLoginDate = "2023-12-01T00:00:00Z",
                    referralCode = "REF123",
                    referredBy = null,
                    totalReferrals = 5,
                    lifetimeEarnings = 2500.0,
                    dailyMiningRate = 0.0833,
                    maxDailyEarnings = 100.0,
                    todayEarnings = 12.5,
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