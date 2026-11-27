package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.nullo.voidapp.core.fakes.FakeAttachmentStorage
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeChatRepository
import com.nullo.voidapp.core.fakes.FakeConversationRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.testing.StoreTestScope
import com.nullo.voidapp.core.testing.withStoreTestScope
import com.nullo.voidapp.feature.chat.domain.DeleteAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.DismissErrorMessageUseCase
import com.nullo.voidapp.feature.chat.domain.ExportAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.ImportAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveActiveModelUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveMessagesUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.RegenerateLastResponseUseCase
import com.nullo.voidapp.feature.chat.domain.SendPromptUseCase
import com.nullo.voidapp.feature.chat.domain.SetThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.SetWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.StartNewConversationUseCase
import de.infix.testBalloon.framework.core.Test
import kotlin.time.Duration

internal class ChatStoreTestEnv(
    val store: ChatStore,
    val conversationRepository: FakeConversationRepository,
    val catalogRepository: FakeCatalogRepository,
    val sessionRepository: FakeSessionRepository,
    val chatRepository: FakeChatRepository,
    val attachmentStorage: FakeAttachmentStorage,
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

internal suspend fun Test.ExecutionScope.withChatStore(
    conversationRepository: FakeConversationRepository = FakeConversationRepository(),
    catalogRepository: FakeCatalogRepository = FakeCatalogRepository(),
    sessionRepository: FakeSessionRepository = FakeSessionRepository(),
    chatRepository: FakeChatRepository = FakeChatRepository(),
    attachmentStorage: FakeAttachmentStorage = FakeAttachmentStorage(),
    activeConversation: ActiveConversationHolder = ActiveConversationHolder(),
    block: suspend ChatStoreTestEnv.() -> Unit,
) {
    withStoreTestScope {
        val store = ChatStoreFactory(
            storeFactory = DefaultStoreFactory(),
            observeActiveModelUseCase = ObserveActiveModelUseCase(sessionRepository),
            observeChatModelByIdUseCase = ObserveChatModelByIdUseCase(catalogRepository),
            observeImageModelByIdUseCase = ObserveImageModelByIdUseCase(catalogRepository),
            observeThinkingEnabledUseCase = ObserveThinkingEnabledUseCase(sessionRepository),
            observeWebSearchEnabledUseCase = ObserveWebSearchEnabledUseCase(sessionRepository),
            dismissErrorMessageUseCase = DismissErrorMessageUseCase(chatRepository),
            observeConversationByIdUseCase = ObserveConversationByIdUseCase(conversationRepository),
            regenerateLastResponseUseCase = RegenerateLastResponseUseCase(chatRepository),
            startNewConversationUseCase = StartNewConversationUseCase(conversationRepository),
            setThinkingEnabledUseCase = SetThinkingEnabledUseCase(sessionRepository),
            setWebSearchEnabledUseCase = SetWebSearchEnabledUseCase(sessionRepository),
            observeMessagesUseCase = ObserveMessagesUseCase(conversationRepository),
            sendPromptUseCase = SendPromptUseCase(chatRepository),
            importAttachmentUseCase = ImportAttachmentUseCase(attachmentStorage),
            deleteAttachmentUseCase = DeleteAttachmentUseCase(attachmentStorage),
            exportAttachmentUseCase = ExportAttachmentUseCase(attachmentStorage)
        ).create(activeConversation)
        val env = ChatStoreTestEnv(
            store = store,
            conversationRepository = conversationRepository,
            catalogRepository = catalogRepository,
            sessionRepository = sessionRepository,
            chatRepository = chatRepository,
            attachmentStorage = attachmentStorage,
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
