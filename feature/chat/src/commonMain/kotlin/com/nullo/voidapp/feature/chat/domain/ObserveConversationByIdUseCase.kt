package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.model.Conversation
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing conversation by its ID. */
internal class ObserveConversationByIdUseCase(private val conversationRepository: ConversationRepository) {

    operator fun invoke(conversationId: Long): Flow<Conversation?> = conversationRepository.observeConversationById(
        conversationId
    )
}
