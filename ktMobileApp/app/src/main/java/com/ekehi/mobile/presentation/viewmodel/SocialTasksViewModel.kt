package com.ekehi.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.mobile.analytics.AnalyticsManager
import com.ekehi.mobile.data.model.SocialTask
import com.ekehi.mobile.data.repository.SocialTaskRepository
import com.ekehi.mobile.data.repository.offline.OfflineSocialTaskRepository
import com.ekehi.mobile.data.sync.SyncManager
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.domain.usecase.SocialTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialTasksViewModel @Inject constructor(
    private val socialTaskUseCase: SocialTaskUseCase,
    private val socialTaskRepository: SocialTaskRepository,
    private val syncManager: SyncManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _socialTasks = MutableStateFlow<Resource<List<SocialTask>>>(Resource.Loading)
    val socialTasks: StateFlow<Resource<List<SocialTask>>> = _socialTasks

    private val _userSocialTasks = MutableStateFlow<Resource<List<SocialTask>>>(Resource.Loading)
    val userSocialTasks: StateFlow<Resource<List<SocialTask>>> = _userSocialTasks

    fun loadSocialTasks() {
        viewModelScope.launch {
            // Try to get offline data first for immediate UI
            if (socialTaskRepository is OfflineSocialTaskRepository) {
                // For general social tasks, we might not have user-specific caching
                // But we can still try to sync with server
            }
            
            socialTaskUseCase.getSocialTasks().collect { resource ->
                if (resource is Resource.Success) {
                    // Create a placeholder list since the use case returns Unit
                    val placeholderTasks = listOf<SocialTask>()
                    _socialTasks.value = Resource.Success(placeholderTasks)
                    // Cache the tasks if we're using offline repository
                    if (socialTaskRepository is OfflineSocialTaskRepository) {
                        // In a real implementation, we would cache actual tasks
                    }
                } else if (resource is Resource.Error) {
                    // If online fails, try to show cached data
                    if (socialTaskRepository is OfflineSocialTaskRepository) {
                        // In a real implementation, we would load cached tasks
                        _socialTasks.value = resource
                    } else {
                        _socialTasks.value = resource
                    }
                }
            }
        }
    }

    fun loadUserSocialTasks(userId: String) {
        viewModelScope.launch {
            // Try to get offline data first for immediate UI
            if (socialTaskRepository is OfflineSocialTaskRepository) {
                socialTaskRepository.getOfflineSocialTasks(userId).collect { tasks ->
                    if (tasks.isNotEmpty()) {
                        _userSocialTasks.value = Resource.Success(tasks)
                    }
                }
            }
            
            socialTaskUseCase.getUserSocialTasks(userId).collect { resource ->
                // Handle the result of getting user social tasks
                if (resource is Resource.Success) {
                    // Create a placeholder list since the use case returns Unit
                    val placeholderTasks = listOf<SocialTask>()
                    _userSocialTasks.value = Resource.Success(placeholderTasks)
                    // Cache the tasks if we're using offline repository
                    if (socialTaskRepository is OfflineSocialTaskRepository) {
                        // In a real implementation, we would cache actual tasks
                    }
                } else if (resource is Resource.Error) {
                    // If online fails, try to show cached data
                    if (socialTaskRepository is OfflineSocialTaskRepository) {
                        socialTaskRepository.getOfflineSocialTasks(userId).collect { tasks ->
                            if (tasks.isNotEmpty()) {
                                _userSocialTasks.value = Resource.Success(tasks)
                            } else {
                                _userSocialTasks.value = resource
                            }
                        }
                    } else {
                        _userSocialTasks.value = resource
                    }
                }
                
                // Subscribe to real-time updates
                subscribeToSocialTaskUpdates(userId)
            }
        }
    }

    fun completeSocialTask(userId: String, taskId: String, taskTitle: String, reward: Double) {
        viewModelScope.launch {
            socialTaskUseCase.completeSocialTask(userId, taskId).collect { resource ->
                // Handle the result of completing a social task
                if (resource is Resource.Success) {
                    // Track social task completion
                    analyticsManager.trackSocialTaskCompleted(userId, taskId, taskTitle, reward)
                    // Reload the tasks to reflect the change
                    loadUserSocialTasks(userId)
                }
            }
        }
    }

    fun syncSocialTasksData(userId: String) {
        viewModelScope.launch {
            val result = syncManager.syncAllData(userId)
            when (result) {
                is SyncManager.SyncResult.Success -> {
                    // Reload the social tasks after successful sync
                    loadUserSocialTasks(userId)
                    loadSocialTasks()
                }
                is SyncManager.SyncResult.Failure -> {
                    // Handle sync failure
                }
            }
        }
    }

    private fun subscribeToSocialTaskUpdates(userId: String) {
        // Removed realtime functionality for now
        // In a real implementation, you would subscribe to social task updates
    }

    private fun handleRealtimeEvent(event: Any) {
        // Handle real-time updates to social tasks
        // In a real implementation, you would update the UI based on the event
    }

    private fun getUserIdFromCurrentState(): String? {
        // In a real implementation, you would get the user ID from auth state
        return "user_id_placeholder"
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any ongoing subscriptions
    }
}