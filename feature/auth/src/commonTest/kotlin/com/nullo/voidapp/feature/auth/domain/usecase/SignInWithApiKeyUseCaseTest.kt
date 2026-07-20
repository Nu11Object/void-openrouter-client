package com.nullo.voidapp.feature.auth.domain.usecase

import com.nullo.voidapp.feature.auth.FakeAuthRepository
import com.nullo.voidapp.core.network.auth.exception.InvalidApiKeyException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SignInWithApiKeyUseCaseTest {

    @Test
    fun `invoke validates and saves cleaned key`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val useCase = SignInWithApiKeyUseCase(fakeRepo)

        useCase("  Bearer my-key  ")

        assertEquals("my-key", fakeRepo.validateApiKeyCalls.first())
        assertEquals("my-key", fakeRepo.saveApiKeyCalls.first())
        assertEquals("my-key", fakeRepo.getApiKey())
    }

    @Test
    fun `invoke does not save key if validation fails`() = runTest {
        val fakeRepo = FakeAuthRepository().apply {
            throwOnValidate = InvalidApiKeyException()
        }
        val useCase = SignInWithApiKeyUseCase(fakeRepo)

        assertFailsWith<InvalidApiKeyException> {
            useCase("invalid-key")
        }

        assertTrue(fakeRepo.saveApiKeyCalls.isEmpty())
        assertNull(fakeRepo.getApiKey())
    }
}
