package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow

internal class FakeAuthRepository(
    private val storage: FakeApiKeyStorage = FakeApiKeyStorage(),
    var hangOnOAuth: Boolean = false,
) : AuthRepository {

    var saveApiKeyCalls: List<String> = emptyList()
        private set
    var validateApiKeyCalls: List<String> = emptyList()
        private set
    var exchangeAuthCodeCallsCount: Int = 0
        private set

    var throwOnSave: Throwable? = null
    var throwOnValidate: Throwable? = null
    var throwOnExchange: Throwable? = null

    var fakeApiKeyResult: String = "api_key_from_exchange"
    var oAuthWasCancelled: Boolean = false
        private set

    override suspend fun getApiKey(): String? = storage.getApiKey()

    override fun hasApiKey(): Flow<Boolean> = storage.hasApiKey()

    override suspend fun saveApiKey(apiKey: String) {
        saveApiKeyCalls = saveApiKeyCalls + apiKey
        throwOnSave?.let { throw it }
        storage.saveApiKey(apiKey)
    }

    override suspend fun validateApiKey(apiKey: String) {
        validateApiKeyCalls = validateApiKeyCalls + apiKey
        throwOnValidate?.let { throw it }
    }

    override suspend fun exchangeAuthCodeForApiKey(code: String, codeVerifier: String): String {
        exchangeAuthCodeCallsCount++
        throwOnExchange?.let { throw it }

        if (hangOnOAuth) {
            try {
                awaitCancellation()
            } finally {
                oAuthWasCancelled = true
            }
        }

        return fakeApiKeyResult
    }
}
