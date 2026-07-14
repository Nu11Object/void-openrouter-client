package com.nullo.voidapp.feature.auth.util

import androidx.browser.auth.AuthTabIntent
import androidx.core.net.toUri
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.data.OAuthCallbackDispatcher
import com.nullo.voidapp.feature.auth.data.network.AuthConfig
import com.nullo.voidapp.feature.auth.presentation.AuthTabLauncherHolder
import com.nullo.voidapp.feature.auth.util.entity.OAuthException
import com.nullo.voidapp.feature.auth.util.util.OAuthWebLauncher
import kotlinx.coroutines.withTimeout
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_auth_launcher_not_ready
import kotlin.time.Duration.Companion.milliseconds

internal class AndroidOAuthWebLauncher(
    private val launcherHolder: AuthTabLauncherHolder,
    private val dispatcher: OAuthCallbackDispatcher,
) : OAuthWebLauncher {

    override fun getCallbackUrl(): String = AuthConfig.MOBILE_CALLBACK_URL

    override suspend fun launch(authUrl: String): String {
        val launcher = launcherHolder.launcher
            ?: throw OAuthException(UiText(Res.string.exception_auth_launcher_not_ready))

        dispatcher.reset()
        AuthTabIntent.Builder()
            .build()
            .launch(launcher, authUrl.toUri(), AuthConfig.CALLBACK_SCHEME)

        return withTimeout(AuthConfig.OAUTH_TIMEOUT_MS.milliseconds) {
            dispatcher.awaitCallback()
        }
    }
}
