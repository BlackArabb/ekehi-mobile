package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.network.service.OAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val oAuthService: OAuthService
) : ViewModel() {

    private val _oauthState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val oauthState: StateFlow<Resource<String>> = _oauthState

    fun initiateGoogleOAuth() {
        viewModelScope.launch {
            try {
                val oauthUrl = oAuthService.initiateGoogleOAuth()
                _oauthState.value = Resource.Success(oauthUrl)
            } catch (e: Exception) {
                _oauthState.value = Resource.Error("Failed to initiate Google OAuth: ${e.message}")
            }
        }
    }
}