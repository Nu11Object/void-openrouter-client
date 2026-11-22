package com.nullo.voidapp.feature.settings.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.auth.domain.usecase.ValidateAndSaveApiKeyUseCase
import com.nullo.voidapp.core.fakes.FakeAuthRepository
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeConversationRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.fakes.FakeSettingsRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import com.nullo.voidapp.feature.settings.domain.ChangeAppThemeUseCase
import com.nullo.voidapp.feature.settings.domain.ObserveAppThemeUseCase
import com.nullo.voidapp.feature.settings.domain.SignOutUseCase
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope

internal class StoreTestEnv(
    val store: SettingsStore,
    val settingsRepository: FakeSettingsRepository,
    val authRepository: FakeAuthRepository,
    val sessionRepository: FakeSessionRepository,
    val catalogRepository: FakeCatalogRepository,
    val conversationRepository: FakeConversationRepository,
    private val testScope: StoreTestScope,
) {

    val backgroundScope: CoroutineScope
        get() = testScope.backgroundScope

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

internal suspend fun Test.ExecutionScope.withSettingsStore(
    settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    block: suspend StoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val sessionRepository = FakeSessionRepository()
        val catalogRepository = FakeCatalogRepository()
        val conversationRepository = FakeConversationRepository()
        val store = SettingsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            validateAndSaveApiKeyUseCase = ValidateAndSaveApiKeyUseCase(authRepository),
            observeAppThemeUseCase = ObserveAppThemeUseCase(settingsRepository),
            changeAppThemeUseCase = ChangeAppThemeUseCase(settingsRepository),
            signOutUseCase = SignOutUseCase(
                authRepository = authRepository,
                conversationRepository = conversationRepository,
                catalogRepository = catalogRepository,
                sessionRepository = sessionRepository,
                settingsRepository = settingsRepository
            )
        ).create()
        val env = StoreTestEnv(
            store = store,
            settingsRepository = settingsRepository,
            authRepository = authRepository,
            sessionRepository = sessionRepository,
            catalogRepository = catalogRepository,
            conversationRepository = conversationRepository,
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
