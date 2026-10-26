package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.model.Conversation
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing conversations. */
internal class ObserveConversationsUseCase(private val conversationRepository: ConversationRepository) {

    operator fun invoke(): Flow<List<Conversation>> = conversationRepository.observeConversations()
}
