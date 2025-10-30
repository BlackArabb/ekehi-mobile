package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.domain.usecase.SocialTaskUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialTasksViewModel @Inject constructor(
    private val socialTasksUseCase: SocialTaskUseCase,
    private val socialTasksRepository: SocialTaskRepository
) : ViewModel() {

    private val _socialTasks = MutableStateFlow<Resource<List<SocialTask>>>(Resource.Loading)
    val socialTasks: StateFlow<Resource<List<SocialTask>>> = _socialTasks

    private var currentUserId: String? = null

    init {
        // Listen for refresh events
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is Event.RefreshSocialTasks -> {
                        loadUserSocialTasks(event.userId)
                    }
                    else -> {
                        // Handle other events if needed
                    }
                }
            }
        }
    }

    fun loadSocialTasks() {
        viewModelScope.launch {
            socialTasksUseCase.getSocialTasks().collect { resource ->
                _socialTasks.value = resource
            }
        }
    }

    fun loadUserSocialTasks(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            socialTasksUseCase.getUserSocialTasks(userId).collect { resource ->
                _socialTasks.value = resource
            }
        }
    }

    fun completeSocialTask(userId: String, taskId: String, taskLink: String, rewardAmount: Double) {
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.completeSocialTask(userId, taskId)
                if (result.isSuccess) {
                    // Send event to refresh social tasks
                    viewModelScope.launch {
                        EventBus.sendEvent(Event.RefreshSocialTasks(userId))
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun verifySocialTask(userId: String, taskId: String) {
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.verifySocialTask(userId, taskId)
                if (result.isSuccess) {
                    // Send event to refresh social tasks
                    viewModelScope.launch {
                        EventBus.sendEvent(Event.RefreshSocialTasks(userId))
                        // Also refresh leaderboard and profile since rewards might have been added
                        EventBus.sendEvent(Event.RefreshUserProfile)
                        EventBus.sendEvent(Event.RefreshLeaderboard)
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}