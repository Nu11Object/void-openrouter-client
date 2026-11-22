package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.auth.domain.repository.AuthRepository
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeAuthRepository(initialApiKey: String? = null) : AuthRepository {

    private val storedApiKey = MutableStateFlow(initialApiKey)

    val calls: List<String>
        field = mutableListOf<String>()

    var exchangedApiKey: String = "exchanged-api-key"

    var getApiKeyFailure: Throwable? = null
    var saveApiKeyFailure: Throwable? = null
    var validateApiKeyFailure: Throwable? = null
    var exchangeAuthCodeForApiKeyFailure: Throwable? = null
    var clearApiKeyFailure: Throwable? = null

    var validateDelay: Duration = Duration.ZERO

    override suspend fun getApiKey(): String? {
        calls += "getApiKey"
        getApiKeyFailure?.let { throw it }
        return storedApiKey.value
    }

    override fun hasApiKey(): Flow<Boolean> = storedApiKey.map { it != null }

    override suspend fun saveApiKey(apiKey: String) {
        calls += "saveApiKey:$apiKey"
        saveApiKeyFailure?.let { throw it }
        storedApiKey.update { apiKey }
    }

    override suspend fun validateApiKey(apiKey: String) {
        calls += "validateApiKey:$apiKey"
        delay(validateDelay)
        validateApiKeyFailure?.let { throw it }
    }

    override suspend fun exchangeAuthCodeForApiKey(code: String?, codeVerifier: String): String {
        calls += "exchangeAuthCodeForApiKey:$code:$codeVerifier"
        exchangeAuthCodeForApiKeyFailure?.let { throw it }
        return exchangedApiKey
    }

    override suspend fun clearApiKey() {
        calls += "clearApiKey"
        clearApiKeyFailure?.let { throw it }
        storedApiKey.update { null }
    }
}
