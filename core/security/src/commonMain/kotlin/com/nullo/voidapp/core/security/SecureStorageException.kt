package com.nullo.voidapp.core.security

/** Thrown when the platform's secure storage fails operating or is unavailable. */
class SecureStorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
