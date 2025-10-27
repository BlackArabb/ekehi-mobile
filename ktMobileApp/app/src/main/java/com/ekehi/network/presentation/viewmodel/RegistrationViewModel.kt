package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.domain.usecase.AuthUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.security.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val analyticsManager: AnalyticsManager,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _registrationState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val registrationState: StateFlow<Resource<Unit>> = _registrationState

    fun register(name: String, email: String, password: String) {
        Log.d("RegistrationViewModel", "Registration attempt started with email: $email")
        
        viewModelScope.launch {
            try {
                // Validate inputs before proceeding
                val nameValidation = InputValidator.validateAndSanitizeText(name, 50)
                val emailValidation = InputValidator.validateAndSanitizeText(email, 255)
                val passwordValidation = InputValidator.validateAndSanitizeText(password, 100)
                
                Log.d("RegistrationViewModel", "Input validation completed")
                
                // Check if all validations pass
                if (!nameValidation.isValid) {
                    val errorMessage = "Invalid name: ${nameValidation.errorMessage}"
                    Log.e("RegistrationViewModel", errorMessage)
                    _registrationState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                if (!emailValidation.isValid) {
                    val errorMessage = "Invalid email: ${emailValidation.errorMessage}"
                    Log.e("RegistrationViewModel", errorMessage)
                    _registrationState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                if (!passwordValidation.isValid) {
                    val errorMessage = "Invalid password: ${passwordValidation.errorMessage}"
                    Log.e("RegistrationViewModel", errorMessage)
                    _registrationState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                // Additional specific validations
                if (!InputValidator.isValidName(nameValidation.sanitizedInput)) {
                    val errorMessage = "Name contains invalid characters"
                    Log.e("RegistrationViewModel", errorMessage)
                    _registrationState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                if (!InputValidator.isValidEmail(emailValidation.sanitizedInput)) {
                    val errorMessage = "Please enter a valid email address"
                    Log.e("RegistrationViewModel", errorMessage)
                    _registrationState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                if (!InputValidator.isValidPassword(passwordValidation.sanitizedInput)) {
                    val errorMessage = "Password must be at least 8 characters long and contain uppercase, lowercase, and numeric characters"
                    Log.e("RegistrationViewModel", errorMessage)
                    _registrationState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                // Track registration attempt
                analyticsManager.trackSignUp("email")
                Log.d("RegistrationViewModel", "Registration attempt tracked in analytics")
                
                // Set loading state
                _registrationState.value = Resource.Loading
                Log.d("RegistrationViewModel", "Set loading state")
                
                // Perform registration
                Log.d("RegistrationViewModel", "Starting registration request")
                authUseCase.register(
                    emailValidation.sanitizedInput, 
                    passwordValidation.sanitizedInput, 
                    nameValidation.sanitizedInput
                ).collect { resource ->
                    Log.d("RegistrationViewModel", "Received registration response: ${resource.javaClass.simpleName}")
                    _registrationState.value = resource
                    
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("RegistrationViewModel", "Registration successful")
                        }
                        is Resource.Error -> {
                            Log.e("RegistrationViewModel", "Registration failed: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("RegistrationViewModel", "Registration in progress")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Registration failed: ${e.message}"
                Log.e("RegistrationViewModel", errorMessage, e)
                _registrationState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    fun registerWithGoogle(idToken: String, name: String, email: String) {
        Log.d("RegistrationViewModel", "Google registration attempt started with email: $email")
        
        viewModelScope.launch {
            try {
                // Track Google registration attempt
                analyticsManager.trackSignUp("google")
                Log.d("RegistrationViewModel", "Google registration attempt tracked in analytics")
                
                // Set loading state
                _registrationState.value = Resource.Loading
                Log.d("RegistrationViewModel", "Set loading state for Google registration")
                
                // Perform Google registration
                Log.d("RegistrationViewModel", "Starting Google registration request")
                authUseCase.registerWithGoogle(idToken, name, email).collect { resource ->
                    Log.d("RegistrationViewModel", "Received Google registration response: ${resource.javaClass.simpleName}")
                    _registrationState.value = resource
                    
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("RegistrationViewModel", "Google registration successful")
                        }
                        is Resource.Error -> {
                            Log.e("RegistrationViewModel", "Google registration failed: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("RegistrationViewModel", "Google registration in progress")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Google registration failed: ${e.message}"
                Log.e("RegistrationViewModel", errorMessage, e)
                _registrationState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _registrationState.value = Resource.Idle
    }
}