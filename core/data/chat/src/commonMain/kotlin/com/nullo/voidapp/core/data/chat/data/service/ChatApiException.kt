package com.nullo.voidapp.core.data.chat.data.service

/**
 * Base exception for failures during OpenRouter Chat API requests.
 * Preserves the API error message when available and the underlying cause.
 */
open class ChatApiException(
    /** Text received from OpenRouter; null when there was no API response (offline, timeout). */
    val rawMessage: String?,
    cause: Throwable?,
) : Exception(rawMessage ?: cause?.message, cause)

/** Indicates that the account has insufficient credits to process the request. */
class InsufficientCreditsException(
    rawMessage: String?,
    cause: Throwable? = null
) : ChatApiException(rawMessage, cause)

/** Indicates that the request was rejected due to rate limiting. */
class RateLimitedException(
    rawMessage: String?,
    cause: Throwable? = null
) : ChatApiException(rawMessage, cause)

/** Indicates that OpenRouter encountered an internal server error. */
class InternalServerException(
    rawMessage: String?,
    cause: Throwable? = null
) : ChatApiException(rawMessage, cause)

/** Indicates that a request failed due to a network or connectivity issue. */
class NetworkException(
    rawMessage: String?,
    cause: Throwable? = null
) : ChatApiException(rawMessage, cause)

/** Indicates that a request was rejected due to wrong API key. */
class UnauthorizedException(
    rawMessage: String?,
    cause: Throwable? = null
) : ChatApiException(rawMessage, cause)
