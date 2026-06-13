package com.nullo.voidapp.core.utils.kotlin

private const val BEARER = "Bearer"

/** Prepends the [BEARER] prefix to the string.
 *
 * Example:
 * ```kotlin
 * "some-key".withBearerPrefix() // Returns "Bearer some-key"
 * ```
 */
fun String.withBearerPrefix() = "$BEARER $this"

/** Removes the [BEARER] prefix from the string.
 *
 * Note: This function only removes the prefix itself. Any subsequent spaces
 * remain intact and should be cleaned using [trim].
 *
 * Example:
 * ```kotlin
 * "Bearer some-key".removeBearerPrefix() // Returns " some-key"
 * ```
 */
fun String.removeBearerPrefix() = this.removePrefix(BEARER)

/**
 * Cleans the API key by removing surrounding whitespace and the [BEARER] prefix if present.
 *
 * Example:
 * ```kotlin
 * "  Bearer some-key  ".asCleanedApiKey() // Returns "some-key"
 * ```
 */
fun String.asCleanedApiKey() = this.trim().removeBearerPrefix().trim()
