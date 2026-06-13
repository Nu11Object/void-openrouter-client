package com.nullo.voidapp.feature.auth.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExchangeCodeResponse(
    @SerialName("key") val apiKey: String
)
