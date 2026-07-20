package com.nullo.voidapp.feature.auth.data.repository

import com.nullo.voidapp.feature.auth.FakeApiKeyStorage
import com.nullo.voidapp.feature.auth.FakeAuthApiService
import com.nullo.voidapp.core.network.auth.exception.InvalidApiKeyException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AuthRepositoryImplTest {

    private fun createRepository(
        apiKeyStorage: FakeApiKeyStorage = FakeApiKeyStorage(),
        authApiService: FakeAuthApiService = FakeAuthApiService(),
    ): AuthRepositoryImpl = AuthRepositoryImpl(
        apiKeyStorage = apiKeyStorage,
        authApiService = authApiService
    )

    @Test
    fun `getApiKey returns null when no key stored`() = runTest {
        val repo = createRepository()
        assertNull(repo.getApiKey())
        assertFalse(repo.hasApiKey().first())
    }

    @Test
    fun `saveApiKey successfully persists token`() = runTest {
        val storage = FakeApiKeyStorage()
        val repo = createRepository(apiKeyStorage = storage)

        repo.saveApiKey("some-api-key")

        assertEquals("some-api-key", storage.getApiKey())
        assertTrue(repo.hasApiKey().first())
    }

    @Test
    fun `validateApiKey forwards call to api service`() = runTest {
        val apiService = FakeAuthApiService()
        val repo = createRepository(authApiService = apiService)

        repo.validateApiKey("test-key")

        assertEquals("test-key", apiService.lastValidatedKey)
    }

    @Test
    fun `validateApiKey propagates exceptions from api service`() = runTest {
        val apiService = FakeAuthApiService().apply {
            validateResult = Result.failure(InvalidApiKeyException())
        }
        val repo = createRepository(authApiService = apiService)

        assertFailsWith<InvalidApiKeyException> {
            repo.validateApiKey("bad-key")
        }
    }

    @Test
    fun `exchangeAuthCodeForApiKey returns API key on success`() = runTest {
        val apiService = FakeAuthApiService().apply {
            exchangeResult = Result.success("returned_api_key")
        }
        val repo = createRepository(authApiService = apiService)

        val result = repo.exchangeAuthCodeForApiKey("code123", "verifier123")

        assertEquals("returned_api_key", result)
        assertEquals("code123", apiService.lastExchangedCode)
        assertEquals("verifier123", apiService.lastExchangedVerifier)
    }
}
