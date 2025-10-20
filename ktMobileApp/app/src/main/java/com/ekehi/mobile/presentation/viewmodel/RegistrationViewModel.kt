package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.analytics.AnalyticsManager
import com.ekehi.mobile.domain.usecase.AuthUseCase
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.performance.PerformanceMonitor
import com.ekehi.mobile.security.InputValidator
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

    private val _registrationState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val registrationState: StateFlow<Resource<Unit>> = _registrationState

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            // Validate inputs before proceeding
            val nameValidation = InputValidator.validateAndSanitizeText(name, 50)
            val emailValidation = InputValidator.validateAndSanitizeText(email, 255)
            val passwordValidation = InputValidator.validateAndSanitizeText(password, 100)
            
            // Check if all validations pass
            if (!nameValidation.isValid) {
                _registrationState.value = Resource.Error("Invalid name: ${nameValidation.errorMessage}")
                return@launch
            }
            
            if (!emailValidation.isValid) {
                _registrationState.value = Resource.Error("Invalid email: ${emailValidation.errorMessage}")
                return@launch
            }
            
            if (!passwordValidation.isValid) {
                _registrationState.value = Resource.Error("Invalid password: ${passwordValidation.errorMessage}")
                return@launch
            }
            
            // Additional specific validations
            if (!InputValidator.isValidName(nameValidation.sanitizedInput)) {
                _registrationState.value = Resource.Error("Name contains invalid characters")
                return@launch
            }
            
            if (!InputValidator.isValidEmail(emailValidation.sanitizedInput)) {
                _registrationState.value = Resource.Error("Please enter a valid email address")
                return@launch
            }
            
            if (!InputValidator.isValidPassword(passwordValidation.sanitizedInput)) {
                _registrationState.value = Resource.Error("Password must be at least 8 characters long and contain uppercase, lowercase, and numeric characters")
                return@launch
            }
            
            // Track registration attempt
            analyticsManager.trackSignUp("email")
            
            performanceMonitor.measureExecutionTime({
                authUseCase.register(
                    emailValidation.sanitizedInput, 
                    passwordValidation.sanitizedInput, 
                    nameValidation.sanitizedInput
                ).collect { resource ->
                    if (resource is Resource.Success) {
                        // Registration successful
                    } else if (resource is Resource.Error) {
                        // Registration failed
                    }
                    _registrationState.value = resource
                }
            }, "register")
        }
    }
}