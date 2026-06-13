package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.feature.auth.data.network.service.AuthApiService

internal class FakeAuthApiService : AuthApiService {

    var validateResult: Result<Unit> = Result.success(Unit)
    var exchangeResult: Result<String> = Result.success("api_key_from_exchange")

    var lastValidatedKey: String? = null
        private set
    var lastExchangedCode: String? = null
        private set
    var lastExchangedVerifier: String? = null
        private set

    override suspend fun validateApiKey(apiKey: String) {
        lastValidatedKey = apiKey
        validateResult.getOrThrow()
    }

    override suspend fun exchangeCode(code: String, codeVerifier: String): String {
        lastExchangedCode = code
        lastExchangedVerifier = codeVerifier
        return exchangeResult.getOrThrow()
    }
}
