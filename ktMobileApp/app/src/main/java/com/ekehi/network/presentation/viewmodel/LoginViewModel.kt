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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
    
    // Fallback scope in case viewModelScope is cancelled
    private val fallbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun login(email: String, password: String) {
        Log.d("LoginViewModel", "Login attempt started with email: $email")
        
        // Set loading state immediately on main thread to ensure UI updates
        _loginState.value = Resource.Loading
        
        // Use fallback scope to ensure coroutine always runs
        // This prevents issues when viewModelScope is cancelled on some devices
        try {
            fallbackScope.launch {
                try {
                    // Validate inputs
                    val emailValidation = InputValidator.validateAndSanitizeText(email, 255)
                    val passwordValidation = InputValidator.validateAndSanitizeText(password, 100)
                    
                    // Check if all validations pass
                    if (!emailValidation.isValid) {
                        val errorMessage = "Invalid email: ${emailValidation.errorMessage}"
                        Log.e("LoginViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _loginState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!passwordValidation.isValid) {
                        val errorMessage = "Invalid password: ${passwordValidation.errorMessage}"
                        Log.e("LoginViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _loginState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    // Additional specific validations
                    if (!InputValidator.isValidEmail(emailValidation.sanitizedInput)) {
                        val errorMessage = "Please enter a valid email address"
                        Log.e("LoginViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _loginState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    // Track login attempt
                    try {
                        analyticsManager.trackLogin("email")
                    } catch (e: Exception) {
                        Log.w("LoginViewModel", "Failed to track login in analytics: ${e.message}")
                        // Don't fail login if analytics fails
                    }
                    
                    // Perform login
                    authUseCase.login(
                        emailValidation.sanitizedInput, 
                        passwordValidation.sanitizedInput
                    ).collect { resource -> 
                        withContext(Dispatchers.Main) {
                            _loginState.value = resource
                        }
                        
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
                                // Loading state already set
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
                    withContext(Dispatchers.Main) {
                        _loginState.value = Resource.Error(errorMessage)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Exception launching coroutine: ${e.message}", e)
            _loginState.value = Resource.Error("Failed to start login process: ${e.message}")
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
    
    /**
     * Checks if there's a current logged-in user with an active session
     * This is called on app startup to determine authentication state
     */
    fun checkCurrentUser() {
        Log.d("LoginViewModel", "=== CHECKING CURRENT USER ===")
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            
            try {
                // Check if there's an active Appwrite session
                authUseCase.hasActiveSession().collect { sessionResource ->
                    when (sessionResource) {
                        is Resource.Success -> {
                            if (sessionResource.data == true) {
                                Log.d("LoginViewModel", "✅ Active session found, getting user data")
                                
                                // Get current user data
                                authUseCase.getCurrentUser().collect { userResource ->
                                    when (userResource) {
                                        is Resource.Success -> {
                                            Log.d("LoginViewModel", "✅ User authenticated")
                                            _loginState.value = Resource.Success(Unit)
                                            
                                            // No analytics tracking needed for auto-login check
                                        }
                                        is Resource.Error -> {
                                            Log.e("LoginViewModel", "❌ Failed to get user data: ${userResource.message}")
                                            _loginState.value = Resource.Error(userResource.message)
                                        }
                                        else -> {
                                            // Handle other states if needed
                                        }
                                    }
                                }

                            } else {
                                Log.d("LoginViewModel", "❌ No active session found")
                                _loginState.value = Resource.Error("No active session")
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "❌ Error checking session: ${sessionResource.message}")
                            _loginState.value = Resource.Error(sessionResource.message)
                        }
                        is Resource.Loading -> {
                            // Already set loading state
                        }
                        is Resource.Idle -> {
                            // Do nothing
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ Exception checking current user: ${e.message}", e)
                _loginState.value = Resource.Error(e.message ?: "Authentication check failed")
            }
        }
    }
    
    /**
     * Resets the login state to Idle
     */
    fun resetState() {
        Log.d("LoginViewModel", "Resetting login state")
        _loginState.value = Resource.Idle
    }
}