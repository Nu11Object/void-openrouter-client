package com.nullo.voidapp.core.auth.data.service

import com.nullo.voidapp.core.auth.domain.exception.AuthException.CodeExchangeException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.InternalServerException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.InvalidApiKeyException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.UnexpectedStatusException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.ValidationNetworkException

internal interface AuthApiService {

    /**
     * Sends a POST request to exchange the authorization code for an API key.
     *
     * @throws CodeExchangeException If the authorization code exchange failed.
     */
    suspend fun exchangeCode(code: String?, codeVerifier: String): String

    /**
     * Sends a GET request to check if provided API key is valid.
     *
     * @throws InvalidApiKeyException If the provided API key is invalid or expired.
     * @throws InternalServerException If the server returned an internal error.
     * @throws UnexpectedStatusException If the server returned an unhandled status code.
     * @throws ValidationNetworkException If a network failure occurs before a response is received.
     */
    suspend fun validateApiKey(apiKey: String)
}
