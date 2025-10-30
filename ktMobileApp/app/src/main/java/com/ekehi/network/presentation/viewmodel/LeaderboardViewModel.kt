package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.domain.usecase.LeaderboardUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
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

    private var currentUserId: String? = null

    init {
        // Listen for refresh events
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is Event.RefreshLeaderboard -> {
                        loadLeaderboard()
                    }
                    is Event.RefreshUserProfile -> {
                        // Also refresh leaderboard when user profile changes
                        loadLeaderboard()
                        currentUserId?.let { userId ->
                            loadUserRank(userId)
                        }
                    }
                    else -> {
                        // Handle other events if needed
                    }
                }
            }
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            leaderboardUseCase.getLeaderboard().collect { resource ->
                _leaderboard.value = resource
            }
        }
    }

    fun loadUserRank(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            leaderboardUseCase.getUserRank(userId).collect { resource ->
                _userRank.value = resource
            }
        }
    }
}