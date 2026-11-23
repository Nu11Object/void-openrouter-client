package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import kotlin.time.Duration
import kotlinx.coroutines.delay

private const val DEFAULT_REDIRECT_URL = "https://callback.example/callback"

internal class FakeOAuthWebLauncher : OAuthWebLauncher {

    var redirectUrl: String = DEFAULT_REDIRECT_URL

    val authUrls: List<String>
        field = mutableListOf<String>()

    var result: suspend (String) -> String = { "$redirectUrl?code=test-code" }
    var failure: Throwable? = null
    var launchDelay: Duration = Duration.ZERO

    override fun getCallbackUrl(): String = redirectUrl

    override suspend fun launch(authUrl: String): String {
        authUrls += authUrl
        delay(launchDelay)
        failure?.let { throw it }
        return result(authUrl)
    }
}
