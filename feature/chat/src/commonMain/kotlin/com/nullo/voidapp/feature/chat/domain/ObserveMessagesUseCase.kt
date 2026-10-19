package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing messages of the conversation. */
internal class ObserveMessagesUseCase(
    private val conversationRepository: ConversationRepository,
) {

    operator fun invoke(conversationId: Long): Flow<List<Message>> {
        return conversationRepository.observeMessages(conversationId)
    }
}
