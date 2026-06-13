package com.nullo.voidapp.feature.auth.domain

import com.nullo.voidapp.feature.auth.data.network.AuthConfig
import com.nullo.voidapp.feature.auth.domain.entity.OAuthCancelledException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException
import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class IosOAuthWebLauncher : OAuthWebLauncher {

    override fun getCallbackUrl(): String = AuthConfig.MOBILE_CALLBACK_URL

    override suspend fun launch(authUrl: String): String = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL.URLWithString(authUrl) ?: run {
                continuation.resumeWithException(
                    OAuthException("Malformed auth URL: $authUrl")
                )
                return@suspendCancellableCoroutine
            }

            val session = ASWebAuthenticationSession(
                uRL = nsUrl,
                callbackURLScheme = AuthConfig.CALLBACK_SCHEME,
                completionHandler = { callbackURL, error ->
                    when {
                        error != null -> {
                            val isCancelled = error.code == CANCELLED_LOGIN_ERROR_CODE
                            continuation.resumeWithException(
                                if (isCancelled) {
                                    OAuthCancelledException()
                                } else {
                                    OAuthException(error.localizedDescription)
                                }
                            )
                        }

                        callbackURL != null -> continuation.resume(
                            callbackURL.absoluteString ?: ""
                        )

                        else -> continuation.resumeWithException(
                            OAuthException("No callback URL and no error returned")
                        )
                    }
                }
            )

            session.presentationContextProvider = KeyWindowContextProvider()
            session.prefersEphemeralWebBrowserSession = false

            if (!session.start()) {
                continuation.resumeWithException(
                    OAuthException("Failed to start ASWebAuthenticationSession")
                )
            }

            continuation.invokeOnCancellation { session.cancel() }
        }
    }

    private companion object {
        const val CANCELLED_LOGIN_ERROR_CODE = 1L
    }
}

private class KeyWindowContextProvider :
    NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {

    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): ASPresentationAnchor = ASPresentationAnchor()
}
