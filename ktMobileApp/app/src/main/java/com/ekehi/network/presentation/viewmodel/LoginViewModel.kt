package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.domain.usecase.AuthUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.security.ErrorHandler
import com.ekehi.network.security.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
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
    
    fun checkCurrentUser() {
        Log.d("LoginViewModel", "Checking current user status")
        
        viewModelScope.launch {
            try {
                // Set loading state
                _loginState.value = Resource.Loading
                Log.d("LoginViewModel", "Set loading state for user check")
                
                // Check if we have a valid current user
                // This will verify if the OAuth session was created successfully
                authUseCase.getCurrentUser().collect { resource ->
                    Log.d("LoginViewModel", "Received current user response: ${resource.javaClass.simpleName}")
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("LoginViewModel", "Current user verified successfully")
                            // User is authenticated, navigate to dashboard
                            _loginState.value = Resource.Success(Unit)
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Failed to verify current user: ${resource.message}")
                            // User is not authenticated, show error
                            _loginState.value = Resource.Error("Authentication failed. Please try again.")
                        }
                        is Resource.Loading -> {
                            Log.d("LoginViewModel", "User verification in progress")
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
                Log.e("LoginViewModel", "User verification exception: ${e.message}", e)
                _loginState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _loginState.value = Resource.Idle
    }
}