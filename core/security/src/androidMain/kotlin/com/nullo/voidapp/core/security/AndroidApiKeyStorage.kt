package com.nullo.voidapp.core.security

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class AndroidApiKeyStorage(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) : ApiKeyStorage {

    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private val hasKeyState = MutableStateFlow<Boolean?>(null)

    override fun hasApiKey(): Flow<Boolean> = flow {
        if (hasKeyState.value == null) {
            val exists = runCatching {
                dataStore.data.map { it.contains(KEY_API_KEY) }.first()
            }.getOrDefault(false)

            hasKeyState.value = exists
        }
        emitAll(hasKeyState.filterNotNull())
    }.distinctUntilChanged()

    override suspend fun getApiKey(): String? {
        val encrypted = runCatching {
            dataStore.data.map { it[KEY_API_KEY] }.first()
        }.getOrElse { cause ->
            throw SecureStorageException("Android DataStore read failed", cause)
        }

        if (encrypted == null) return null

        return runCatching {
            val decoded = Base64.decode(encrypted, Base64.DEFAULT)
            aead.decrypt(decoded, ASSOCIATED_DATA).toString(Charsets.UTF_8)
        }.getOrElse { cause ->
            throw SecureStorageException(
                "Android Decryption failed (Key might be corrupted)", cause
            )
        }
    }

    override suspend fun saveApiKey(newKey: String) {
        runCatching {
            val encrypted = aead.encrypt(newKey.toByteArray(Charsets.UTF_8), ASSOCIATED_DATA)
            dataStore.edit { it[KEY_API_KEY] = Base64.encodeToString(encrypted, Base64.DEFAULT) }
            hasKeyState.value = true
        }.getOrElse { cause ->
            throw SecureStorageException("Android Secure Storage write failed", cause)
        }
    }

    override suspend fun clear() {
        runCatching {
            dataStore.edit { it.remove(KEY_API_KEY) }
            hasKeyState.value = false
        }.getOrElse { cause ->
            throw SecureStorageException("Android Secure Storage failed to clear", cause)
        }
    }

    companion object {

        private const val KEYSET_NAME = "keyset"
        private const val PREF_FILE_NAME = "tink_prefs"
        private const val MASTER_KEY_URI = "android-keystore://master_key"
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val ASSOCIATED_DATA = "aead_aad".toByteArray(Charsets.UTF_8)
    }
}
