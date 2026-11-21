package com.nullo.voidapp.core.testing

import com.nullo.voidapp.core.security.ApiKeyStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeApiKeyStorage(initialKey: String? = null) : ApiKeyStorage {

    private val currentKey = MutableStateFlow(initialKey)

    val savedKeys: List<String>
        field = mutableListOf<String>()

    var getFailure: Throwable? = null
    var saveFailure: Throwable? = null

    var getCalls = 0
        private set

    override suspend fun saveApiKey(newKey: String) {
        saveFailure?.let { throw it }
        currentKey.update { newKey }
        savedKeys += newKey
    }

    override suspend fun getApiKey(): String? {
        getCalls++
        getFailure?.let { throw it }
        return currentKey.value
    }

    override suspend fun clear() {
        currentKey.update { null }
    }

    override fun hasApiKey(): Flow<Boolean> = currentKey.map { it != null }
}
