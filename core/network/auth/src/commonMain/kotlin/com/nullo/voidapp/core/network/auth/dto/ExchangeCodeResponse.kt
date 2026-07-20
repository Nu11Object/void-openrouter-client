package com.nullo.voidapp.core.network.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExchangeCodeResponse(
    @SerialName("key") val apiKey: String
)
