package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import kotlinx.coroutines.awaitCancellation

internal class FakeOAuthWebLauncher(
    private val callbackUrl: String = "http://callback",
    private val launchResult: Result<String> = Result.success("http://callback?code=auth_code"),
    private val hangIndefinitely: Boolean = false,
) : OAuthWebLauncher {

    var launchCount: Int = 0
        private set
    var lastAuthUrl: String? = null
        private set
    var wasCancelled: Boolean = false
        private set

    override fun getCallbackUrl(): String = callbackUrl

    override suspend fun launch(authUrl: String): String {
        launchCount++
        lastAuthUrl = authUrl
        if (hangIndefinitely) {
            try {
                awaitCancellation()
            } finally {
                wasCancelled = true
            }
        }
        return launchResult.getOrThrow()
    }
}
