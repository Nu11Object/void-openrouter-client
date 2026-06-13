package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.core.security.ApiKeyStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class FakeApiKeyStorage : ApiKeyStorage {

    private val keyState = MutableStateFlow<String?>(null)

    override suspend fun getApiKey(): String? = keyState.value

    override fun hasApiKey(): Flow<Boolean> {
        return keyState
            .map { it != null }
            .distinctUntilChanged()
    }

    override suspend fun saveApiKey(newKey: String) {
        keyState.value = newKey
    }

    override suspend fun clear() {
        keyState.value = null
    }
}
