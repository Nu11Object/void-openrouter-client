package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.fakes.FakeConversationRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import com.nullo.voidapp.feature.chat.domain.DeleteConversationUseCase
import com.nullo.voidapp.feature.chat.domain.FindConversationIdsByTextUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationsUseCase
import com.nullo.voidapp.feature.chat.domain.RenameConversationUseCase
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration

internal class SideMenuStoreTestEnv(
    val store: SideMenuStore,
    val conversationRepository: FakeConversationRepository,
    val activeConversation: ActiveConversationHolder,
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

internal suspend fun Test.ExecutionScope.withSideMenuStore(
    conversationRepository: FakeConversationRepository = FakeConversationRepository(),
    block: suspend SideMenuStoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val activeConversation = ActiveConversationHolder()
        val store = SideMenuStoreFactory(
            storeFactory = DefaultStoreFactory(),
            observeConversationsUseCase = ObserveConversationsUseCase(conversationRepository),
            findConversationIdsByTextUseCase = FindConversationIdsByTextUseCase(conversationRepository),
            renameConversationUseCase = RenameConversationUseCase(conversationRepository),
            deleteConversationUseCase = DeleteConversationUseCase(conversationRepository)
        ).create(activeConversation)
        val env = SideMenuStoreTestEnv(
            store = store,
            conversationRepository = conversationRepository,
            activeConversation = activeConversation,
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
