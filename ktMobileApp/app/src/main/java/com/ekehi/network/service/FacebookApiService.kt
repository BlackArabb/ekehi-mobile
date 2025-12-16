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
class FacebookApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://graph.facebook.com/v18.0"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    suspend fun verifyPageLike(
        pageId: String,
        userAccessToken: String
    ): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/me/likes/$pageId") {
                parameter("access_token", userAccessToken)
            }
            
            val responseText = response.bodyAsText()
            val result = json.decodeFromString<FacebookLikeResponse>(responseText)
            
            Result.success(result.data.isNotEmpty())
        } catch (e: Exception) {
            // If endpoint returns error, user hasn't liked the page
            Result.success(false)
        }
    }
}

@Serializable
data class FacebookLikeResponse(
    val data: List<FacebookPageData>
)

@Serializable
data class FacebookPageData(
    val id: String,
    val name: String
)