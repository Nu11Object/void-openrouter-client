package com.nullo.voidapp.feature.auth.exception

import com.nullo.voidapp.core.utils.resources.LocalizedException
import com.nullo.voidapp.core.utils.resources.UiText

/**
 * Thrown for any error that occurs during the OAuth authentication flow.
 *
 * Carries a localized [UiText] describing the failure to be shown directly to the user.
 */
internal class OAuthException(uiText: UiText, cause: Throwable? = null) :
    LocalizedException(uiText, cause)
