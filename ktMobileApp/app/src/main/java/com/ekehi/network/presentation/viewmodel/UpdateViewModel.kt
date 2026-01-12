package com.ekehi.network.presentation.viewmodel

import android.util.Log
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
        if (_isDownloading.value) {
            Log.d("UpdateViewModel", "Download already in progress")
            return
        }
        
        if (downloadUrl.isEmpty()) {
            Log.e("UpdateViewModel", "Download URL is empty!")
            _downloadProgress.value = DownloadProgress.Failed("Invalid download URL")
            return
        }

        Log.d("UpdateViewModel", "Starting download from: $downloadUrl")
        viewModelScope.launch {
            _isDownloading.value = true
            try {
                apkDownloadManager.downloadAndInstallApk(downloadUrl).collect { progress ->
                    _downloadProgress.value = progress
                    when (progress) {
                        is DownloadProgress.Completed -> {
                            Log.d("UpdateViewModel", "Download completed successfully")
                            _isDownloading.value = false
                        }
                        is DownloadProgress.Failed -> {
                            Log.e("UpdateViewModel", "Download failed: ${progress.error}")
                            _isDownloading.value = false
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateViewModel", "Exception during download", e)
                _downloadProgress.value = DownloadProgress.Failed(e.message ?: "Unknown error")
                _isDownloading.value = false
            }
        }
    }

    fun dismissUpdate() {
        _updateStatus.value = UpdateStatus.NoUpdateNeeded
    }
}
