package com.nullo.voidapp.core.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExchangeCodeResponse(
    @SerialName("key") val apiKey: String
)
