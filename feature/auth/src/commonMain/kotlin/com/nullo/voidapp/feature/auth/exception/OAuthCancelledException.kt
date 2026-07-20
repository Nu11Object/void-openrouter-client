package com.nullo.voidapp.feature.auth.exception

/** Thrown when the user explicitly cancels the OAuth authentication flow. */
internal class OAuthCancelledException(cause: Throwable? = null) : Exception(cause)
