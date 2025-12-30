package com.ekehi.network.data.model

data class AdContent(
    val id: String = "",
    val type: AdType = AdType.IMAGE,
    val title: String = "",
    val content: String = "", // URL for images, text for text ads
    val actionUrl: String = "",
    val isActive: Boolean = true,
    val priority: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class AdType {
    IMAGE,
    TEXT,
    ANIMATED_IMAGE
}