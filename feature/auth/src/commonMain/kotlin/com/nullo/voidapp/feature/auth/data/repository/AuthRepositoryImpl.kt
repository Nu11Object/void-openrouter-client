package com.nullo.voidapp.feature.auth.data.repository

import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.feature.auth.data.network.service.AuthApiService
import com.nullo.voidapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class AuthRepositoryImpl(
    private val apiKeyStorage: ApiKeyStorage,
    private val authApiService: AuthApiService,
) : AuthRepository {

    override suspend fun getApiKey(): String? {
        return apiKeyStorage.getApiKey()
    }

    override fun hasApiKey(): Flow<Boolean> = apiKeyStorage.hasApiKey()

    override suspend fun saveApiKey(apiKey: String) {
        apiKeyStorage.saveApiKey(apiKey)
    }

    override suspend fun validateApiKey(apiKey: String) {
        authApiService.validateApiKey(apiKey)
    }

    override suspend fun exchangeAuthCodeForApiKey(code: String, codeVerifier: String): String {
        return authApiService.exchangeCode(code, codeVerifier)
    }
}
