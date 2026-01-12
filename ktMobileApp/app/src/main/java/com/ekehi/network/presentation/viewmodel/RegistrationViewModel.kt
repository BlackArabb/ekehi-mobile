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
import com.ekehi.network.domain.model.Country
import com.ekehi.network.domain.model.CountryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val analyticsManager: AnalyticsManager,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _registrationState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val registrationState: StateFlow<Resource<Unit>> = _registrationState
    
    private val _countries = MutableStateFlow(CountryData.allCountries)
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    fun onCountrySelected(country: Country) {
        val previousCountry = _selectedCountry.value
        _selectedCountry.value = country
        
        val currentPhone = _phoneNumber.value
        if (currentPhone.isEmpty()) {
            _phoneNumber.value = country.code + " "
        } else if (previousCountry != null && currentPhone.startsWith(previousCountry.code)) {
            // Replace old country code with new one
            _phoneNumber.value = currentPhone.replaceFirst(previousCountry.code, country.code)
        } else if (!currentPhone.startsWith("+")) {
            // Prepend if no country code exists
            _phoneNumber.value = country.code + " " + currentPhone
        } else if (currentPhone.startsWith("+")) {
            // Try to identify the old code and replace it, or just replace the first part
            val parts = currentPhone.split(" ", limit = 2)
            if (parts.isNotEmpty() && parts[0].startsWith("+")) {
                val remainder = if (parts.size > 1) " " + parts[1] else " "
                _phoneNumber.value = country.code + remainder
            }
        }
    }

    fun onPhoneNumberChanged(newNumber: String) {
        _phoneNumber.value = newNumber
    }
    
    // Fallback scope in case viewModelScope is cancelled
    private val fallbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun register(name: String, email: String, password: String, referralCode: String = "", phoneNumber: String = "", country: String = "") {
        Log.d("RegistrationViewModel", "=== REGISTRATION ATTEMPT STARTED ===")
        Log.d("RegistrationViewModel", "Email: $email, Name: $name, Phone: $phoneNumber, Country: $country")
        
        // Set loading state immediately on main thread to ensure UI updates
        _registrationState.value = Resource.Loading
        
        // Use fallback scope to ensure coroutine always runs
        // This prevents issues when viewModelScope is cancelled on some devices
        try {
            fallbackScope.launch {
                try {
                    // Validate inputs before proceeding
                    val nameValidation = InputValidator.validateAndSanitizeText(name, 50)
                    val emailValidation = InputValidator.validateAndSanitizeText(email, 255)
                    val phoneNumberValidation = InputValidator.validateAndSanitizeText(phoneNumber, 20)
                    val countryValidation = InputValidator.validateAndSanitizeText(country, 100)
                    val passwordValidation = InputValidator.validateAndSanitizeText(password, 100)
                    val referralCodeValidation = InputValidator.validateAndSanitizeText(referralCode, 50)
                    
                    // Check if all validations pass
                    if (!nameValidation.isValid) {
                        val errorMessage = "Invalid name: ${nameValidation.errorMessage}"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!emailValidation.isValid) {
                        val errorMessage = "Invalid email: ${emailValidation.errorMessage}"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!phoneNumberValidation.isValid) {
                        val errorMessage = "Invalid phone number: ${phoneNumberValidation.errorMessage}"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!countryValidation.isValid) {
                        val errorMessage = "Invalid country: ${countryValidation.errorMessage}"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!passwordValidation.isValid) {
                        val errorMessage = "Invalid password: ${passwordValidation.errorMessage}"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    // Additional specific validations
                    if (!InputValidator.isValidName(nameValidation.sanitizedInput)) {
                        val errorMessage = "Name contains invalid characters"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!InputValidator.isValidEmail(emailValidation.sanitizedInput)) {
                        val errorMessage = "Please enter a valid email address"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    if (!InputValidator.isValidPassword(passwordValidation.sanitizedInput)) {
                        val errorMessage = "Password must be at least 8 characters long and contain uppercase, lowercase, and numeric characters"
                        Log.e("RegistrationViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _registrationState.value = Resource.Error(errorMessage)
                        }
                        return@launch
                    }
                    
                    // Track registration attempt
                    try {
                        analyticsManager.trackSignUp("email")
                    } catch (e: Exception) {
                        Log.w("RegistrationViewModel", "Failed to track registration in analytics: ${e.message}")
                        // Don't fail registration if analytics fails
                    }
                    
                    Log.d("RegistrationViewModel", "✅ All validations passed, proceeding with registration")
                    
                    // Perform registration
                    authUseCase.register(
                        emailValidation.sanitizedInput, 
                        passwordValidation.sanitizedInput, 
                        nameValidation.sanitizedInput,
                        referralCodeValidation.sanitizedInput,
                        phoneNumberValidation.sanitizedInput,
                        countryValidation.sanitizedInput
                    ).collect { resource ->
                        withContext(Dispatchers.Main) {
                            _registrationState.value = resource
                        }
                        
                        when (resource) {
                            is Resource.Success -> {
                                Log.d("RegistrationViewModel", "✅ Registration successful!")
                                Log.d("RegistrationViewModel", "Profile will be created automatically by ProfileViewModel")
                                // Profile creation will happen automatically when user navigates to profile screen
                                // ProfileViewModel.init() will call loadUserProfile() which will create the profile if it doesn't exist
                            }
                            is Resource.Error -> {
                                Log.e("RegistrationViewModel", "❌ Registration failed: ${resource.message}")
                            }
                            is Resource.Loading -> {
                                Log.d("RegistrationViewModel", "⏳ Registration in progress...")
                            }
                            is Resource.Idle -> {
                                // Do nothing for Idle state
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = "Registration failed: ${e.message}"
                    Log.e("RegistrationViewModel", "❌ $errorMessage", e)
                    withContext(Dispatchers.Main) {
                        _registrationState.value = Resource.Error(errorMessage)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RegistrationViewModel", "❌ Exception launching coroutine: ${e.message}", e)
            _registrationState.value = Resource.Error("Failed to start registration process: ${e.message}")
        }
    }
    
    fun registerWithGoogle(idToken: String, name: String, email: String, phoneNumber: String = "", country: String = "") {
        Log.d("RegistrationViewModel", "=== GOOGLE REGISTRATION ATTEMPT STARTED ===")
        Log.d("RegistrationViewModel", "Email: $email, Name: $name, Phone: $phoneNumber, Country: $country")
        
        // Set loading state immediately on main thread to ensure UI updates
        _registrationState.value = Resource.Loading
        
        // Use fallback scope to ensure coroutine always runs
        try {
            fallbackScope.launch {
                try {
                    // Track Google registration attempt
                    try {
                        analyticsManager.trackSignUp("google")
                    } catch (e: Exception) {
                        Log.w("RegistrationViewModel", "Failed to track Google registration in analytics: ${e.message}")
                        // Don't fail registration if analytics fails
                    }
                    
                    Log.d("RegistrationViewModel", "✅ Proceeding with Google registration")
                    
                    // Perform Google registration
                    authUseCase.registerWithGoogle(idToken, name, email, phoneNumber, country).collect { resource ->
                        withContext(Dispatchers.Main) {
                            _registrationState.value = resource
                        }
                        
                        when (resource) {
                            is Resource.Success -> {
                                Log.d("RegistrationViewModel", "✅ Google registration successful!")
                                Log.d("RegistrationViewModel", "Profile will be created automatically by ProfileViewModel")
                                // Profile creation will happen automatically when user navigates to profile screen
                                // ProfileViewModel.init() will call loadUserProfile() which will create the profile if it doesn't exist
                            }
                            is Resource.Error -> {
                                Log.e("RegistrationViewModel", "❌ Google registration failed: ${resource.message}")
                            }
                            is Resource.Loading -> {
                                Log.d("RegistrationViewModel", "⏳ Google registration in progress...")
                            }
                            is Resource.Idle -> {
                                // Do nothing for Idle state
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = "Google registration failed: ${e.message}"
                    Log.e("RegistrationViewModel", "❌ $errorMessage", e)
                    withContext(Dispatchers.Main) {
                        _registrationState.value = Resource.Error(errorMessage)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RegistrationViewModel", "❌ Exception launching coroutine: ${e.message}", e)
            _registrationState.value = Resource.Error("Failed to start Google registration process: ${e.message}")
        }
    }
    
    fun resetState() {
        Log.d("RegistrationViewModel", "Resetting registration state")
        _registrationState.value = Resource.Idle
    }
}