package com.nullo.voidapp.core.network.auth.exception

/**
 * Thrown when the authorization code exchange request fails, such as due to
 * a connectivity issue, timeout, serialization error, or an error response
 * from the server.
 */
class CodeExchangeException(cause: Throwable? = null) : Exception(cause)
