package com.nullo.voidapp.core.utils.resources

import voidapp.core.utils.generated.resources.Res
import voidapp.core.utils.generated.resources.error_unknown

/** An exception that wraps a [UiText] */
open class LocalizedException(
    val uiText: UiText,
    cause: Throwable? = null
) : Exception(cause)

/**
 * Safely maps any [Throwable] to a user-friendly [UiText].
 *
 * Resolves as follows:
 * 1. If it's a [LocalizedException], returns its wrapped [UiText].
 * 2. If it's a standard [Exception] with a non-blank message, wraps the message in [UiText.Static].
 * 3. Otherwise, falls back to the generic unknown error resource.
 */
fun Throwable.toUiText(): UiText {
    val msg = message
    return when (this) {
        is LocalizedException -> uiText
        is Exception if !msg.isNullOrBlank() -> UiText.Static(msg)
        else -> UiText.Resource(Res.string.error_unknown)
    }
}
