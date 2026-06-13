package com.nullo.voidapp.feature.auth.domain.util

import com.nullo.voidapp.feature.auth.domain.entity.OAuthCancelledException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException

/**
 * OAuth web flow handler.
 *
 * Opens the system browser to handle the OAuth authorization flow and suspends
 * until the authentication code redirect is captured.
 */
internal interface OAuthWebLauncher {

    /**
     * Returns the platform-specific redirect (callback) URL.
     */
    fun getCallbackUrl(): String

    /**
     * Opens the [authUrl] in the system browser and suspends until the auth-code redirect occurs.
     *
     * @param authUrl The full authorization URL to open in the browser.
     * @return The full callback URL captured from the redirect (including params like `?code=...`).
     * @throws OAuthCancelledException The user explicitly closes the browser or cancels the flow.
     * @throws OAuthException Any error occurs.
     */
    suspend fun launch(authUrl: String): String
}
