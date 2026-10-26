package com.nullo.voidapp.core.data.chat.data.dto.chat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestDto(
    @SerialName("model")
    val model: String,
    @SerialName("messages")
    val messages: List<ChatMessageDto>,
    @SerialName("stream")
    val stream: Boolean = true,
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Double? = null,
    @SerialName("repetition_penalty")
    val repetitionPenalty: Double? = null,
    @SerialName("seed")
    val seed: Int? = null,
    @SerialName("reasoning")
    val reasoning: ReasoningConfigDto? = null,
    @SerialName("plugins")
    val plugins: List<PluginConfigDto>? = null,
)

@Serializable
data class ReasoningConfigDto(@SerialName("enabled") val enabled: Boolean = true)

@Serializable
data class PluginConfigDto(@SerialName("id") val id: String)

@Serializable
data class ChatMessageDto(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: List<ChatContentPartDto>,
)

@Serializable
sealed interface ChatContentPartDto {

    @Serializable
    @SerialName("text")
    data class Text(@SerialName("text") val text: String) : ChatContentPartDto

    @Serializable
    @SerialName("image_url")
    data class ImageUrl(@SerialName("image_url") val imageUrl: ImageUrlDto) : ChatContentPartDto
}

@Serializable
data class ImageUrlDto(@SerialName("url") val url: String)
