package com.ekehi.network.data.repository

import android.util.Log
import com.ekehi.network.data.model.AdContent
import com.ekehi.network.data.model.AdType
import com.ekehi.network.service.AppwriteService
import io.appwrite.Query
import io.appwrite.models.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsRepository @Inject constructor(
    private val appwriteService: AppwriteService
) {
    suspend fun getActiveAds(): List<AdContent> {
        return try {
            Log.d("AdsRepository", "🔄 Fetching ads from Appwrite...")
            
            val response = appwriteService.databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.ADS_COLLECTION,
                queries = listOf(
                    Query.equal("isActive", true),
                    Query.orderAsc("priority")
                )
            )
            
            Log.d("AdsRepository", "✅ Fetched ${response.documents.size} ads from database")
            
            response.documents.mapIndexedNotNull { index, doc ->
                try {
                    val ad = parseAdDocument(doc)
                    Log.d("AdsRepository", "  [$index] ID: ${ad.id}, Type: ${ad.type}, Title: ${ad.title}")
                    Log.d("AdsRepository", "      Content: ${ad.content.take(100)}... (showing first 100 chars)")
                    ad
                } catch (e: Exception) {
                    Log.e("AdsRepository", "❌ Failed to parse ad ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("AdsRepository", "❌ Error fetching ads from database", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun parseAdDocument(doc: Document<Map<String, Any>>): AdContent {
        val rawContent = doc.data["content"] as? String ?: ""
        val adTypeStr = doc.data["type"] as? String
        
        Log.d("AdsRepository", "    Raw type: $adTypeStr")
        Log.d("AdsRepository", "    Raw content from DB: $rawContent")
        
        // Determine the ad type
        val adType = when (adTypeStr) {
            "TEXT" -> AdType.TEXT
            "ANIMATED_IMAGE" -> AdType.ANIMATED_IMAGE
            "IMAGE" -> AdType.IMAGE
            else -> {
                Log.w("AdsRepository", "Unknown ad type: $adTypeStr, defaulting to IMAGE")
                AdType.IMAGE
            }
        }
        
        // Process content based on type
        val content = if (adType == AdType.TEXT) {
            // For TEXT ads, use the content as-is (it's the actual text to display)
            rawContent
        } else {
            // For IMAGE/ANIMATED_IMAGE ads, ensure it's a valid URL
            if (rawContent.startsWith("http")) {
                // Remove token parameter if it exists - tokens might be expired
                val urlWithoutToken = rawContent.split("&token=")[0]
                Log.d("AdsRepository", "    Cleaned URL (removed token): $urlWithoutToken")
                urlWithoutToken
            } else {
                // It's a file ID, build the full Appwrite URL
                val fileId = rawContent
                val url = "https://fra.cloud.appwrite.io/v1/storage/buckets/${AppwriteService.ADS_BANNERS_BUCKET}/files/$fileId/view?project=68c2dd6e002112935ed2"
                Log.d("AdsRepository", "    Built URL from file ID: $url")
                url
            }
        }
        
        return AdContent(
            id = doc.id,
            type = adType,
            title = doc.data["title"] as? String ?: "",
            content = content,
            actionUrl = doc.data["actionUrl"] as? String ?: "",
            isActive = doc.data["isActive"] as? Boolean ?: false,
            priority = when (val priority = doc.data["priority"]) {
                is Long -> priority.toInt()
                is Int -> priority
                is Double -> priority.toInt()
                else -> 0
            }
        )
    }
    
    suspend fun getImageAds(): List<AdContent> {
        return getActiveAds().filter { 
            it.type == AdType.IMAGE || it.type == AdType.ANIMATED_IMAGE 
        }
    }
    
    suspend fun getTextAds(): List<AdContent> {
        return getActiveAds().filter { it.type == AdType.TEXT }
    }
}