package com.nullo.voidapp.feature.auth.domain.usecase

import com.nullo.voidapp.core.auth.domain.exception.AuthException.CodeExchangeException
import com.nullo.voidapp.core.auth.domain.repository.AuthRepository
import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.feature.auth.data.network.buildOAuthUrl
import com.nullo.voidapp.feature.auth.data.network.extractAuthCodeParam
import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.domain.util.PkceGenerator

/**
 * Use case for authenticating the user via the OpenRouter OAuth web-based flow.
 *
 * Coordinates the full cryptographic PKCE login flow: generates unique verifier parameters,
 * launches the system browser, captures the resulting authorization code from the redirect URL,
 * exchanges it for a permanent API key, and stores the acquired key securely on the platform.
 *
 * @throws CodeExchangeException If the authorization code exchange failed.
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
        val apiKey = authRepository.exchangeAuthCodeForApiKey(code, pkce.codeVerifier)

        authRepository.saveApiKey(apiKey)
    }
}
