package com.nullo.voidapp.feature.auth.domain

import androidx.browser.auth.AuthTabIntent
import androidx.core.net.toUri
import com.nullo.voidapp.feature.auth.data.OAuthCallbackDispatcher
import com.nullo.voidapp.feature.auth.data.network.AuthConfig
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException
import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.presentation.AuthTabLauncherHolder
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

internal class AndroidOAuthWebLauncher(
    private val launcherHolder: AuthTabLauncherHolder,
    private val dispatcher: OAuthCallbackDispatcher,
) : OAuthWebLauncher {

    override fun getCallbackUrl(): String = AuthConfig.MOBILE_CALLBACK_URL

    override suspend fun launch(authUrl: String): String {
        val launcher = launcherHolder.launcher
            ?: throw OAuthException("Auth Tab launcher is not registered yet")

        dispatcher.reset()
        AuthTabIntent.Builder()
            .build()
            .launch(launcher, authUrl.toUri(), AuthConfig.CALLBACK_SCHEME)

        return withTimeout(AuthConfig.OAUTH_TIMEOUT_MS.milliseconds) {
            dispatcher.awaitCallback()
        }
    }
}
