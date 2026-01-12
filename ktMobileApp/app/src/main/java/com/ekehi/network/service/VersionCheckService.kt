package com.ekehi.network.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.ekehi.network.data.model.AppVersionConfig
import com.ekehi.network.data.model.UpdateStatus
import io.appwrite.Client
import io.appwrite.Query
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
                Log.d("VersionCheck", "=== Starting Update Check ===")
                val currentCode = getCurrentVersionCode()
                Log.d("VersionCheck", "Current app version code: $currentCode")
                
                // Fetch the latest version config document from Appwrite
                val response = databases.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = VERSION_CONFIG_COLLECTION_ID,
                    queries = listOf(
                        Query.orderDesc("$" + "createdAt"),
                        Query.limit(1)
                    )
                )

                if (response.documents.isEmpty()) {
                    Log.w("VersionCheck", "No version config found in collection: $VERSION_CONFIG_COLLECTION_ID")
                    return@withContext UpdateStatus.NoUpdateNeeded
                }

                val document = response.documents[0]
                Log.d("VersionCheck", "Fetched document: ${document.id}")
                Log.d("VersionCheck", "Document data: ${document.data}")
                
                // Safely extract values with defaults
                val latestVerName = document.data["latestVersion"] as? String ?: "1.0.0"
                val latestVerCode = (document.data["latestVersionCode"] as? Number)?.toInt() ?: 0
                val minVerName = (document.data["minimumVersion"] as? String) ?: latestVerName
                val minVerCode = ((document.data["minimumVersionCode"] as? Number)?.toInt()) ?: latestVerCode
                val mandatory = (document.data["isMandatory"] as? Boolean) ?: false
                val url = document.data["downloadUrl"] as? String ?: ""
                val notes = (document.data["releaseNotes"] as? String) ?: ""
                val date = (document.data["releaseDate"] as? String) ?: ""
                
                val config = AppVersionConfig(
                    latestVersion = latestVerName,
                    latestVersionCode = latestVerCode,
                    minimumVersion = minVerName,
                    minimumVersionCode = minVerCode,
                    isMandatory = mandatory,
                    downloadUrl = url,
                    releaseNotes = notes,
                    releaseDate = date
                )
                
                Log.d("VersionCheck", "Target version: $latestVerCode (Mandatory: $mandatory)")
                Log.d("VersionCheck", "Minimum required version: $minVerCode")
                
                when {
                    // Current version is below minimum - MANDATORY UPDATE
                    currentCode < minVerCode -> {
                        Log.d("VersionCheck", ">>> TRIGGERING MANDATORY UPDATE (Current $currentCode < Min $minVerCode)")
                        UpdateStatus.UpdateAvailable(config, isMandatory = true)
                    }
                    // Update available but not necessarily below minimum
                    currentCode < latestVerCode -> {
                        Log.d("VersionCheck", ">>> TRIGGERING UPDATE (Current $currentCode < Latest $latestVerCode). Mandatory: $mandatory")
                        UpdateStatus.UpdateAvailable(config, isMandatory = mandatory)
                    }
                    // Up to date
                    else -> {
                        Log.d("VersionCheck", "App is up to date ($currentCode >= $latestVerCode)")
                        UpdateStatus.NoUpdateNeeded
                    }
                }
            } catch (e: Exception) {
                Log.e("VersionCheck", "CRITICAL ERROR during update check", e)
                UpdateStatus.Error("Failed to check for updates: ${e.message}")
            }
        }
    }
}
