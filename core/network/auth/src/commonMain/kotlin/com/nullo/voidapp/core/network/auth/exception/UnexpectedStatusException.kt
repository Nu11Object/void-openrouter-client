package com.nullo.voidapp.core.network.auth.exception

import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.http.HttpStatusCode.Companion.InternalServerError

/**
 * Thrown when the server responds with an HTTP status code that is not
 * explicitly handled during API key validation (i.e. anything other than
 * [OK], [Unauthorized], or [InternalServerError]).
 */
class UnexpectedStatusException(cause: Throwable? = null) : Exception(cause)
