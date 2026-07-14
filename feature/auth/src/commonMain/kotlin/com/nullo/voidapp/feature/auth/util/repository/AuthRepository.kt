package com.nullo.voidapp.feature.auth.util.repository

import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.feature.auth.util.entity.InternalServerException
import com.nullo.voidapp.feature.auth.util.entity.InvalidApiKeyException
import com.nullo.voidapp.feature.auth.util.entity.OAuthException
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing user authentication.
 *
 * Coordinates OpenRouter API key retrieval, validation, direct key persistence,
 * and the code exchange steps of the OAuth authentication flow.
 */
internal interface AuthRepository {

    /**
     * Returns the currently stored API key, or `null` if the user is not authenticated.
     *
     * @throws SecureStorageException If the underlying platform secure storage is corrupted
     * or inaccessible.
     */
    suspend fun getApiKey(): String?

    /**
     * Returns `true` if an API key is present in the storage, indicating an active session.
     *
     * @throws SecureStorageException If the underlying platform secure storage
     * fails to verify key presence.
     */
    fun hasApiKey(): Flow<Boolean>

    /**
     * Persists the provided [apiKey] into the platform's secure storage.
     *
     * @param apiKey The API key to be saved.
     * @throws SecureStorageException If the storage is unavailable or the write operation fails.
     */
    suspend fun saveApiKey(apiKey: String)

    /**
     * Validates the given [apiKey] against the remote OpenRouter service.
     *
     * @param apiKey The API key to verify.
     * @throws InvalidApiKeyException If the provided API key is invalid or expired.
     * @throws InternalServerException If an unexpected server error occurs on OpenRouter's side.
     * @throws Exception If any unexpected error occurs, such as network connectivity issues.
     */
    suspend fun validateApiKey(apiKey: String)

    /**
     * Exchanges a captured OAuth authorization code for a permanent OpenRouter API key.
     *
     * @param code The authorization code received from the OAuth redirect.
     * @param codeVerifier The original cryptographic PKCE verifier used to initiate the flow.
     * @return The permanent OpenRouter API key retrieved from the server.
     * @throws OAuthException If any error occurs during the code-to-key exchange.
     */
    suspend fun exchangeAuthCodeForApiKey(code: String, codeVerifier: String): String
}
