package com.ekehi.network.presentation.viewmodel

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

    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val loginState: StateFlow<Resource<Unit>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Validate inputs before proceeding
                val emailValidation = InputValidator.validateAndSanitizeText(email, 255)
                val passwordValidation = InputValidator.validateAndSanitizeText(password, 100)
                
                // Check if all validations pass
                if (!emailValidation.isValid) {
                    _loginState.value = Resource.Error("Invalid email: ${emailValidation.errorMessage}")
                    return@launch
                }
                
                if (!passwordValidation.isValid) {
                    _loginState.value = Resource.Error("Invalid password: ${passwordValidation.errorMessage}")
                    return@launch
                }
                
                // Additional specific validations
                if (!InputValidator.isValidEmail(emailValidation.sanitizedInput)) {
                    _loginState.value = Resource.Error("Please enter a valid email address")
                    return@launch
                }
                
                // Track login attempt
                analyticsManager.trackLogin("email")
                
                authUseCase.login(
                    emailValidation.sanitizedInput, 
                    passwordValidation.sanitizedInput
                ).collect { resource -> _loginState.value = resource }
            } catch (e: Exception) {
                val errorResult = ErrorHandler.handleException(e, "Failed to login")
                _loginState.value = Resource.Error(errorResult.userMessage)
            }
        }
    }
}