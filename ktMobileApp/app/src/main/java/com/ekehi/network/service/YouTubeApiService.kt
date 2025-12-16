package com.ekehi.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val apiKey = com.ekehi.network.BuildConfig.YOUTUBE_API_KEY
    private val baseUrl = "https://www.googleapis.com/youtube/v3"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    suspend fun verifySubscription(
        channelId: String,
        userAccessToken: String
    ): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/subscriptions") {
                parameter("part", "snippet")
                parameter("mine", "true")
                parameter("forChannelId", channelId)
                parameter("key", apiKey)
                header("Authorization", "Bearer $userAccessToken")
            }
            
            val responseText = response.bodyAsText()
            val result = json.decodeFromString<YouTubeSubscriptionResponse>(responseText)
            
            Result.success(result.items.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyVideoLike(
        videoId: String,
        userAccessToken: String
    ): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/videos/getRating") {
                parameter("id", videoId)
                parameter("key", apiKey)
                header("Authorization", "Bearer $userAccessToken")
            }
            
            val responseText = response.bodyAsText()
            val result = json.decodeFromString<YouTubeRatingResponse>(responseText)
            
            val isLiked = result.items.firstOrNull()?.rating == "like"
            Result.success(isLiked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class YouTubeSubscriptionResponse(
    val items: List<YouTubeSubscription>
)

@Serializable
data class YouTubeSubscription(
    val snippet: YouTubeSnippet
)

@Serializable
data class YouTubeSnippet(
    val title: String,
    val resourceId: YouTubeResourceId
)

@Serializable
data class YouTubeResourceId(
    val channelId: String
)

@Serializable
data class YouTubeRatingResponse(
    val items: List<YouTubeRating>
)

@Serializable
data class YouTubeRating(
    val videoId: String,
    val rating: String // "like", "dislike", "none"
)