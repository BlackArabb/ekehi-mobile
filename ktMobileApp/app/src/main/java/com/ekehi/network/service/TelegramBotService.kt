package com.ekehi.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramBotService @Inject constructor(
    private val httpClient: HttpClient
) {
    // Calls admin proxy - bot token stays on server, never in APK
    private val proxyUrl = "https://ekehi-admin.vercel.app/api/verify/telegram"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun verifyChannelMembership(
        chatId: String,
        userId: Long
    ): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.post(proxyUrl) {
                contentType(ContentType.Application.Json)
                setBody("""{"chatId":"$chatId","userId":$userId}""")
            }

            val responseText = response.bodyAsText()
            val result = json.decodeFromString<TelegramProxyResponse>(responseText)

            if (result.success) {
                Result.success(result.isMember)
            } else {
                Result.failure(Exception(result.error ?: "Verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class TelegramProxyResponse(
    val success: Boolean,
    val isMember: Boolean = false,
    val error: String? = null
)