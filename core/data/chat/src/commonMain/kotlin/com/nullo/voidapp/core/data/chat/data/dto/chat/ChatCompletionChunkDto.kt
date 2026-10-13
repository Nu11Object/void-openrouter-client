package com.nullo.voidapp.core.data.chat.data.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionChunkDto(
    @SerialName("id")
    val id: String,
    @SerialName("choices")
    val choices: List<ChatStreamChoiceDto> = emptyList(),
    @SerialName("usage")
    val usage: ChatUsageDto? = null,
    @SerialName("error")
    val error: ChatStreamErrorDto? = null,
)

@Serializable
data class ChatStreamChoiceDto(
    @SerialName("index")
    val index: Int,
    @SerialName("delta")
    val delta: ChatDeltaDto,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class ChatDeltaDto(
    @SerialName("role")
    val role: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("reasoning")
    val reasoning: String? = null,
)

@Serializable
data class ChatUsageDto(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
    @SerialName("cost")
    val cost: Double? = null,
)

@Serializable
data class ChatStreamErrorDto(
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String,
)
