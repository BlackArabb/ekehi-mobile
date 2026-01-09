package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.domain.usecase.UserUseCase
import com.ekehi.network.domain.usecase.AuthUseCase
import com.ekehi.network.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SecondaryInfoViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val authUseCase: AuthUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _secondaryInfoState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val secondaryInfoState: StateFlow<Resource<Unit>> = _secondaryInfoState

    fun submitSecondaryInfo(phoneNumber: String, country: String) {
        Log.d("SecondaryInfoViewModel", "=== SECONDARY INFO SUBMISSION STARTED ===")
        Log.d("SecondaryInfoViewModel", "Phone: $phoneNumber, Country: $country")
        
        // Set loading state immediately on main thread to ensure UI updates
        _secondaryInfoState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                // Validate inputs before proceeding
                if (phoneNumber.isEmpty()) {
                    val errorMessage = "Phone number is required"
                    Log.e("SecondaryInfoViewModel", errorMessage)
                    _secondaryInfoState.value = Resource.Error(errorMessage)
                    return@launch
                }
                
                if (country.isEmpty()) {
                    val errorMessage = "Country is required"
                    Log.e("SecondaryInfoViewModel", errorMessage)
                    _secondaryInfoState.value = Resource.Error(errorMessage)
                    return@launch
                }

                Log.d("SecondaryInfoViewModel", "✅ All validations passed, proceeding with secondary info submission")
                
                // Get current user to get their userId
                authUseCase.getCurrentUserIfLoggedIn().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val user = resource.data
                            if (user != null) {
                                val updates = mapOf(
                                    "phoneNumber" to phoneNumber,
                                    "country" to country
                                )
                                
                                userUseCase.updateUserProfile(user.id, updates).collect { updateResource ->
                                    
                                    when (updateResource) {
                                        is Resource.Success -> {
                                            Log.d("SecondaryInfoViewModel", "✅ Secondary info submitted successfully!")
                                            _secondaryInfoState.value = Resource.Success(Unit)
                                            // Track the event
                                            try {
                                                analyticsManager.trackEvent("secondary_info_submitted")
                                            } catch (e: Exception) {
                                                Log.w("SecondaryInfoViewModel", "Failed to track secondary info submission: ${e.message}")
                                            }
                                        }
                                        is Resource.Error -> {
                                            Log.e("SecondaryInfoViewModel", "❌ Secondary info submission failed: ${updateResource.message}")
                                            _secondaryInfoState.value = Resource.Error(updateResource.message)
                                        }
                                        is Resource.Loading -> {
                                            Log.d("SecondaryInfoViewModel", "⏳ Secondary info submission in progress...")
                                            _secondaryInfoState.value = Resource.Loading
                                        }
                                        is Resource.Idle -> {
                                            // Do nothing for Idle state
                                        }
                                    }
                                }
                            } else {
                                val errorMessage = "Current user is null"
                                Log.e("SecondaryInfoViewModel", errorMessage)
                                _secondaryInfoState.value = Resource.Error(errorMessage)
                            }
                        }
                        is Resource.Error -> {
                            val errorMessage = "Failed to get current user: ${resource.message}"
                            Log.e("SecondaryInfoViewModel", errorMessage)
                            _secondaryInfoState.value = Resource.Error(errorMessage)
                        }
                        is Resource.Loading -> {
                            Log.d("SecondaryInfoViewModel", "⏳ Getting current user...")
                        }
                        is Resource.Idle -> {
                            // Do nothing for Idle state
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Secondary info submission failed: ${e.message}"
                Log.e("SecondaryInfoViewModel", "❌ $errorMessage", e)
                _secondaryInfoState.value = Resource.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        Log.d("SecondaryInfoViewModel", "Resetting secondary info state")
        _secondaryInfoState.value = Resource.Idle
    }
}