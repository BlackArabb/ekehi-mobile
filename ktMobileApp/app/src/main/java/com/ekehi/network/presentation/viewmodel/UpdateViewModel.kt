package com.ekehi.network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.UpdateStatus
import com.ekehi.network.service.ApkDownloadManager
import com.ekehi.network.service.DownloadProgress
import com.ekehi.network.service.VersionCheckService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val versionCheckService: VersionCheckService,
    private val apkDownloadManager: ApkDownloadManager
) : ViewModel() {

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    init {
        checkForUpdates()
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateStatus.value = versionCheckService.checkForUpdate()
        }
    }

    fun startDownload(downloadUrl: String) {
        if (_isDownloading.value) return
        
        viewModelScope.launch {
            _isDownloading.value = true
            apkDownloadManager.downloadAndInstallApk(downloadUrl).collect { progress ->
                _downloadProgress.value = progress
                if (progress is DownloadProgress.Completed || progress is DownloadProgress.Failed) {
                    _isDownloading.value = false
                }
            }
        }
    }

    fun dismissUpdate() {
        _updateStatus.value = UpdateStatus.NoUpdateNeeded
    }
}
