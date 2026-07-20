package com.nullo.voidapp.core.network.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExchangeCodeRequest(
    @SerialName("code") val code: String,
    @SerialName("code_verifier") val codeVerifier: String,
    @SerialName("code_challenge_method") val codeChallengeMethod: String,
)
