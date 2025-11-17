package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.domain.usecase.ReferralUseCase
import com.ekehi.network.data.model.Referral
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val referralUseCase: ReferralUseCase
) : ViewModel() {
    
    private val _referrals = MutableStateFlow<Resource<List<Referral>>>(Resource.Loading)
    val referrals: StateFlow<Resource<List<Referral>>> = _referrals
    
    fun loadReferrals() {
        viewModelScope.launch {
            _referrals.value = Resource.Loading
            try {
                // For now, we'll return an empty list since we don't have user context
                // In a real implementation, we would get the current user ID and fetch their referrals
                _referrals.value = Resource.Success(emptyList())
            } catch (e: Exception) {
                _referrals.value = Resource.Error(e.message ?: "Failed to load referrals")
            }
        }
    }
}