package com.nullo.voidapp.core.data.chat.domain

import com.nullo.voidapp.core.data.chat.data.service.InsufficientCreditsException
import com.nullo.voidapp.core.data.chat.data.service.InternalServerException
import com.nullo.voidapp.core.data.chat.data.service.NetworkException
import com.nullo.voidapp.core.data.chat.data.service.RateLimitedException
import com.nullo.voidapp.core.data.chat.data.service.UnauthorizedException
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
     * @throws UnauthorizedException if the API key is invalid or missing
     * @throws InsufficientCreditsException if the account has insufficient credits
     * @throws RateLimitedException if the request was rate limited
     * @throws InternalServerException if the server or the upstream provider failed
     * @throws NetworkException if the request fails for another reason
     */
    fun generateResponse(
        history: List<Message>,
        prompt: Message.Prompt,
        responseMessageId: Long,
        activeModel: ActiveModelRef,
    ): Flow<Message>
}
