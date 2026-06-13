package com.nullo.voidapp.feature.auth.domain.usecase

import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.feature.auth.data.network.buildOAuthUrl
import com.nullo.voidapp.feature.auth.data.network.extractAuthCodeParam
import com.nullo.voidapp.feature.auth.domain.entity.OAuthCancelledException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException
import com.nullo.voidapp.feature.auth.domain.repository.AuthRepository
import com.nullo.voidapp.feature.auth.domain.util.OAuthWebLauncher
import com.nullo.voidapp.feature.auth.domain.util.PkceGenerator

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
            ?: throw OAuthException("OAuth result didn't contain an authorization code parameter")

        val apiKey = authRepository.exchangeAuthCodeForApiKey(code, pkce.codeVerifier)
        authRepository.saveApiKey(apiKey)
    }
}
