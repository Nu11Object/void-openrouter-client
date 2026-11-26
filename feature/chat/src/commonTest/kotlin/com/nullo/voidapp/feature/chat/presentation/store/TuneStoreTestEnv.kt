package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import com.nullo.voidapp.feature.chat.domain.ObserveActiveModelUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatTuningParametersUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageParametersUseCase
import com.nullo.voidapp.feature.chat.domain.SetChatTuningParametersUseCase
import com.nullo.voidapp.feature.chat.domain.SetImageParametersUseCase
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration

internal class TuneStoreTestEnv(
    val store: TuneStore,
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

internal suspend fun Test.ExecutionScope.withTuneStore(
    catalogRepository: FakeCatalogRepository = FakeCatalogRepository(),
    sessionRepository: FakeSessionRepository = FakeSessionRepository(),
    block: suspend TuneStoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val store = TuneStoreFactory(
            storeFactory = DefaultStoreFactory(),
            observeActiveModelUseCase = ObserveActiveModelUseCase(sessionRepository),
            observeChatModelByIdUseCase = ObserveChatModelByIdUseCase(catalogRepository),
            observeImageModelByIdUseCase = ObserveImageModelByIdUseCase(catalogRepository),
            observeChatTuningParametersUseCase = ObserveChatTuningParametersUseCase(sessionRepository),
            observeImageParametersUseCase = ObserveImageParametersUseCase(sessionRepository),
            setChatTuningParametersUseCase = SetChatTuningParametersUseCase(sessionRepository),
            setImageParametersUseCase = SetImageParametersUseCase(sessionRepository)
        ).create()
        val env = TuneStoreTestEnv(
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
