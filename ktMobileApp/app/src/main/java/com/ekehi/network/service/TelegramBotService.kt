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
class TelegramBotService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val botToken = com.ekehi.network.BuildConfig.TELEGRAM_BOT_TOKEN
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    suspend fun verifyChannelMembership(
        chatId: String,
        userId: Long
    ): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/getChatMember") {
                parameter("chat_id", chatId)
                parameter("user_id", userId)
            }
            
            val responseText = response.bodyAsText()
            val result = json.decodeFromString<TelegramApiResponse>(responseText)
            
            if (result.ok && result.result != null) {
                val isMember = result.result.status in listOf(
                    "member", "administrator", "creator"
                )
                Result.success(isMember)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class TelegramApiResponse(
    val ok: Boolean,
    val result: ChatMember? = null
)

@Serializable
data class ChatMember(
    val status: String,
    val user: TelegramUser
)

@Serializable
data class TelegramUser(
    val id: Long,
    val is_bot: Boolean,
    val first_name: String,
    val username: String? = null
)