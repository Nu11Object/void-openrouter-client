package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.ChatRepository

/** Use case responsible for dismissing the error message in a conversation. */
internal class DismissErrorMessageUseCase(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(messageId: Long) {
        chatRepository.dismissError(messageId)
    }
}
