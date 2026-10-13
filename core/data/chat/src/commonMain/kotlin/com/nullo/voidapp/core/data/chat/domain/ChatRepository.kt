package com.nullo.voidapp.core.data.chat.domain

import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Message

/** Manages chat interactions, including response generation and error handling. */
interface ChatRepository {

    /**
     * Sends [query] to the active model in the specified conversation and persists the response
     * as it is generated. Generation failures are saved as [Message.Error] messages
     * instead of being thrown. Cancellation is propagated to the caller.
     */
    suspend fun sendPrompt(conversationId: Long, query: String, activeModel: ActiveModelRef)

    /**
     * Regenerates the response to the last prompt in the specified conversation,
     * replacing its previous response.
     */
    suspend fun regenerateLastPrompt(conversationId: Long, activeModel: ActiveModelRef)

    /**
     * Dismisses the error message identified by [messageId] by deleting it.
     */
    suspend fun dismissError(messageId: Long)
}
