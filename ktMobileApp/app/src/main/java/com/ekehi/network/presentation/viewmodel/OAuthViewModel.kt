package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.service.OAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val oAuthService: OAuthService
) : ViewModel() {

    private val _oauthState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val oauthState: StateFlow<Resource<Boolean>> = _oauthState

    fun initiateGoogleOAuth(activity: ComponentActivity) {
        Log.d("OAuthViewModel", "Initiating Google OAuth")
        viewModelScope.launch {
            try {
                _oauthState.value = Resource.Loading
                // Note: The actual OAuth flow will redirect to the OAuth provider
                // and then back to our app via the callback URLs
                // The result will be handled in OAuthCallbackActivity
                oAuthService.initiateGoogleOAuth(activity)
                // We don't emit success here because the flow is not complete yet
                // The OAuthCallbackActivity will handle the final result
                Log.d("OAuthViewModel", "Google OAuth flow initiated, waiting for callback")
            } catch (e: Exception) {
                Log.e("OAuthViewModel", "Google OAuth error: ${e.message}", e)
                _oauthState.value = Resource.Error("Failed to initiate OAuth flow: ${e.message}")
            }
        }
    }
    
    fun handleOAuthResult(success: Boolean, errorMessage: String? = null) {
        Log.d("OAuthViewModel", "Handling OAuth result: success=$success")
        viewModelScope.launch {
            _oauthState.value = if (success) {
                Resource.Success(true)
            } else {
                Resource.Error(errorMessage ?: "OAuth authentication failed")
            }
        }
    }
    
    fun resetState() {
        _oauthState.value = Resource.Idle
    }
}