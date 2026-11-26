package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.data.catalog.domain.ObservePinnedChatModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObservePinnedImageModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.core.data.catalog.domain.UseModelUseCase
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration

internal class ModelSelectorStoreTestEnv(
    val store: ModelSelectorStore,
    val catalogRepository: FakeCatalogRepository,
    val sessionRepository: FakeSessionRepository,
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

internal suspend fun Test.ExecutionScope.withModelSelectorStore(
    catalogRepository: FakeCatalogRepository = FakeCatalogRepository(),
    sessionRepository: FakeSessionRepository = FakeSessionRepository(),
    block: suspend ModelSelectorStoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val store = ModelSelectorStoreFactory(
            storeFactory = DefaultStoreFactory(),
            observePinnedChatModelsUseCase = ObservePinnedChatModelsUseCase(catalogRepository),
            observePinnedImageModelsUseCase = ObservePinnedImageModelsUseCase(catalogRepository),
            setPinStatusUseCase = SetPinStatusUseCase(catalogRepository),
            useModelUseCase = UseModelUseCase(sessionRepository)
        ).create()
        val env = ModelSelectorStoreTestEnv(
            store = store,
            catalogRepository = catalogRepository,
            sessionRepository = sessionRepository,
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
