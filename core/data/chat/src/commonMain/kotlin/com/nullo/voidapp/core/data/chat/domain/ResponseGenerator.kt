package com.nullo.voidapp.core.data.chat.domain

import com.nullo.voidapp.core.data.chat.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.chat.data.service.exception.NetworkException
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Generates responses using the currently active model.
 * @see [generateResponse]
 */
interface ResponseGenerator {

    /**
     * Generates a response to [prompt] using the active model and [history] as context.
     *
     * Emits cumulative [Message.Completion] snapshots for chat models,
     * or a single [Message.Images] result for image models.
     * Cancelling collection stops generation.
     *
     * @param history previous messages used as context
     * @param prompt prompt to respond to
     * @param responseMessageId ID of the response message
     * @param activeModel currently active model
     * @return a flow of generated response messages
     * @throws InternalServerException if the server returns an internal error
     * @throws NetworkException if the request fails for another reason
     */
    fun generateResponse(
        history: List<Message>,
        prompt: Message.Prompt,
        responseMessageId: Long,
        activeModel: ActiveModelRef,
    ): Flow<Message>
}
