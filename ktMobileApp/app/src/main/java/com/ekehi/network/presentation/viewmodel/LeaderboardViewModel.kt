package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.usecase.LeaderboardUseCase
import com.ekehi.network.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardUseCase: LeaderboardUseCase
) : ViewModel() {
    
    private val _leaderboard = MutableStateFlow<Resource<List<Map<String, Any>>>>(Resource.Loading)
    val leaderboard: StateFlow<Resource<List<Map<String, Any>>>> = _leaderboard
    
    private val _userRank = MutableStateFlow<Resource<Int>>(Resource.Loading)
    val userRank: StateFlow<Resource<Int>> = _userRank

    fun loadLeaderboard() {
        viewModelScope.launch {
            leaderboardUseCase.getLeaderboard().collect { resource ->
                _leaderboard.value = resource
            }
        }
    }

    fun loadUserRank(userId: String) {
        viewModelScope.launch {
            leaderboardUseCase.getUserRank(userId).collect { resource ->
                _userRank.value = resource
            }
        }
    }
}