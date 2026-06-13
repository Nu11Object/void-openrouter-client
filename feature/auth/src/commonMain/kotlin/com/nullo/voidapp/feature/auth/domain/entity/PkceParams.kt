package com.nullo.voidapp.feature.auth.domain.entity

internal data class PkceParams(
    val codeVerifier: String,
    val codeChallenge: String,
    val codeChallengeMethod: String = "S256",
)
