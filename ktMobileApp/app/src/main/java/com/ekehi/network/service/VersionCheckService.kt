package com.ekehi.network.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.ekehi.network.data.model.AppVersionConfig
import com.ekehi.network.data.model.UpdateStatus
import io.appwrite.Client
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionCheckService @Inject constructor(
    private val context: Context,
    private val client: Client
) {
    private val databases = Databases(client)
    
    companion object {
        const val DATABASE_ID = "68c336e7000f87296feb" // Using actual DATABASE_ID
        const val VERSION_CONFIG_COLLECTION_ID = "app_version_config"
        const val VERSION_CONFIG_DOCUMENT_ID = "current_version"
    }
    
    // Get current app version
    fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            Log.e("VersionCheck", "Error getting version code", e)
            1
        }
    }
    
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            Log.e("VersionCheck", "Error getting version name", e)
            "1.0.0"
        }
    }
    
    // Check for updates from Appwrite
    suspend fun checkForUpdate(): UpdateStatus {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("VersionCheck", "Checking for updates...")
                
                // Fetch version config from Appwrite
                val document = databases.getDocument(
                    databaseId = DATABASE_ID,
                    collectionId = VERSION_CONFIG_COLLECTION_ID,
                    documentId = VERSION_CONFIG_DOCUMENT_ID
                )
                
                val config = AppVersionConfig(
                    latestVersion = document.data["latestVersion"] as String,
                    latestVersionCode = (document.data["latestVersionCode"] as Number).toInt(),
                    minimumVersion = (document.data["minimumVersion"] as? String) ?: "1.0.0",
                    minimumVersionCode = ((document.data["minimumVersionCode"] as? Number) ?: 0).toInt(),
                    isMandatory = document.data["isMandatory"] as Boolean,
                    downloadUrl = document.data["downloadUrl"] as String,
                    releaseNotes = (document.data["releaseNotes"] as? String) ?: "",
                    releaseDate = (document.data["releaseDate"] as? String) ?: ""
                )
                
                val currentVersionCode = getCurrentVersionCode()
                
                Log.d("VersionCheck", "Current version: $currentVersionCode")
                Log.d("VersionCheck", "Latest version: ${config.latestVersionCode}")
                Log.d("VersionCheck", "Minimum version: ${config.minimumVersionCode}")
                
                when {
                    // Current version is below minimum - MANDATORY UPDATE
                    currentVersionCode < config.minimumVersionCode -> {
                        Log.d("VersionCheck", "Mandatory update required (below minimum)")
                        UpdateStatus.UpdateAvailable(config, isMandatory = true)
                    }
                    // Update available
                    currentVersionCode < config.latestVersionCode -> {
                        Log.d("VersionCheck", "Update available. Mandatory: ${config.isMandatory}")
                        UpdateStatus.UpdateAvailable(config, isMandatory = config.isMandatory)
                    }
                    // Up to date
                    else -> {
                        Log.d("VersionCheck", "App is up to date")
                        UpdateStatus.NoUpdateNeeded
                    }
                }
            } catch (e: Exception) {
                Log.e("VersionCheck", "Error checking for updates", e)
                UpdateStatus.Error("Failed to check for updates: ${e.message}")
            }
        }
    }
}
