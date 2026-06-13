package com.nullo.voidapp.feature.auth.domain.entity

internal class OAuthCancelledException
    : Exception("OAuth flow was cancelled.")

internal class OAuthException(message: String, cause: Throwable? = null)
    : Exception(message, cause)

internal class InvalidApiKeyException
    : Exception("The provided API key is invalid or expired. Please check it and try again.")

internal class InternalServerException
    : Exception("An unexpected server error occurred on OpenRouter's side. Please try again.")
