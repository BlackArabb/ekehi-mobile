package com.ekehi.network.data.model

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object NoUpdateNeeded : UpdateStatus()
    data class UpdateAvailable(
        val config: AppVersionConfig,
        val isMandatory: Boolean
    ) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}
