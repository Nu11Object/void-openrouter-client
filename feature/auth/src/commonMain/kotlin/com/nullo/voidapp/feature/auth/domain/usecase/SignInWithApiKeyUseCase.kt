package com.nullo.voidapp.feature.auth.domain.usecase

import com.nullo.voidapp.core.network.auth.exception.InternalServerException
import com.nullo.voidapp.core.network.auth.exception.InvalidApiKeyException
import com.nullo.voidapp.core.network.auth.exception.UnexpectedStatusException
import com.nullo.voidapp.core.network.auth.exception.ValidationNetworkException
import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.kotlin.asCleanedApiKey
import com.nullo.voidapp.feature.auth.domain.repository.AuthRepository

/**
 * Use case for manually authenticating with a direct OpenRouter API key.
 *
 * Sanitizes the raw user input, validates the clean key against the remote OpenRouter servers,
 * and securely persists it into local storage upon a successful validation response.
 *
 * @throws InvalidApiKeyException If the provided API key is invalid or expired.
 * @throws InternalServerException If OpenRouter encounters an unexpected internal error
 * on its side.
 * @throws UnexpectedStatusException If the server returned an unhandled status code.
 * @throws ValidationNetworkException If a network failure occurs before a response is received.
 * @throws SecureStorageException If the platform secure storage is inaccessible
 * or fails to write the key.
 */
internal class SignInWithApiKeyUseCase(
    private val authRepository: AuthRepository,
) {

    suspend operator fun invoke(apiKey: String) {
        val cleanApiKey = apiKey.asCleanedApiKey()
        authRepository.validateApiKey(cleanApiKey)
        authRepository.saveApiKey(cleanApiKey)
    }
}
