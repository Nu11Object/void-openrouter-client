package com.nullo.voidapp.feature.auth.util.usecase

import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.FakeAuthRepository
import com.nullo.voidapp.feature.auth.FakeOAuthWebLauncher
import com.nullo.voidapp.feature.auth.FakePkceGenerator
import com.nullo.voidapp.feature.auth.util.entity.OAuthException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class SignInViaOpenRouterUseCaseTest {

    @Test
    fun `invoke executes full flow and saves key`() = runTest {
        val fakeRepo = FakeAuthRepository().apply { fakeApiKeyResult = "api_key_from_exchange" }
        val launcher = FakeOAuthWebLauncher()
        val pkceGenerator = FakePkceGenerator()
        val useCase = SignInViaOpenRouterUseCase(fakeRepo, pkceGenerator, launcher)

        useCase()

        assertNotNull(launcher.lastAuthUrl)
        assertTrue(launcher.lastAuthUrl!!.contains("code_challenge=test_code_challenge"))

        assertEquals(1, fakeRepo.exchangeAuthCodeCallsCount)

        assertEquals("api_key_from_exchange", fakeRepo.saveApiKeyCalls.first())
        assertEquals("api_key_from_exchange", fakeRepo.getApiKey())
    }

    @Test
    fun `invoke throws when redirect url has no code param`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val launcher = FakeOAuthWebLauncher(
            launchResult = Result.success("http://callback?foo=bar")
        )
        val useCase = SignInViaOpenRouterUseCase(fakeRepo, FakePkceGenerator(), launcher)

        assertFailsWith<OAuthException> { useCase() }
        assertTrue(fakeRepo.saveApiKeyCalls.isEmpty())
    }

    @Test
    fun `invoke throws when web launcher fails`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val launcher = FakeOAuthWebLauncher(
            launchResult = Result.failure(OAuthException(UiText.Empty))
        )
        val useCase = SignInViaOpenRouterUseCase(fakeRepo, FakePkceGenerator(), launcher)

        assertFailsWith<OAuthException> { useCase() }
    }

    @Test
    fun `invoke throws when code exchange fails`() = runTest {
        val fakeRepo = FakeAuthRepository().apply {
            throwOnExchange = OAuthException(UiText.Empty)
        }
        val useCase = SignInViaOpenRouterUseCase(
            fakeRepo, FakePkceGenerator(), FakeOAuthWebLauncher()
        )

        assertFailsWith<OAuthException> { useCase() }
        assertTrue(fakeRepo.saveApiKeyCalls.isEmpty())
    }

    @Test
    fun `invoke propagates cancellation without wrapping it`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val launcher = FakeOAuthWebLauncher(hangIndefinitely = true)
        val useCase = SignInViaOpenRouterUseCase(fakeRepo, FakePkceGenerator(), launcher)

        val deferred = async { useCase() }
        advanceUntilIdle()
        deferred.cancel()

        assertFailsWith<CancellationException> { deferred.await() }
    }
}
