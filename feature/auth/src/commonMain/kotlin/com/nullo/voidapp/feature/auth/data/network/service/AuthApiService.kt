package com.nullo.voidapp.feature.auth.data.network.service

import com.nullo.voidapp.feature.auth.domain.entity.InternalServerException
import com.nullo.voidapp.feature.auth.domain.entity.InvalidApiKeyException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException

internal interface AuthApiService {

    /**
     * Sends a POST request to exchange the authorization code for an API key.
     *
     * @throws OAuthException If the authorization code exchange failed.
     */
    suspend fun exchangeCode(code: String, codeVerifier: String): String

    /**
     * Sends a GET request to check if provided API key is valid.
     *
     * @throws InvalidApiKeyException If the provided api key is invalid or expired
     * @throws InternalServerException If the server returned an internal error
     * @throws Exception If an unexpected error or network failure occurs
     */
    suspend fun validateApiKey(apiKey: String)
}
