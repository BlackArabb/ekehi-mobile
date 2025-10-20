package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.domain.usecase.LeaderboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardUseCase: LeaderboardUseCase
) : ViewModel() {

    fun loadLeaderboard() {
        viewModelScope.launch {
            leaderboardUseCase.getLeaderboard().collect { resource ->
                // Handle the result of getting leaderboard data
            }
        }
    }

    fun loadUserRank(userId: String) {
        viewModelScope.launch {
            leaderboardUseCase.getUserRank(userId).collect { resource ->
                // Handle the result of getting user rank
            }
        }
    }
}