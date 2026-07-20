package com.nullo.voidapp.core.network.auth.exception

import io.ktor.http.HttpStatusCode.Companion.Unauthorized

/**
 * Thrown when the server responds with [Unauthorized]
 * while validating an API key.
 *
 * Indicates that the provided API key is invalid, expired, or has been revoked.
 */
class InvalidApiKeyException(cause: Throwable? = null) : Exception(cause)
