package com.ekehi.network.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.util.DebugLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

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

    fun handleGoogleSignInResult(activity: ComponentActivity, data: Intent?) {
        DebugLogger.logStep("VM_HANDLE_GOOGLE_SIGN_IN_RESULT", "Processing Google sign-in result")
        
        if (data == null) {
            DebugLogger.logError("VM_HANDLE_RESULT", "Intent data is null", Exception("Null intent data"))
            _oauthState.value = Resource.Error("Authentication failed: No data received")
            return
        }

        viewModelScope.launch {
            try {
                _oauthState.value = Resource.Loading
                
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                
                DebugLogger.logState("VM_GOOGLE_RESULT", "Parsed", mapOf(
                    "account" to (account?.email ?: "null"),
                    "hasIdToken" to (idToken != null)
                ))
                
                if (idToken != null) {
                    DebugLogger.logStep("VM_GOOGLE_ID_TOKEN_RECEIVED", "Received token, creating Appwrite session")
                    val result = oAuthService.handleNativeGoogleLogin(idToken)
                    
                    if (result.isSuccess) {
                        val resultMap = result.getOrThrow()
                        val isProfileComplete = resultMap["isProfileComplete"] as? Boolean ?: false
                        
                        DebugLogger.logStep("VM_NATIVE_LOGIN_SUCCESS", "Session created successfully. Profile complete: $isProfileComplete")
                        
                        _requiresAdditionalInfo.value = !isProfileComplete
                        _oauthState.value = Resource.Success(true)
                        
                        // Set resolution for navigation
                        _authResolution.value = if (isProfileComplete) {
                            AuthResolution.AuthenticatedComplete
                        } else {
                            AuthResolution.AuthenticatedIncomplete
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Appwrite session creation failed"
                        DebugLogger.logError("VM_NATIVE_LOGIN_FAILED", error, result.exceptionOrNull() as? Exception ?: Exception(error))
                        
                        // Fallback to browser-based OAuth if native login fails
                        DebugLogger.logStep("VM_FALLBACK", "Native login failed, falling back to browser OAuth")
                        initiateBrowserOAuth(activity)
                    }
                } else {
                    DebugLogger.logError("VM_NATIVE_LOGIN_FAILED", "No ID token received from Google", Exception("No ID token"))
                    // Fallback to browser-based OAuth
                    initiateBrowserOAuth(activity)
                }
            } catch (e: ApiException) {
                val statusCode = e.statusCode
                DebugLogger.logError("VM_GOOGLE_SIGN_IN_API_ERROR", "Status Code: $statusCode", e)
                
                // 12501: SIGN_IN_CANCELLED
                // 12502: SIGN_IN_CURRENTLY_IN_PROGRESS
                // 7: NETWORK_ERROR
                if (statusCode == 12501) { 
                    DebugLogger.logStep("VM_GOOGLE_SIGN_IN_CANCELLED", "User cancelled sign-in")
                    _oauthState.value = Resource.Idle
                } else if (statusCode == 12502) {
                    DebugLogger.logStep("VM_GOOGLE_SIGN_IN_PROGRESS", "Sign-in already in progress")
                    // Don't change state, let it continue
                } else {
                    val error = "Google Sign-In failed (Code: $statusCode)"
                    // For other errors, try fallback to browser
                    DebugLogger.logStep("VM_FALLBACK", "Google API error, falling back to browser OAuth")
                    initiateBrowserOAuth(activity)
                }
            } catch (e: Exception) {
                DebugLogger.logError("VM_HANDLE_GOOGLE_SIGN_IN_RESULT", "Unexpected error: ${e.message}", e)
                // Fallback to browser-based OAuth
                initiateBrowserOAuth(activity)
            }
        }
    }

    private fun initiateBrowserOAuth(activity: ComponentActivity) {
        viewModelScope.launch {
            try {
                _oauthState.value = Resource.Loading
                // We'll call a dedicated method in OAuthService or just use createOAuth2Token directly
                // For now, let's assume initiateGoogleOAuth has a way to force browser or we add a new method
                oAuthService.initiateGoogleOAuth(activity, forceBrowser = true)
            } catch (e: Exception) {
                _oauthState.value = Resource.Error("OAuth initiation failed: ${e.message}")
            }
        }
    }

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