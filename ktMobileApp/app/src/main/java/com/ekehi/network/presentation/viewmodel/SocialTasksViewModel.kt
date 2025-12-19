package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private var currentUserId: String? = null
    private var youtubeAccessToken: String? = null

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
    
    fun setYouTubeAccessToken(token: String) {
        youtubeAccessToken = token
    }

    fun completeSocialTask(userId: String, taskId: String, proofData: Map<String, Any>?) {
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
                    
                    // Send event to refresh social tasks only if verification succeeded or is pending
                    if (verificationResult is VerificationResult.Success || verificationResult is VerificationResult.Pending) {
                        viewModelScope.launch {
                            EventBus.sendEvent(Event.RefreshSocialTasks(userId))
                            // Only refresh leaderboard and profile if coins were awarded (success only)
                            if (verificationResult is VerificationResult.Success) {
                                EventBus.sendEvent(Event.RefreshUserProfile)
                                EventBus.sendEvent(Event.RefreshLeaderboard)
                            }
                        }
                    } else {
                        // On failure, still refresh to show the rejected status
                        viewModelScope.launch {
                            EventBus.sendEvent(Event.RefreshSocialTasks(userId))
                        }
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
    
    fun resetVerificationState() {
        _verificationState.value = VerificationState.Idle
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    data class Success(val message: String) : VerificationState()
    data class Pending(val message: String) : VerificationState()
    data class Error(val message: String) : VerificationState()
}