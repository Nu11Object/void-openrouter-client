package com.nullo.voidapp.core.data.chat.data.dto.image

import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatUsageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageGenerationResponseDto(
    @SerialName("created")
    val created: Long,
    @SerialName("data")
    val data: List<GeneratedImageDto>,
    @SerialName("usage")
    val usage: ChatUsageDto? = null,
)

@Serializable
data class GeneratedImageDto(
    @SerialName("b64_json")
    val b64Json: String,
    @SerialName("media_type")
    val mediaType: String? = null,
)
