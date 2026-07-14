package com.nullo.voidapp.feature.auth.util.entity

import com.nullo.voidapp.core.utils.resources.LocalizedException
import com.nullo.voidapp.core.utils.resources.UiText
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_internal
import voidapp.feature.auth.generated.resources.exception_invalid_api_key
import voidapp.feature.auth.generated.resources.exception_oauth_canceled

internal class OAuthCancelledException
    : LocalizedException(UiText(Res.string.exception_oauth_canceled))

internal class OAuthException(uiText: UiText, cause: Throwable? = null)
    : LocalizedException(uiText, cause)

internal class ApiKeyException(uiText: UiText, cause: Throwable? = null)
    : LocalizedException(uiText, cause)

internal class InvalidApiKeyException
    : LocalizedException(UiText(Res.string.exception_invalid_api_key))

internal class InternalServerException
    : LocalizedException(UiText(Res.string.exception_internal))
