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
    private val referralUseCase: ReferralUseCase,
    private val authRepository: com.ekehi.network.data.repository.AuthRepository
) : ViewModel() {
    
    private val _referrals = MutableStateFlow<Resource<List<Referral>>>(Resource.Loading)
    val referrals: StateFlow<Resource<List<Referral>>> = _referrals
    
    fun loadReferrals() {
        viewModelScope.launch {
            _referrals.value = Resource.Loading
            try {
                // Get current user ID from auth repository
                val result = authRepository.getCurrentUser()
                if (result.isSuccess) {
                    val userId = result.getOrNull()?.id
                    if (!userId.isNullOrEmpty()) {
                        val referrals = referralUseCase.getReferralsByReferrerId(userId)
                        _referrals.value = Resource.Success(referrals)
                    } else {
                        _referrals.value = Resource.Error("User not authenticated")
                    }
                } else {
                    _referrals.value = Resource.Error("Failed to get user: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _referrals.value = Resource.Error(e.message ?: "Failed to load referrals")
            }
        }
    }
}