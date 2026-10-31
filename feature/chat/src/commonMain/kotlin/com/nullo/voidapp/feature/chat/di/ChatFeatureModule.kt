package com.nullo.voidapp.feature.chat.di

import com.nullo.voidapp.feature.chat.domain.DeleteAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.DeleteConversationUseCase
import com.nullo.voidapp.feature.chat.domain.DismissErrorMessageUseCase
import com.nullo.voidapp.feature.chat.domain.FindConversationIdsByTextUseCase
import com.nullo.voidapp.feature.chat.domain.ImportAttachmentUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveActiveModelUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationsUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveMessagesUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.RegenerateLastResponseUseCase
import com.nullo.voidapp.feature.chat.domain.RenameConversationUseCase
import com.nullo.voidapp.feature.chat.domain.SendPromptUseCase
import com.nullo.voidapp.feature.chat.domain.SetThinkingEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.SetWebSearchEnabledUseCase
import com.nullo.voidapp.feature.chat.domain.StartNewConversationUseCase
import com.nullo.voidapp.feature.chat.presentation.component.ChatComponent
import com.nullo.voidapp.feature.chat.presentation.component.DefaultChatComponent
import com.nullo.voidapp.feature.chat.presentation.store.ChatStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val chatFeatureModule = module {
    // TODO: break ChatStore into smaller, more focused stores
    factory {
        ChatStoreFactory(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory {
        ChatComponent.Factory { onNavigateToModelCatalog, onNavigateToAppSettings, componentContext ->
            DefaultChatComponent(
                onNavigateToModelCatalog = onNavigateToModelCatalog,
                onNavigateToAppSettings = onNavigateToAppSettings,
                chatStoreFactory = get(),
                componentContext = componentContext
            )
        }
    }

    factoryOf(::DismissErrorMessageUseCase)
    factoryOf(::ObserveActiveModelUseCase)
    factoryOf(::ObserveChatModelByIdUseCase)
    factoryOf(::ObserveConversationByIdUseCase)
    factoryOf(::ObserveConversationsUseCase)
    factoryOf(::DeleteConversationUseCase)
    factoryOf(::FindConversationIdsByTextUseCase)
    factoryOf(::ObserveImageModelByIdUseCase)
    factoryOf(::ObserveMessagesUseCase)
    factoryOf(::ObserveThinkingEnabledUseCase)
    factoryOf(::ObserveWebSearchEnabledUseCase)
    factoryOf(::RegenerateLastResponseUseCase)
    factoryOf(::SendPromptUseCase)
    factoryOf(::SetThinkingEnabledUseCase)
    factoryOf(::SetWebSearchEnabledUseCase)
    factoryOf(::StartNewConversationUseCase)
    factoryOf(::RenameConversationUseCase)
    factoryOf(::DeleteAttachmentUseCase)
    factoryOf(::ImportAttachmentUseCase)
}
