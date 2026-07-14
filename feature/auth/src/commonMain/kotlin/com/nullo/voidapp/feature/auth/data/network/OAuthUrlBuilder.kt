package com.nullo.voidapp.feature.auth.data.network

import com.nullo.voidapp.feature.auth.util.entity.PkceParams
import io.ktor.http.URLBuilder

/**
 * Constructs the full OpenRouter authorization URL with the required OAuth and PKCE parameters.
 */
internal fun buildOAuthUrl(callbackUrl: String, pkceParams: PkceParams): String =
    URLBuilder(AuthConfig.AUTH_URL).apply {
        parameters.append("callback_url", callbackUrl)
        parameters.append("code_challenge", pkceParams.codeChallenge)
        parameters.append("code_challenge_method", pkceParams.codeChallengeMethod)
    }.buildString()

/**
 * Extracts the authorization code from the redirect callback URL query parameters.
 *
 * @return The captured code value, or `null` if the parameter is missing.
 */
internal fun String.extractAuthCodeParam(): String? = URLBuilder(this).parameters["code"]
