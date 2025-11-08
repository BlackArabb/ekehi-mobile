package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "SettingsViewModel"
    }
    
    private val _miningNotificationsEnabled = MutableStateFlow(true)
    private val _socialTaskNotificationsEnabled = MutableStateFlow(true)
    private val _referralNotificationsEnabled = MutableStateFlow(true)
    private val _streakNotificationsEnabled = MutableStateFlow(true)
    
    val miningNotificationsEnabled: StateFlow<Boolean> = _miningNotificationsEnabled
    val socialTaskNotificationsEnabled: StateFlow<Boolean> = _socialTaskNotificationsEnabled
    val referralNotificationsEnabled: StateFlow<Boolean> = _referralNotificationsEnabled
    val streakNotificationsEnabled: StateFlow<Boolean> = _streakNotificationsEnabled

    fun updateMiningNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _miningNotificationsEnabled.value = enabled
            // In a real implementation, this would persist the setting
            Log.d(TAG, "Mining notifications updated: $enabled")
        }
    }
    
    fun updateSocialTaskNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _socialTaskNotificationsEnabled.value = enabled
            // In a real implementation, this would persist the setting
            Log.d(TAG, "Social task notifications updated: $enabled")
        }
    }
    
    fun updateReferralNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _referralNotificationsEnabled.value = enabled
            // In a real implementation, this would persist the setting
            Log.d(TAG, "Referral notifications updated: $enabled")
        }
    }
    
    fun updateStreakNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _streakNotificationsEnabled.value = enabled
            // In a real implementation, this would persist the setting
            Log.d(TAG, "Streak notifications updated: $enabled")
        }
    }

    fun updatePrivacySettings(enabled: Boolean) {
        viewModelScope.launch {
            // In a real implementation, this would update privacy settings
            // For now, we'll just log the action
            Log.d(TAG, "Privacy settings updated: $enabled")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initiating sign out")
                authUseCase.logout().collect { resource ->
                    when (resource) {
                        is com.ekehi.network.domain.model.Resource.Success -> {
                            Log.d(TAG, "Sign out successful")
                        }
                        is com.ekehi.network.domain.model.Resource.Error -> {
                            Log.e(TAG, "Sign out failed: ${resource.message}")
                        }
                        is com.ekehi.network.domain.model.Resource.Loading -> {
                            Log.d(TAG, "Sign out in progress")
                        }
                        is com.ekehi.network.domain.model.Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign out exception: ${e.message}", e)
            }
        }
    }
}