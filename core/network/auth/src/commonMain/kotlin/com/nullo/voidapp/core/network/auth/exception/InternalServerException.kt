package com.nullo.voidapp.core.network.auth.exception

import io.ktor.http.HttpStatusCode.Companion.InternalServerError

/**
 * Thrown when the server responds with [InternalServerError]
 * while validating an API key.
 */
class InternalServerException(cause: Throwable? = null) : Exception(cause)
