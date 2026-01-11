package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.util.DebugLogger

import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.service.OAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthResolution {
    object AuthenticatedComplete : AuthResolution()
    object AuthenticatedIncomplete : AuthResolution()
    object Unauthenticated : AuthResolution()
}

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val oAuthService: OAuthService
) : ViewModel() {

    private val _oauthState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val oauthState: StateFlow<Resource<Boolean>> = _oauthState

    fun initiateGoogleOAuth(activity: ComponentActivity) {
        DebugLogger.logStep("VM_INITIATE_GOOGLE_OAUTH", "Initiating Google OAuth")
        viewModelScope.launch {
            try {
                _oauthState.value = Resource.Loading
                oAuthService.initiateGoogleOAuth(activity)
                DebugLogger.logStep("VM_GOOGLE_OAUTH_INITIATED", "Google OAuth flow initiated, waiting for callback")
            } catch (e: Exception) {
                DebugLogger.logError("VM_INITIATE_GOOGLE_OAUTH", "Google OAuth error: ${e.message}", e)
                _oauthState.value = Resource.Error("Failed to initiate OAuth: ${e.message}")
            }
        }
    }
    
    fun initiateGoogleOAuthForRegistration(activity: ComponentActivity) {
        DebugLogger.logStep("VM_INITIATE_GOOGLE_OAUTH_REG", "Initiating Google OAuth for registration")
        // This method initiates Google OAuth specifically for registration
        _oauthState.value = Resource.Loading
        // Call the actual OAuth service
        initiateGoogleOAuth(activity)
    }
    
    fun initiateGoogleOAuthForLogin(activity: ComponentActivity) {
        DebugLogger.logStep("VM_INITIATE_GOOGLE_OAUTH_LOGIN", "Initiating Google OAuth for login")
        // This method initiates Google OAuth specifically for login
        _oauthState.value = Resource.Loading
        // Call the actual OAuth service
        initiateGoogleOAuth(activity)
    }
    
    // Variable to track if additional info is needed after OAuth
    private val _requiresAdditionalInfo = MutableStateFlow(false)
    val requiresAdditionalInfo: StateFlow<Boolean> = _requiresAdditionalInfo
    
    fun setRequiresAdditionalInfo(requires: Boolean) {
        _requiresAdditionalInfo.value = requires
    }
    
    fun handleOAuthResult(success: Boolean, errorMessage: String? = null, requiresAdditionalInfo: Boolean = false) {
        DebugLogger.logStep("VM_HANDLE_OAUTH_RESULT", "Handling OAuth result: success=$success, error=$errorMessage, requiresAdditionalInfo=$requiresAdditionalInfo")
        viewModelScope.launch {
            _requiresAdditionalInfo.value = requiresAdditionalInfo
            _oauthState.value = if (success) {
                Resource.Success(true)
            } else {
                Resource.Error(errorMessage ?: "OAuth authentication failed")
            }
        }
    }
    
    fun checkProfileCompleteness(phoneNumber: String, country: String) {
        // Check if required fields are missing
        val isProfileIncomplete = phoneNumber.isEmpty() || country.isEmpty()
        _requiresAdditionalInfo.value = isProfileIncomplete
        
        DebugLogger.logState("VM_PROFILE_COMPLETENESS", "Checked", mapOf(
            "isIncomplete" to isProfileIncomplete,
            "phoneNumber" to phoneNumber,
            "country" to country
        ))
    }
    
    private val _authResolution = MutableStateFlow<AuthResolution?>(null)
    val authResolution: StateFlow<AuthResolution?> = _authResolution
    
    fun onOAuthSuccess() {
        DebugLogger.logStep("VM_OAUTH_SUCCESS", "Checking profile completeness after OAuth")
        viewModelScope.launch {
            try {
                _oauthState.value = Resource.Loading
                
                // Get current user
                DebugLogger.logStep("VM_GET_USER", "Fetching current user")
                val currentUserResult = authRepository.getCurrentUser()
                if (currentUserResult.isSuccess) {
                    val user = currentUserResult.getOrNull()
                    if (user != null) {
                        DebugLogger.logState("VM_USER_FOUND", "Success", mapOf(
                            "userId" to user.id,
                            "email" to user.email,
                            "name" to user.name
                        ))
                        
                        // Fetch user profile to check if additional information is needed
                        DebugLogger.logStep("VM_FETCH_PROFILE", "Getting user profile")
                        val profileResult = userRepository.getUserProfile(user.id)
                        
                        if (profileResult.isSuccess) {
                            val userProfile = profileResult.getOrThrow()
                            // Check if required fields are missing
                            val isProfileIncomplete = userProfile.phoneNumber.isEmpty() || userProfile.country.isEmpty()
                            
                            DebugLogger.logState("VM_PROFILE_CHECK", "Complete", mapOf(
                                "isIncomplete" to isProfileIncomplete,
                                "phoneNumber" to userProfile.phoneNumber,
                                "country" to userProfile.country
                            ))
                            
                            DebugLogger.logStep("VM_RESOLUTION", "Setting resolution to ${if (isProfileIncomplete) "AuthenticatedIncomplete" else "AuthenticatedComplete"}")
                            _authResolution.value = if (isProfileIncomplete) {
                                AuthResolution.AuthenticatedIncomplete
                            } else {
                                AuthResolution.AuthenticatedComplete
                            }
                            
                            _oauthState.value = Resource.Success(true)
                        } else {
                            // If we couldn't get the profile, treat as incomplete
                            DebugLogger.logStep("VM_RESOLUTION", "Setting resolution to AuthenticatedIncomplete (profile fetch failed)")
                            _authResolution.value = AuthResolution.AuthenticatedIncomplete
                            _oauthState.value = Resource.Success(true)
                            val exception = profileResult.exceptionOrNull()
                            DebugLogger.logError("VM_FETCH_PROFILE", "Could not fetch user profile", exception as? Exception ?: Exception(exception?.message ?: "Unknown error"))
                        }
                    } else {
                        // No user found
                        DebugLogger.logStep("VM_RESOLUTION", "Setting resolution to Unauthenticated (no user found)")
                        _authResolution.value = AuthResolution.Unauthenticated
                        _oauthState.value = Resource.Error("No authenticated user found")
                    }
                } else {
                    // Error getting current user
                    DebugLogger.logStep("VM_RESOLUTION", "Setting resolution to Unauthenticated (error getting user)")
                    _authResolution.value = AuthResolution.Unauthenticated
                    _oauthState.value = Resource.Error("Failed to get current user: ${currentUserResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                DebugLogger.logError("VM_OAUTH_SUCCESS", "Error checking profile completeness", e)
                // On error, assume incomplete
                DebugLogger.logStep("VM_RESOLUTION", "Setting resolution to AuthenticatedIncomplete (error case)")
                _authResolution.value = AuthResolution.AuthenticatedIncomplete
                _oauthState.value = Resource.Success(true) // Still consider OAuth successful
            }
        }
    }
    
    fun resetResolution() {
        _authResolution.value = null
    }
    
    fun resetState() {
        DebugLogger.logStep("VM_RESET_STATE", "Resetting OAuth state")
        _oauthState.value = Resource.Idle
    }
}