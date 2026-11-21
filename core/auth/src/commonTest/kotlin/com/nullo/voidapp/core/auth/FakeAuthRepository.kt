package com.nullo.voidapp.core.auth

import com.nullo.voidapp.core.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class FakeAuthRepository(initialKey: String? = null) : AuthRepository {

    private val currentKey = MutableStateFlow(initialKey)

    val calls = mutableListOf<String>()

    var validateFailure: Throwable? = null
    var saveFailure: Throwable? = null

    override suspend fun getApiKey(): String? = currentKey.value

    override fun hasApiKey(): Flow<Boolean> = currentKey.map { it != null }

    override suspend fun saveApiKey(apiKey: String) {
        calls += "save:$apiKey"
        saveFailure?.let { throw it }
        currentKey.update { apiKey }
    }

    override suspend fun validateApiKey(apiKey: String) {
        calls += "validate:$apiKey"
        validateFailure?.let { throw it }
    }

    override suspend fun exchangeAuthCodeForApiKey(code: String?, codeVerifier: String): String = "exchanged-key"

    override suspend fun clearApiKey() {
        currentKey.update { null }
    }
}
