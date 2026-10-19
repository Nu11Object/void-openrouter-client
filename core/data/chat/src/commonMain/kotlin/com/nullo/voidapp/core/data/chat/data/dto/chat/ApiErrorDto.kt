package com.nullo.voidapp.core.data.chat.data.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ApiErrorEnvelopeDto(
    @SerialName("error")
    val error: ApiErrorDto,
)

@Serializable
data class ApiErrorDto(
    @SerialName("code")
    val code: JsonPrimitive? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("metadata")
    val metadata: JsonObject? = null,
)
