package com.nullo.voidapp.core.auth.domain.exception

import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

sealed class AuthException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /**
     * Thrown when the server responds with [Unauthorized]
     * while validating an API key.
     *
     * Indicates that the provided API key is invalid, expired, or has been revoked.
     */
    class InvalidApiKeyException(cause: Throwable? = null) : AuthException(cause = cause)

    /**
     * Thrown when the authorization code exchange request fails, such as due to
     * a connectivity issue, timeout, serialization error, or an error response
     * from the server.
     */
    class CodeExchangeException(cause: Throwable? = null) : AuthException(cause = cause)

    /**
     * Thrown when the API key validation request fails before a response is received,
     * such as due to a connectivity issue, timeout, or serialization error.
     */
    class ValidationNetworkException(cause: Throwable? = null) : AuthException(cause = cause)

    /**
     * Thrown when the server responds with [InternalServerError]
     * while validating an API key.
     */
    class InternalServerException : AuthException()

    /**
     * Thrown when the server responds with an HTTP status code that is not
     * explicitly handled during API key validation (i.e. anything other than
     * [OK], [Unauthorized], or [InternalServerError]).
     */
    class UnexpectedStatusException : AuthException()
}
