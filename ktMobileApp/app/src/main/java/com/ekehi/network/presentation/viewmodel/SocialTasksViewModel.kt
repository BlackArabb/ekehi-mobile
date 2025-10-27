package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.domain.usecase.SocialTaskUseCase
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

    private val _socialTasks = MutableStateFlow<List<SocialTask>>(emptyList())
    val socialTasks: StateFlow<List<SocialTask>> = _socialTasks

    fun loadSocialTasks() {
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.getAllSocialTasks()
                if (result.isSuccess) {
                    _socialTasks.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun loadUserSocialTasks(userId: String) {
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.getUserSocialTasks(userId)
                if (result.isSuccess) {
                    // We need to convert UserSocialTask to SocialTask for display
                    // This is a simplified implementation - in a real app you'd join the data
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun completeSocialTask(userId: String, taskId: String, taskLink: String, rewardAmount: Double) {
        viewModelScope.launch {
            try {
                val result = socialTasksRepository.completeSocialTask(userId, taskId)
                if (result.isSuccess) {
                    // Handle success
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
                    // Handle success
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}