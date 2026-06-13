package com.nullo.voidapp.feature.auth.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.feature.auth.FakeApiKeyStorage
import com.nullo.voidapp.feature.auth.FakeAuthRepository
import com.nullo.voidapp.feature.auth.FakeOAuthWebLauncher
import com.nullo.voidapp.feature.auth.FakePkceGenerator
import com.nullo.voidapp.feature.auth.domain.entity.InvalidApiKeyException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthCancelledException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException
import com.nullo.voidapp.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.nullo.voidapp.feature.auth.domain.usecase.SignInViaOpenRouterUseCase
import com.nullo.voidapp.feature.auth.domain.usecase.SignInWithApiKeyUseCase
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore.State.Completion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthStoreTest {

    private val testDispatcher = StandardTestDispatcher()

    private fun createStore(
        fakeRepo: FakeAuthRepository = FakeAuthRepository(),
        fakePkce: FakePkceGenerator = FakePkceGenerator(),
        fakeLauncher: FakeOAuthWebLauncher = FakeOAuthWebLauncher()
    ): AuthStore {
        val signInWithApiKeyUseCase = SignInWithApiKeyUseCase(fakeRepo)
        val signInViaOpenRouterUseCase =
            SignInViaOpenRouterUseCase(fakeRepo, fakePkce, fakeLauncher)
        val observeAuthStateUseCase = ObserveAuthStateUseCase(fakeRepo)

        return AuthStoreFactory(
            storeFactory = DefaultStoreFactory(),
            signInWithApiKeyUseCase = signInWithApiKeyUseCase,
            signInViaOpenRouterUseCase = signInViaOpenRouterUseCase,
            observeAuthStateUseCase = observeAuthStateUseCase
        ).create()
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest(testDispatcher) {
        val store = createStore()

        assertEquals("", store.state.apiKey)
        assertFalse(store.state.isLoading)
        assertFalse(store.state.isOAuthInProgress)
        assertNull(store.state.error)
        assertNull(store.state.completion)
    }

    @Test
    fun `ChangeApiKey updates apiKey in state`() = runTest(testDispatcher) {
        val store = createStore()

        store.accept(AuthStore.Intent.ChangeApiKey("test-key"))
        advanceUntilIdle()

        assertEquals("test-key", store.state.apiKey)
    }

    @Test
    fun `DismissError clears error after failed submission`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository().apply { throwOnValidate = InvalidApiKeyException() }
        val store = createStore(fakeRepo = fakeRepo)

        store.accept(AuthStore.Intent.ChangeApiKey("bad"))
        advanceUntilIdle()
        store.accept(AuthStore.Intent.SubmitApiKey)
        advanceUntilIdle()
        assertNotNull(store.state.error)

        store.accept(AuthStore.Intent.DismissError)
        advanceUntilIdle()

        assertNull(store.state.error)
    }

    @Test
    fun `SubmitApiKey authenticates with Animated completion on success`() =
        runTest(testDispatcher) {
            val store = createStore()

            store.accept(AuthStore.Intent.ChangeApiKey("valid-key"))
            advanceUntilIdle()
            store.accept(AuthStore.Intent.SubmitApiKey)
            advanceUntilIdle()

            assertEquals(Completion.Animated, store.state.completion)
        }

    @Test
    fun `SubmitApiKey shows error and no completion on failure`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository().apply { throwOnValidate = InvalidApiKeyException() }
        val store = createStore(fakeRepo = fakeRepo)

        store.accept(AuthStore.Intent.ChangeApiKey("bad"))
        advanceUntilIdle()
        store.accept(AuthStore.Intent.SubmitApiKey)
        advanceUntilIdle()

        assertNotNull(store.state.error)
        assertNull(store.state.completion)
    }

    @Test
    fun `SubmitApiKey passes current apiKey value to use case`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val store = createStore(fakeRepo = fakeRepo)

        store.accept(AuthStore.Intent.ChangeApiKey("my-key"))
        advanceUntilIdle()
        store.accept(AuthStore.Intent.SubmitApiKey)
        advanceUntilIdle()

        assertEquals("my-key", fakeRepo.validateApiKeyCalls.first())
    }

    @Test
    fun `SignInViaOpenRouter sets loading state`() = runTest(testDispatcher) {
        val store = createStore()

        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()

        assertTrue(store.state.isLoading)
        assertTrue(store.state.isOAuthInProgress)
    }

    @Test
    fun `SignInViaOpenRouter authenticates on success`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository().apply { fakeApiKeyResult = "some_key" }
        val fakeLauncher = FakeOAuthWebLauncher()
        val store = createStore(fakeRepo = fakeRepo, fakeLauncher = fakeLauncher)

        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()

        assertNotNull(fakeLauncher.lastAuthUrl)
        assertEquals(Completion.Animated, store.state.completion)
    }

    @Test
    fun `SignInViaOpenRouter shows error on failure`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository().apply { throwOnExchange = OAuthException("failed") }
        val store = createStore(fakeRepo = fakeRepo)

        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()

        assertNotNull(store.state.error)
        assertNull(store.state.completion)
    }

    @Test
    fun `SignInViaOpenRouter cancels a previous in-flight attempt`() = runTest(testDispatcher) {
        val fakeLauncher = FakeOAuthWebLauncher(hangIndefinitely = true)
        val store = createStore(fakeLauncher = fakeLauncher)

        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()
        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()

        assertTrue(fakeLauncher.wasCancelled)
    }

    @Test
    fun `OAuthCancelledException resets UI state without error`() = runTest(testDispatcher) {
        val fakeLauncher = FakeOAuthWebLauncher(
            launchResult = Result.failure(OAuthCancelledException())
        )
        val store = createStore(fakeLauncher = fakeLauncher)

        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()

        assertFalse(store.state.isOAuthInProgress)
        assertFalse(store.state.isLoading)
        assertNull(store.state.error)
        assertNull(store.state.completion)
    }

    @Test
    fun `CancelOAuth cancels an in-flight flow and resets state`() = runTest(testDispatcher) {
        val fakeLauncher = FakeOAuthWebLauncher(hangIndefinitely = true)
        val store = createStore(fakeLauncher = fakeLauncher)

        store.accept(AuthStore.Intent.SignInViaOpenRouter)
        advanceUntilIdle()
        assertTrue(store.state.isOAuthInProgress)

        store.accept(AuthStore.Intent.CancelOAuth)
        advanceUntilIdle()

        assertTrue(fakeLauncher.wasCancelled)
        assertFalse(store.state.isLoading)
        assertFalse(store.state.isOAuthInProgress)
    }

    @Test
    fun `bootstrapper sets Instant completion when key exists at creation`() =
        runTest(testDispatcher) {
            val storage = FakeApiKeyStorage().apply { saveApiKey("existing-key") }
            val fakeRepo = FakeAuthRepository(storage = storage)
            val store = createStore(fakeRepo = fakeRepo)

            advanceUntilIdle()

            assertEquals(Completion.Instant, store.state.completion)
        }

    @Test
    fun `bootstrapper sets Animated completion when key appears after creation`() =
        runTest(testDispatcher) {
            val storage = FakeApiKeyStorage()
            val fakeRepo = FakeAuthRepository(storage = storage)
            val store = createStore(fakeRepo = fakeRepo)

            advanceUntilIdle()
            assertNull(store.state.completion)

            storage.saveApiKey("new-key")
            advanceUntilIdle()

            assertEquals(Completion.Animated, store.state.completion)
        }
}
