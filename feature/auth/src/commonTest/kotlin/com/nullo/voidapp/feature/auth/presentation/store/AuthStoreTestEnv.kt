package com.nullo.voidapp.feature.auth.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.auth.domain.usecase.ValidateAndSaveApiKeyUseCase
import com.nullo.voidapp.core.fakes.FakeAuthRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import com.nullo.voidapp.feature.auth.FakeOAuthWebLauncher
import com.nullo.voidapp.feature.auth.FakePkceGenerator
import com.nullo.voidapp.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.nullo.voidapp.feature.auth.domain.usecase.SignInViaOpenRouterUseCase
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration

internal class AuthStoreTestEnv(
    val store: AuthStore,
    val authRepository: FakeAuthRepository,
    val launcher: FakeOAuthWebLauncher,
    private val testScope: StoreTestScope,
) {

    fun idle() {
        testScope.idle()
    }

    fun advanceBy(duration: Duration) {
        testScope.advanceBy(duration)
    }

    fun dispose() {
        store.dispose()
    }
}

internal suspend fun Test.ExecutionScope.withAuthStore(
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    launcher: FakeOAuthWebLauncher = FakeOAuthWebLauncher(),
    pkce: FakePkceGenerator = FakePkceGenerator(),
    block: suspend AuthStoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val store = AuthStoreFactory(
            storeFactory = DefaultStoreFactory(),
            signInViaOpenRouterUseCase = SignInViaOpenRouterUseCase(
                authRepository = authRepository,
                pkceGenerator = pkce,
                oAuthWebLauncher = launcher
            ),
            validateAndSaveApiKeyUseCase = ValidateAndSaveApiKeyUseCase(authRepository),
            observeAuthStateUseCase = ObserveAuthStateUseCase(authRepository)
        ).create()
        val env = AuthStoreTestEnv(
            store = store,
            authRepository = authRepository,
            launcher = launcher,
            testScope = this
        )
        try {
            env.idle()
            env.block()
        } finally {
            env.dispose()
        }
    }
}
