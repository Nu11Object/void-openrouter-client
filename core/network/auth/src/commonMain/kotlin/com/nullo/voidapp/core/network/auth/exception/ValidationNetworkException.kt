package com.nullo.voidapp.core.network.auth.exception

/**
 * Thrown when the API key validation request fails before a response is received,
 * such as due to a connectivity issue, timeout, or serialization error.
 */
class ValidationNetworkException(cause: Throwable? = null) : Exception(cause)
