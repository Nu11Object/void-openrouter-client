package com.nullo.voidapp.feature.auth.util.usecase

import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.data.network.buildOAuthUrl
import com.nullo.voidapp.feature.auth.data.network.extractAuthCodeParam
import com.nullo.voidapp.feature.auth.util.entity.OAuthCancelledException
import com.nullo.voidapp.feature.auth.util.entity.OAuthException
import com.nullo.voidapp.feature.auth.util.repository.AuthRepository
import com.nullo.voidapp.feature.auth.util.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.util.util.PkceGenerator
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_auth_code

/**
 * Use case for authenticating the user via the OpenRouter OAuth web-based flow.
 *
 * Coordinates the full cryptographic PKCE login flow: generates unique verifier parameters,
 * launches the system browser, captures the resulting authorization code from the redirect URL,
 * exchanges it for a permanent API key, and stores the acquired key securely on the platform.
 *
 * @throws OAuthCancelledException If the user explicitly cancels the authentication flow.
 * @throws OAuthException If the redirect result is malformed, lacks an auth code parameter,
 * or the server exchange fails.
 * @throws SecureStorageException If the platform secure storage is unavailable
 * or fails to save the acquired key.
 */
internal class SignInViaOpenRouterUseCase(
    private val authRepository: AuthRepository,
    private val pkceGenerator: PkceGenerator,
    private val oAuthWebLauncher: OAuthWebLauncher,
) {

    suspend operator fun invoke() {
        val pkce = pkceGenerator.generate()
        val callbackUrl = oAuthWebLauncher.getCallbackUrl()
        val authUrl = buildOAuthUrl(callbackUrl, pkce)

        val callbackResult = oAuthWebLauncher.launch(authUrl)

        val code = callbackResult.extractAuthCodeParam()
            ?: throw OAuthException(UiText(Res.string.exception_auth_code))

        val apiKey = authRepository.exchangeAuthCodeForApiKey(code, pkce.codeVerifier)
        authRepository.saveApiKey(apiKey)
    }
}
