package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.domain.usecase.SocialTaskUseCase
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import com.ekehi.network.domain.verification.VerificationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SocialTasksViewModel @Inject constructor(
    private val socialTasksUseCase: SocialTaskUseCase,
    private val socialTasksRepository: SocialTaskRepository
) : ViewModel() {

    private val _socialTasks = MutableStateFlow<Resource<List<SocialTask>>>(Resource.Loading)
    val socialTasks: StateFlow<Resource<List<SocialTask>>> = _socialTasks
    
    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState

    // Caching and local state management
    private val _cachedSocialTasks = MutableStateFlow<Resource<List<SocialTask>>>(Resource.Loading)
    val cachedSocialTasks: StateFlow<Resource<List<SocialTask>>> = _cachedSocialTasks
    
    // User-specific social tasks
    private val _userSocialTasks = MutableStateFlow<List<SocialTask>>(emptyList())
    val userSocialTasks: StateFlow<List<SocialTask>> = _userSocialTasks
    
    // Track local task states to avoid full refreshes
    private val _localTaskStates = MutableStateFlow<Map<String, SocialTask>>(emptyMap())
    val localTaskStates: StateFlow<Map<String, SocialTask>> = _localTaskStates

    private var currentUserId: String? = null
    private var youtubeAccessToken: String? = null

    init {
        // Listen for refresh and account events
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is Event.RefreshSocialTasks -> {
                        loadUserSocialTasks(event.userId)
                    }
                    is Event.UserLoggedOut, is Event.AccountDeleted -> {
                        Log.d("SocialTasksViewModel", "Received ${event::class.simpleName} event - clearing data")
                        _userSocialTasks.value = emptyList()
                        _cachedSocialTasks.value = com.ekehi.network.domain.model.Resource.Error("User logged out")
                    }
                    else -> {
                        // Handle other events if needed
                    }
                }
            }
        }
    }
    
    // Caching methods
    fun cacheSocialTasks(tasks: List<SocialTask>) {
        _cachedSocialTasks.value = Resource.Success(tasks)
    }
    
    fun getCachedTasks(): Resource<List<SocialTask>> {
        return _cachedSocialTasks.value
    }
    
    // Local state management for individual tasks
    fun updateLocalTaskState(taskId: String, updatedTask: SocialTask) {
        val currentStates = _localTaskStates.value.toMutableMap()
        currentStates[taskId] = updatedTask
        _localTaskStates.value = currentStates
    }
    
    fun getLocalTaskState(taskId: String): SocialTask? {
        return _localTaskStates.value[taskId]
    }
    
    /**
     * Get all tasks with local state overrides applied
     */
    fun getCombinedTasks(): List<SocialTask> {
        val currentTasks = when (val currentState = _socialTasks.value) {
            is Resource.Success -> currentState.data
            else -> emptyList()
        }
        
        val localStates = _localTaskStates.value
        
        // Apply local states to tasks
        return currentTasks.map { task ->
            localStates[task.id] ?: task
        }
    }
    
    fun clearLocalTaskState() {
        _localTaskStates.value = emptyMap()
    }
    
    fun updateTaskStatusLocally(userId: String, taskId: String, newStatus: String, isCompleted: Boolean = false, isVerified: Boolean = false, incrementCompletionCount: Boolean = false) {
        // Get the current tasks
        val currentTasks = when (val currentState = _socialTasks.value) {
            is Resource.Success -> currentState.data
            else -> emptyList()
        }
        
        // Update the specific task
        val updatedTasks = currentTasks.map { task ->
            if (task.id == taskId) {
                val newCompletionCount = if (incrementCompletionCount) {
                    task.completionCountToday + 1
                } else {
                    task.completionCountToday
                }
                val newTotalAccumulatedRewards = if (incrementCompletionCount) {
                    task.totalAccumulatedRewards + task.rewardCoins
                } else {
                    task.totalAccumulatedRewards
                }
                task.copy(
                    status = newStatus,
                    isCompleted = isCompleted,
                    isVerified = isVerified,
                    completionCountToday = newCompletionCount,
                    totalAccumulatedRewards = newTotalAccumulatedRewards
                )
            } else {
                task
            }
        }
        
        // Update the state with the modified list
        _socialTasks.value = Resource.Success(updatedTasks)
        
        // Also update the local task state cache
        val updatedTask = updatedTasks.find { it.id == taskId }
        if (updatedTask != null) {
            updateLocalTaskState(taskId, updatedTask)
        }
    }
    
    // Method to restore from cache if needed
    fun restoreFromCache() {
        if (_cachedSocialTasks.value is Resource.Success) {
            _socialTasks.value = _cachedSocialTasks.value
        }
    }

    fun loadSocialTasks() {
        viewModelScope.launch {
            socialTasksUseCase.getSocialTasks().collect { resource ->
                _socialTasks.value = resource
                // Also cache the tasks
                if (resource is Resource.Success) {
                    _cachedSocialTasks.value = resource
                }
            }
        }
    }

    fun loadUserSocialTasks(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            socialTasksUseCase.getUserSocialTasks(userId).collect { resource ->
                _socialTasks.value = resource
                // Also cache the tasks
                if (resource is Resource.Success) {
                    _cachedSocialTasks.value = resource
                }
            }
        }
    }
    
    fun setYouTubeAccessToken(token: String) {
        youtubeAccessToken = token
    }

    fun completeSocialTask(userId: String, taskId: String, proofData: Map<String, Any>?) {
        Log.i("EKEHI_DEBUG", "ViewModel completeSocialTask: userId=$userId, taskId=$taskId")
        viewModelScope.launch {
            try {
                _verificationState.value = VerificationState.Loading
                
                // Add YouTube access token if needed
                val enhancedProofData = proofData?.toMutableMap() ?: mutableMapOf()
                
                // Add YouTube access token if this is a YouTube task
                if (enhancedProofData.containsKey("requires_youtube_oauth") && youtubeAccessToken != null) {
                    enhancedProofData["youtube_access_token"] = youtubeAccessToken!!
                }
                
                val result = socialTasksRepository.completeSocialTask(userId, taskId, enhancedProofData)
                if (result.isSuccess) {
                    val (userTask, verificationResult) = result.getOrNull()!!
                    
                    // Get the task to check if it's a blog task
                    val currentTasks = when (val currentState = _socialTasks.value) {
                        is Resource.Success -> currentState.data
                        else -> emptyList()
                    }
                    val task = currentTasks.find { it.id == taskId }
                    val isBlogTask = task?.platform?.lowercase() == "blog"
                    
                    // Update local task state based on verification result
                    when (verificationResult) {
                        is VerificationResult.Success -> {
                            // Update local task state instead of refreshing entire list
                            // For blog tasks, increment completion count and set cooldown immediately
                            if (isBlogTask && task != null) {
                                val newCount = task.completionCountToday + 1
                                val isLimitNowReached = newCount >= task.maxCompletionsPerDay
                                val cooldownUntil = Instant.now().plusSeconds(task.cooldownMinutes * 60L).toString()
                                val nextReset = Instant.now().plusSeconds(86400L).toString()
                                updateLocalTaskState(taskId, task.copy(
                                    status = if (isLimitNowReached) "verified" else "pending",
                                    isCompleted = true,
                                    isVerified = isLimitNowReached,
                                    completionCountToday = newCount,
                                    totalAccumulatedRewards = task.totalAccumulatedRewards + task.rewardCoins,
                                    nextAvailableAt = if (isLimitNowReached) {
                                        Instant.now().plusSeconds(86400L).toString()
                                    } else cooldownUntil,
                                    nextResetTime = task.nextResetTime ?: nextReset
                                ))
                            } else {
                                updateTaskStatusLocally(userId, taskId, "completed", isCompleted = true, isVerified = true)
                            }
                        }
                        is VerificationResult.Pending -> {
                            // Update local task state for pending status
                            updateTaskStatusLocally(userId, taskId, "pending_review", isCompleted = true, isVerified = false, incrementCompletionCount = isBlogTask)
                        }
                        is VerificationResult.Failure -> {
                            // Update local task state for failed status
                            updateTaskStatusLocally(userId, taskId, "failed", isCompleted = false, isVerified = false)
                        }
                    }
                    
                    // Update verification state based on result
                    _verificationState.value = when (verificationResult) {
                        is VerificationResult.Success -> {
                            // Verification succeeded - coins were awarded
                            VerificationState.Success(verificationResult.message)
                        }
                        is VerificationResult.Pending -> {
                            // Pending review - no coins awarded yet
                            VerificationState.Pending(verificationResult.message)
                        }
                        is VerificationResult.Failure -> {
                            // Verification failed - no coins awarded
                            VerificationState.Error(verificationResult.reason)
                        }
                    }
                    
                    // Send events for related updates without forcing social tasks refresh
                    if (verificationResult is VerificationResult.Success) {
                        // Only refresh leaderboard and profile since coins were awarded
                        EventBus.sendEvent(Event.RefreshUserProfile)
                        EventBus.sendEvent(Event.RefreshLeaderboard)
                    }
                } else {
                    _verificationState.value = VerificationState.Error(result.exceptionOrNull()?.message ?: "Failed to complete task")
                }
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error(e.message ?: "Failed to complete task")
                e.printStackTrace()
            }
        }
    }

    fun verifySocialTask(userId: String, taskId: String) {
        // This is now handled in the completeSocialTask method
        // Keeping this for backward compatibility
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.verifySocialTask(userId, taskId)
                if (result.isSuccess) {
                    // Send event to refresh social tasks
                    viewModelScope.launch {
                        EventBus.sendEvent(Event.RefreshSocialTasks(userId))
                        // Also refresh leaderboard and profile since rewards have been added
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
    
    fun clearVerificationState() {
        _verificationState.value = VerificationState.Idle
    }
    
    /**
     * Delete a pending task submission and revert its status to available
     */
    fun deletePendingTask(userId: String, taskId: String) {
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.deletePendingTask(userId, taskId)
                if (result.isSuccess) {
                    // Refresh the tasks list to reflect the status change
                    loadUserSocialTasks(userId)
                    
                    // Clear verification state to hide any dialogs
                    _verificationState.value = VerificationState.Idle
                } else {
                    _verificationState.value = VerificationState.Error(result.exceptionOrNull()?.message ?: "Failed to delete task")
                }
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error(e.message ?: "Failed to delete task")
                e.printStackTrace()
            }
        }
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    data class Success(val message: String) : VerificationState()
    data class Pending(val message: String) : VerificationState()
    data class Error(val message: String) : VerificationState()
}