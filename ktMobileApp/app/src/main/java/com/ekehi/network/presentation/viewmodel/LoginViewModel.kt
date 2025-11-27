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
    
    fun checkCurrentUser() {
        Log.d("LoginViewModel", "=== CHECKING CURRENT USER ===")
        
        viewModelScope.launch {
            try {
                // Add a small delay to ensure the session is fully established
                kotlinx.coroutines.delay(500)
                
                // First check if there's an active session and user profile exists
                Log.d("LoginViewModel", "Step 1: Checking for active session and user profile")
                
                // Try up to 3 times with delays to allow profile creation to complete
                var attempts = 0
                while (attempts < 3) {
                    authUseCase.isAuthenticatedWithProfile().collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                Log.d("LoginViewModel", "Step 1 SUCCESS: Authentication with profile check returned ${resource.data} (attempt ${attempts + 1})")
                                if (resource.data) {
                                    Log.d("LoginViewModel", "✅✅✅ SUCCESS: User is authenticated with profile, navigating to dashboard")
                                    // User is authenticated with profile, navigate to dashboard
                                    _loginState.value = Resource.Success(Unit)
                                    return@collect // Exit the collect block
                                } else {
                                    Log.d("LoginViewModel", "Step 1: User is not fully authenticated (attempt ${attempts + 1})")
                                    if (attempts < 2) {
                                        // Wait before retrying
                                        kotlinx.coroutines.delay(1000)
                                    } else {
                                        // Last attempt, fall back to stored credentials
                                        Log.d("LoginViewModel", "Step 1: All attempts failed, checking stored credentials")
                                        checkStoredCredentials()
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Log.e("LoginViewModel", "Step 1 ERROR: Error checking authentication with profile: ${resource.message}")
                                // Error checking authentication, fall back to stored credentials
                                checkStoredCredentials()
                                return@collect // Exit the collect block
                            }
                            else -> {
                                // For Loading or Idle states, do nothing
                                Log.d("LoginViewModel", "Step 1: Authentication with profile check in progress... (attempt ${attempts + 1})")
                            }
                        }
                    }
                    attempts++
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to verify user")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "Step 1 EXCEPTION: User verification exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Getting current user data")
                
                authUseCase.getCurrentUserIfLoggedIn().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            if (resource.data != null) {
                                Log.d("LoginViewModel", "✅✅✅ Current user data retrieved successfully: ${resource.data.id}")
                                // User is authenticated with data, navigate to dashboard
                                _loginState.value = Resource.Success(Unit)
                            } else {
                                Log.d("LoginViewModel", "❌❌❌ No user data found, user needs to login")
                                // No user data, user needs to login
                                _loginState.value = Resource.Error("No user data found")
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "❌❌❌ ERROR: Failed to get current user: ${resource.message}")
                            // User is not authenticated, show error
                            _loginState.value = Resource.Error(resource.message)
                        }
                        is Resource.Loading -> {
                            Log.d("LoginViewModel", "User data retrieval in progress")
                            _loginState.value = resource
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                            _loginState.value = resource
                        }
                    }
                }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to get user data")
                val errorMessage = errorResult.userMessage
                Log.e("LoginViewModel", "EXCEPTION: User data retrieval exception: ${e.message}", e)
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