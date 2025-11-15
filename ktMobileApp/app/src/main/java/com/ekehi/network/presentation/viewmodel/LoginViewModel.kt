package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.domain.usecase.AuthUseCase
import com.ekehi.network.domain.usecase.UserUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.security.ErrorHandler
import com.ekehi.network.security.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val userUseCase: UserUseCase,
    private val analyticsManager: AnalyticsManager,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val loginState: StateFlow<Resource<Unit>> = _loginState

    fun login(email: String, password: String) {
        Log.d("LoginViewModel", "Login attempt started with email: $email")
        
        viewModelScope.launch {
            try {
                // Validate inputs before proceeding
                val emailValidation = InputValidator.validateAndSanitizeText(email, 255)
                val passwordValidation = InputValidator.validateAndSanitizeText(password, 100)
                
                Log.d("LoginViewModel", "Input validation completed")
                
                // Check if all validations pass
                if (!emailValidation.isValid) {
                    val errorMessage = "Invalid email: ${emailValidation.errorMessage}"
                    Log.e("LoginViewModel", errorMessage)
                    _loginState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                if (!passwordValidation.isValid) {
                    val errorMessage = "Invalid password: ${passwordValidation.errorMessage}"
                    Log.e("LoginViewModel", errorMessage)
                    _loginState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                // Additional specific validations
                if (!InputValidator.isValidEmail(emailValidation.sanitizedInput)) {
                    val errorMessage = "Please enter a valid email address"
                    Log.e("LoginViewModel", errorMessage)
                    _loginState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                // Track login attempt
                analyticsManager.trackLogin("email")
                Log.d("LoginViewModel", "Login attempt tracked in analytics")
                
                // Set loading state
                _loginState.value = Resource.Loading
                Log.d("LoginViewModel", "Set loading state")
                
                // Perform login
                Log.d("LoginViewModel", "Starting login request")
                authUseCase.login(
                    emailValidation.sanitizedInput, 
                    passwordValidation.sanitizedInput
                ).collect { resource -> 
                    Log.d("LoginViewModel", "Received login response: ${resource.javaClass.simpleName}")
                    _loginState.value = resource
                    
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("LoginViewModel", "Login successful")
                            // After successful login, update the user's streak
                            updateStreakAfterLogin()
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Login failed: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("LoginViewModel", "Login in progress")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to login")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "Login exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    fun loginWithGoogle(idToken: String) {
        Log.d("LoginViewModel", "Google login attempt started")
        
        viewModelScope.launch {
            try {
                // Track Google login attempt
                analyticsManager.trackLogin("google")
                Log.d("LoginViewModel", "Google login attempt tracked in analytics")
                
                // Set loading state
                _loginState.value = Resource.Loading
                Log.d("LoginViewModel", "Set loading state for Google login")
                
                // Perform Google login
                Log.d("LoginViewModel", "Starting Google login request")
                authUseCase.loginWithGoogle(idToken).collect { resource -> 
                    Log.d("LoginViewModel", "Received Google login response: ${resource.javaClass.simpleName}")
                    _loginState.value = resource
                    
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("LoginViewModel", "Google login successful")
                            // After successful Google login, update the user's streak
                            updateStreakAfterLogin()
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Google login failed: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("LoginViewModel", "Google login in progress")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to login with Google")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "Google login exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    /**
     * Updates the user's streak after a successful login
     */
    private fun updateStreakAfterLogin() {
        Log.d("LoginViewModel", "=== UPDATING STREAK AFTER LOGIN ===")
        viewModelScope.launch {
            try {
                // Get the current user to update their streak
                authUseCase.getCurrentUserIfLoggedIn().collect { authResource ->
                    when (authResource) {
                        is Resource.Success -> {
                            val user = authResource.data
                            if (user != null) {
                                Log.d("LoginViewModel", "Got current user: ${user.id}, now updating streak")
                                // Get the user profile to update the streak
                                userUseCase.getUserProfile(user.id).collect { profileResource ->
                                    when (profileResource) {
                                        is Resource.Success -> {
                                            Log.d("LoginViewModel", "Got user profile for user: ${user.id}, updating streak")
                                            Log.d("LoginViewModel", "Current streak: ${profileResource.data.currentStreak}")
                                            Log.d("LoginViewModel", "Last login date: ${profileResource.data.lastLoginDate}")
                                            // Update the streak
                                            userUseCase.updateStreak(user.id, profileResource.data).collect { streakResource ->
                                                when (streakResource) {
                                                    is Resource.Success -> {
                                                        Log.d("LoginViewModel", "✅ STREAK UPDATED SUCCESSFULLY AFTER LOGIN")
                                                        Log.d("LoginViewModel", "New streak: ${streakResource.data.currentStreak}")
                                                        Log.d("LoginViewModel", "Last login date: ${streakResource.data.lastLoginDate}")
                                                    }
                                                    is Resource.Error -> {
                                                        Log.e("LoginViewModel", "❌ FAILED TO UPDATE STREAK AFTER LOGIN: ${streakResource.message}")
                                                    }
                                                    else -> {
                                                        // For Loading or Idle states, do nothing
                                                        Log.d("LoginViewModel", "Streak update state: ${streakResource.javaClass.simpleName}")
                                                    }
                                                }
                                            }
                                        }
                                        is Resource.Error -> {
                                            Log.e("LoginViewModel", "Failed to get user profile for streak update: ${profileResource.message}")
                                        }
                                        else -> {
                                            // For Loading or Idle states, do nothing
                                            Log.d("LoginViewModel", "User profile state: ${profileResource.javaClass.simpleName}")
                                        }
                                    }
                                }
                            } else {
                                Log.e("LoginViewModel", "Failed to get current user: user is null")
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Failed to get current user for streak update: ${authResource.message}")
                        }
                        else -> {
                            // For Loading or Idle states, do nothing
                            Log.d("LoginViewModel", "Current user state: ${authResource.javaClass.simpleName}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception updating streak after login: ${e.message}", e)
            }
        }
    }
    
    fun checkCurrentUser() {
        Log.d("LoginViewModel", "=== CHECKING CURRENT USER ===")
        
        viewModelScope.launch {
            try {
                // First check if there's an active session
                Log.d("LoginViewModel", "Step 1: Checking for active session")
                
                authUseCase.hasActiveSession().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("LoginViewModel", "Step 1 SUCCESS: Active session check returned ${resource.data}")
                            if (resource.data) {
                                Log.d("LoginViewModel", "✅✅✅ SUCCESS: Active session found, user is logged in")
                                // If we have an active session, user is authenticated
                                _loginState.value = Resource.Success(Unit)
                            } else {
                                Log.d("LoginViewModel", "Step 1: No active session found, checking stored credentials")
                                // No active session, check stored credentials
                                checkStoredCredentials()
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Step 1 ERROR: Error checking active session: ${resource.message}")
                            // Error checking session, fall back to stored credentials
                            checkStoredCredentials()
                        }
                        else -> {
                            // For Loading or Idle states, do nothing
                            Log.d("LoginViewModel", "Step 1: Active session check in progress...")
                        }
                    }
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to verify user")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "Step 1 EXCEPTION: User verification exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }

    private fun checkStoredCredentials() {
        viewModelScope.launch {
            try {
                // Check if we have valid stored credentials
                Log.d("LoginViewModel", "Step 2: Checking stored credentials")
                
                authUseCase.checkStoredCredentials().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("LoginViewModel", "Step 2 SUCCESS: Stored credentials check returned ${resource.data}")
                            if (resource.data) {
                                Log.d("LoginViewModel", "Step 2: Valid stored credentials found, verifying with Appwrite")
                                // If we have valid stored credentials, check if the session is still valid
                                verifyCurrentUser()
                            } else {
                                Log.d("LoginViewModel", "❌❌❌ Step 2: No valid stored credentials found, user needs to login")
                                // No valid stored credentials, user needs to login
                                _loginState.value = Resource.Error("No valid stored credentials")
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Step 2 ERROR: Error checking stored credentials: ${resource.message}")
                            // Error checking credentials, navigate to login
                            _loginState.value = Resource.Error(resource.message)
                        }
                        else -> {
                            // For Loading or Idle states, do nothing
                            Log.d("LoginViewModel", "Step 2: Stored credentials check in progress...")
                        }
                    }
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to check stored credentials")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "Step 2 EXCEPTION: Stored credentials check exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }

    private fun verifyCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Step 3: Verifying current user with Appwrite")
                
                authUseCase.getCurrentUser().collect { resource -> 
                    Log.d("LoginViewModel", "Step 3: Received current user response: ${resource.javaClass.simpleName}")
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("LoginViewModel", "✅✅✅ Step 3 SUCCESS: Current user verified successfully")
                            // User is authenticated, navigate to dashboard
                            _loginState.value = Resource.Success(Unit)
                            // Update streak when user is verified
                            updateStreakAfterLogin()
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "❌❌❌ Step 3 ERROR: Failed to verify current user: ${resource.message}")
                            // User is not authenticated, show error
                            _loginState.value = Resource.Error(resource.message)
                        }
                        is Resource.Loading -> {
                            Log.d("LoginViewModel", "Step 3: User verification in progress")
                            _loginState.value = resource
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                            _loginState.value = resource
                        }
                    }
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to verify user")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "Step 3 EXCEPTION: User verification exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _loginState.value = Resource.Idle
    }
}