package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.usecase.AuthUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.security.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val securePreferences: SecurePreferences
) : ViewModel() {
    
    companion object {
        private const val TAG = "SettingsViewModel"
        
        // Notification settings keys
        private const val MINING_NOTIFICATIONS_ENABLED = "mining_notifications_enabled"
        private const val SOCIAL_TASK_NOTIFICATIONS_ENABLED = "social_task_notifications_enabled"
        private const val REFERRAL_NOTIFICATIONS_ENABLED = "referral_notifications_enabled"
        private const val STREAK_NOTIFICATIONS_ENABLED = "streak_notifications_enabled"
        private const val EMAIL_NOTIFICATIONS_ENABLED = "email_notifications_enabled"
        private const val IN_APP_NOTIFICATIONS_ENABLED = "in_app_notifications_enabled"
        private const val PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        private const val ANALYTICS_ENABLED = "analytics_enabled"
    }
    
    // State flows for notification settings
    private val _miningNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(MINING_NOTIFICATIONS_ENABLED, true)
    )
    private val _socialTaskNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(SOCIAL_TASK_NOTIFICATIONS_ENABLED, true)
    )
    private val _referralNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(REFERRAL_NOTIFICATIONS_ENABLED, true)
    )
    private val _streakNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(STREAK_NOTIFICATIONS_ENABLED, true)
    )
    private val _emailNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(EMAIL_NOTIFICATIONS_ENABLED, true)
    )
    private val _inAppNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(IN_APP_NOTIFICATIONS_ENABLED, true)
    )
    private val _pushNotificationsEnabled = MutableStateFlow(
        securePreferences.getBoolean(PUSH_NOTIFICATIONS_ENABLED, true)
    )
    private val _analyticsEnabled = MutableStateFlow(
        securePreferences.getBoolean(ANALYTICS_ENABLED, true)
    )
    
    // Results
    private val _passwordChangeResult = MutableStateFlow<Resource<Unit>?>(null)
    private val _notificationSettingsResult = MutableStateFlow<Resource<Unit>?>(null)
    private val _privacySettingsResult = MutableStateFlow<Resource<Unit>?>(null)
    
    // Public state flows
    val miningNotificationsEnabled: StateFlow<Boolean> = _miningNotificationsEnabled
    val socialTaskNotificationsEnabled: StateFlow<Boolean> = _socialTaskNotificationsEnabled
    val referralNotificationsEnabled: StateFlow<Boolean> = _referralNotificationsEnabled
    val streakNotificationsEnabled: StateFlow<Boolean> = _streakNotificationsEnabled
    val emailNotificationsEnabled: StateFlow<Boolean> = _emailNotificationsEnabled
    val inAppNotificationsEnabled: StateFlow<Boolean> = _inAppNotificationsEnabled
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled
    val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled
    
    val passwordChangeResult: StateFlow<Resource<Unit>?> = _passwordChangeResult
    val notificationSettingsResult: StateFlow<Resource<Unit>?> = _notificationSettingsResult
    val privacySettingsResult: StateFlow<Resource<Unit>?> = _privacySettingsResult

    // Notification settings functions
    fun updateMiningNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _miningNotificationsEnabled.value = enabled
            securePreferences.putBoolean(MINING_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "Mining notifications updated: $enabled")
        }
    }
    
    fun updateSocialTaskNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _socialTaskNotificationsEnabled.value = enabled
            securePreferences.putBoolean(SOCIAL_TASK_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "Social task notifications updated: $enabled")
        }
    }
    
    fun updateReferralNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _referralNotificationsEnabled.value = enabled
            securePreferences.putBoolean(REFERRAL_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "Referral notifications updated: $enabled")
        }
    }
    
    fun updateStreakNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _streakNotificationsEnabled.value = enabled
            securePreferences.putBoolean(STREAK_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "Streak notifications updated: $enabled")
        }
    }
    
    fun updateEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _emailNotificationsEnabled.value = enabled
            securePreferences.putBoolean(EMAIL_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "Email notifications updated: $enabled")
        }
    }
    
    fun updateInAppNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _inAppNotificationsEnabled.value = enabled
            securePreferences.putBoolean(IN_APP_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "In-app notifications updated: $enabled")
        }
    }
    
    fun updatePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _pushNotificationsEnabled.value = enabled
            securePreferences.putBoolean(PUSH_NOTIFICATIONS_ENABLED, enabled)
            saveNotificationSettings()
            Log.d(TAG, "Push notifications updated: $enabled")
        }
    }
    
    // Privacy settings functions
    fun updateAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            _analyticsEnabled.value = enabled
            securePreferences.putBoolean(ANALYTICS_ENABLED, enabled)
            savePrivacySettings()
            Log.d(TAG, "Analytics updated: $enabled")
        }
    }
    
    fun updatePrivacySettings(enabled: Boolean) {
        viewModelScope.launch {
            _analyticsEnabled.value = enabled
            securePreferences.putBoolean(ANALYTICS_ENABLED, enabled)
            savePrivacySettings()
            Log.d(TAG, "Privacy settings updated: $enabled")
        }
    }
    
    // Save settings to persistent storage
    private fun saveNotificationSettings() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving notification settings to persistent storage")
                _notificationSettingsResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save notification settings: ${e.message}", e)
                _notificationSettingsResult.value = Resource.Error("Failed to save notification settings: ${e.message}")
            }
        }
    }
    
    private fun savePrivacySettings() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving privacy settings to persistent storage")
                _privacySettingsResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save privacy settings: ${e.message}", e)
                _privacySettingsResult.value = Resource.Error("Failed to save privacy settings: ${e.message}")
            }
        }
    }
    
    // UI navigation functions
    fun showPrivacyPolicy() {
        viewModelScope.launch {
            Log.d(TAG, "Showing privacy policy")
            // In a real implementation, this would navigate to the privacy policy screen
        }
    }

    fun showDataManagement() {
        viewModelScope.launch {
            Log.d(TAG, "Showing data management")
            // In a real implementation, this would navigate to the data management screen
        }
    }

    fun showLoginHistory() {
        viewModelScope.launch {
            Log.d(TAG, "Showing login history")
            // In a real implementation, this would navigate to the login history screen
        }
    }

    fun showTermsOfService() {
        viewModelScope.launch {
            Log.d(TAG, "Showing terms of service")
            // In a real implementation, this would navigate to the terms of service screen
        }
    }

    fun showVersionInfo() {
        viewModelScope.launch {
            Log.d(TAG, "Showing version info")
            // In a real implementation, this would show version information
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initiating password change")
                Log.d(TAG, "Current password length: ${currentPassword.length}")
                Log.d(TAG, "New password length: ${newPassword.length}")
                _passwordChangeResult.value = Resource.Loading
                authUseCase.updatePassword(currentPassword, newPassword).collect { resource ->
                    _passwordChangeResult.value = resource
                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Password change successful")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Password change failed: ${resource.message}")
                            // Also log the full exception if available
                            if (resource.message?.contains("Exception") == true) {
                                Log.e(TAG, "Full error details: ${resource.message}")
                            }
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Password change in progress")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Password change exception: ${e.message}", e)
                _passwordChangeResult.value = Resource.Error("Password change failed: ${e.message}")
            }
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