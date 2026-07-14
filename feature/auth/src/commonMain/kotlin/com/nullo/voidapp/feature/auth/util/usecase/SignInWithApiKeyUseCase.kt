package com.nullo.voidapp.feature.auth.util.usecase

import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.kotlin.asCleanedApiKey
import com.nullo.voidapp.feature.auth.util.entity.InternalServerException
import com.nullo.voidapp.feature.auth.util.entity.InvalidApiKeyException
import com.nullo.voidapp.feature.auth.util.repository.AuthRepository

/**
 * Use case for manually authenticating with a direct OpenRouter API key.
 *
 * Sanitizes the raw user input, validates the clean key against the remote OpenRouter servers,
 * and securely persists it into local storage upon a successful validation response.
 *
 * @throws InvalidApiKeyException If the provided API key is invalid or expired.
 * @throws InternalServerException If OpenRouter encounters an unexpected internal error
 * on its side.
 * @throws SecureStorageException If the platform secure storage is inaccessible
 * or fails to write the key.
 * @throws Exception For any errors, such as a lack of internet connection.
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
