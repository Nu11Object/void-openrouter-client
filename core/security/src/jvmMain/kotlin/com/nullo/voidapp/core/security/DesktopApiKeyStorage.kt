package com.nullo.voidapp.core.security

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

internal class DesktopApiKeyStorage : ApiKeyStorage {

    private val keyring: Keyring by lazy { Keyring.create() }

    private val hasKeyState = MutableStateFlow<Boolean?>(null)

    override fun hasApiKey(): Flow<Boolean> = flow {
        if (hasKeyState.value == null) {
            val exists = withContext(Dispatchers.IO) {
                runCatching { keyring.getPassword(DOMAIN, ACCOUNT) != null }.getOrDefault(false)
            }
            hasKeyState.value = exists
        }
        emitAll(hasKeyState.filterNotNull())
    }.distinctUntilChanged()

    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        runCatching {
            keyring.getPassword(DOMAIN, ACCOUNT)
        }.getOrElse { cause ->
            throw SecureStorageException("Failed to read from Desktop Keyring", cause)
        }
    }

    override suspend fun saveApiKey(newKey: String) = withContext(Dispatchers.IO) {
        runCatching {
            keyring.setPassword(DOMAIN, ACCOUNT, newKey)
            hasKeyState.value = true
        }.getOrElse { cause ->
            throw SecureStorageException("Failed to write to Desktop Keyring", cause)
        }
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        runCatching {
            keyring.deletePassword(DOMAIN, ACCOUNT)
            hasKeyState.value = false
        }.getOrElse { cause ->
            if (cause is PasswordAccessException) return@withContext
            throw SecureStorageException("Failed to clear Desktop Keyring", cause)
        }
    }

    companion object {

        private const val DOMAIN = "com.nullo.voidapp"
        private const val ACCOUNT = "api_key"
    }
}
