package com.nullo.voidapp.feature.auth.util.usecase

import com.nullo.voidapp.feature.auth.FakeApiKeyStorage
import com.nullo.voidapp.feature.auth.FakeAuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ObserveAuthStateUseCaseTest {

    @Test
    fun `invoke reflects repository auth state changes`() = runTest {
        val storage = FakeApiKeyStorage()
        val fakeRepo = FakeAuthRepository(storage = storage)
        val useCase = ObserveAuthStateUseCase(fakeRepo)

        assertFalse(useCase().first())

        storage.saveApiKey("valid-key")

        assertTrue(useCase().first())
    }
}
