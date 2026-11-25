package com.nullo.voidapp.feature.catalog.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.data.catalog.domain.GetImageModelPricingUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObserveChatModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.ObserveImageModelsUseCase
import com.nullo.voidapp.core.data.catalog.domain.RefreshCatalogUseCase
import com.nullo.voidapp.core.data.catalog.domain.SetPinStatusUseCase
import com.nullo.voidapp.core.data.catalog.domain.UseModelUseCase
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope

internal class CatalogStoreTestEnv(
    val store: CatalogStore,
    val catalogRepository: FakeCatalogRepository,
    val sessionRepository: FakeSessionRepository,
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

internal suspend fun Test.ExecutionScope.withCatalogStore(
    catalogRepository: FakeCatalogRepository = FakeCatalogRepository(),
    sessionRepository: FakeSessionRepository = FakeSessionRepository(),
    block: suspend CatalogStoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val store = CatalogStoreFactory(
            storeFactory = DefaultStoreFactory(),
            observeChatModelsUseCase = ObserveChatModelsUseCase(catalogRepository),
            observeImageModelsUseCase = ObserveImageModelsUseCase(catalogRepository),
            getImageModelPricingUseCase = GetImageModelPricingUseCase(catalogRepository),
            refreshCatalogUseCase = RefreshCatalogUseCase(catalogRepository),
            setPinStatusUseCase = SetPinStatusUseCase(catalogRepository),
            useModelUseCase = UseModelUseCase(sessionRepository)
        ).create()
        val env = CatalogStoreTestEnv(
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
