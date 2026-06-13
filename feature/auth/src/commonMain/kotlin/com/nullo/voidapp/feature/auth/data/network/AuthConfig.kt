package com.nullo.voidapp.feature.auth.data.network

internal object AuthConfig {

    const val AUTH_URL = "https://openrouter.ai/auth"

    const val CODE_EXCHANGE_ENDPOINT = "auth/keys"
    const val CHECK_API_KEY_ENDPOINT = "key"

    const val CALLBACK_SCHEME = "voidapp"

    // The app deep link is: voidapp://oauth/callback
    // Must match the AndroidManifest.xml (:feature:auth) and GitHub Pages script.

    // The page hosts a JS script to redirect the web browser back to the app scheme.
    // See: https://github.com/Nu11Object/void-oauth
    const val MOBILE_CALLBACK_URL = "https://nu11object.github.io/void-oauth/oauth-callback.html"

    const val OAUTH_TIMEOUT_MS = 5 * 60 * 1_000L
}
