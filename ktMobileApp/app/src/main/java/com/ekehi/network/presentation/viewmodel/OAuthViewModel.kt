package com.ekehi.network.presentation.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.network.service.OAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OAuthViewModel @Inject constructor(
        private val oAuthService: OAuthService
) : ViewModel() {

    private val _oauthState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val oauthState: StateFlow<Resource<Boolean>> = _oauthState

    fun initiateGoogleOAuth(activity: ComponentActivity) {
        viewModelScope.launch {
            _oauthState.value = Resource.Loading
            val success = oAuthService.initiateGoogleOAuth(activity)
            _oauthState.value = if (success) {
                Resource.Success(true)
            } else {
                Resource.Error("OAuth authentication failed")
            }
        }
    }
}