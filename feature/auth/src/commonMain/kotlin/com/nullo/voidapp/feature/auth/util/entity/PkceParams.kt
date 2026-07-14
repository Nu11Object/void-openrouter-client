package com.nullo.voidapp.feature.auth.util.entity

internal data class PkceParams(
    val codeVerifier: String,
    val codeChallenge: String,
    val codeChallengeMethod: String = "S256",
)
