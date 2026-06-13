package com.nullo.voidapp.core.security

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic secure storage for the OpenRouter API key.
 * * All operations are thread-safe and non-blocking.
 * * Uses DataStore/Tink on Android, Keychain on iOS, and Keyring on Desktop.
 */
interface ApiKeyStorage {

    /**
     * Saves the [newKey], overwriting any existing value.
     *
     * @throws SecureStorageException If the storage is unavailable or write fails.
     */
    suspend fun saveApiKey(newKey: String)

    /**
     * Returns the stored API key without `Bearer` prefix, or `null` if not found.
     *
     * @throws SecureStorageException If the storage is corrupted or read fails.
     */
    suspend fun getApiKey(): String?

    /**
     * Deletes the stored API key.
     *
     * @throws SecureStorageException If the storage fails to delete the key.
     */
    suspend fun clear()

    /**
     * Returns a reactive stream indicating whether the API key is present.
     */
    fun hasApiKey(): Flow<Boolean>
}
