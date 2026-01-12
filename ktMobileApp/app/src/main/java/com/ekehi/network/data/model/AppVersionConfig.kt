package com.ekehi.network.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionConfig(
    val latestVersion: String,           // e.g., "1.2.0"
    val latestVersionCode: Int,          // e.g., 12
    val minimumVersion: String,          // e.g., "1.0.0"
    val minimumVersionCode: Int,         // e.g., 10
    val isMandatory: Boolean,            // Force update?
    val downloadUrl: String,             // Direct APK download URL
    val releaseNotes: String,            // What's new
    val releaseDate: String              // ISO 8601 format
)
