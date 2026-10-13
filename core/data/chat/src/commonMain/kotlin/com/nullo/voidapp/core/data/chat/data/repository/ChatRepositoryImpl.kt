package com.nullo.voidapp.core.data.chat.data.repository

import com.nullo.voidapp.core.data.chat.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.chat.data.service.exception.NetworkException
import com.nullo.voidapp.core.data.chat.domain.ChatRepository
import com.nullo.voidapp.core.data.chat.domain.ResponseGenerator
import com.nullo.voidapp.core.database.dao.ConversationDao
import com.nullo.voidapp.core.database.dao.MessageDao
import com.nullo.voidapp.core.database.entity.MessageEntity
import com.nullo.voidapp.core.database.entity.MessageType
import com.nullo.voidapp.core.database.mapper.toDomain
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

internal class ChatRepositoryImpl(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val responseGenerator: ResponseGenerator,
    private val clock: Clock,
) : ChatRepository {

    override suspend fun sendPrompt(
        conversationId: Long,
        query: String,
        activeModel: ActiveModelRef
    ) {
        touchConversation(conversationId)

        val startedGeneration = messageDao.startGeneration(
            conversationId = conversationId,
            query = query,
            modelId = activeModel.id,
            now = clock.now().toEpochMilliseconds()
        )

        generateResponse(
            conversationId = conversationId,
            query = query,
            promptId = startedGeneration.promptId,
            responseId = startedGeneration.responseId,
            activeModel = activeModel
        )
    }

    override suspend fun regenerateLastPrompt(
        conversationId: Long,
        activeModel: ActiveModelRef
    ) {
        touchConversation(conversationId)
        val prompt = messageDao.getLastPrompt(conversationId) ?: return

        val responseId = messageDao.restartGeneration(
            conversationId = conversationId,
            promptId = prompt.id,
            modelId = activeModel.id,
            now = clock.now().toEpochMilliseconds()
        )

        generateResponse(
            conversationId = conversationId,
            query = prompt.text.orEmpty(),
            promptId = prompt.id,
            responseId = responseId,
            activeModel = activeModel
        )
    }

    @OptIn(FlowPreview::class)
    private suspend fun generateResponse(
        conversationId: Long,
        query: String,
        promptId: Long,
        responseId: Long,
        activeModel: ActiveModelRef,
    ) {
        val history = messageDao
            .getMessagesBefore(conversationId, promptId)
            .map { it.toDomain() }
        val prompt = Message.Prompt(
            id = promptId,
            query = query,
            images = emptyList() // todo: add images
        )

        val latest = MutableStateFlow<Message?>(null)
        responseGenerator.generateResponse(
            history = history,
            prompt = prompt,
            responseMessageId = responseId,
            activeModel = activeModel,
        )
            .onEach { latest.value = it }
            .sample(CHECKPOINT_INTERVAL)
            .onCompletion { cause ->
                when (cause) {
                    null -> latest.value?.let { emit(it) }
                    is CancellationException -> withContext(NonCancellable) {
                        (latest.value as? Message.Completion)?.let { persist(it) }
                        messageDao.markInterrupted(responseId)
                    }
                }
            }
            .catch { throwable ->
                (latest.value as? Message.Completion)?.let { persist(it) }
                messageDao.failGeneration(responseId)
                insertErrorMessage(conversationId, throwable.toErrorReason(), promptId)
            }
            .collect { msg ->
                when (msg) {
                    is Message.Completion -> persist(msg)

                    Message.ImageLoading -> {} // todo
                    is Message.Images -> {} // todo

                    is Message.Interrupted,
                    is Message.Prompt,
                    is Message.Error -> return@collect
                }
            }
    }

    override suspend fun dismissError(messageId: Long) {
        messageDao.deleteErrorMessage(messageId)
    }

    private suspend fun persist(message: Message.Completion) {
        messageDao.updateContent(
            messageId = message.id,
            text = message.text.ifBlank { null },
            reasoning = message.reasoning,
            isGenerating = message.isStreaming
        )
    }

    private fun Throwable.toErrorReason(): ErrorReason = when (this) {
        is NetworkException -> ErrorReason.NETWORK
        is InternalServerException -> ErrorReason.SERVER_INTERNAL
        else -> ErrorReason.UNEXPECTED
    }

    private suspend fun insertErrorMessage(
        conversationId: Long,
        errorReason: ErrorReason,
        promptId: Long? = null,
    ) {
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                messageType = MessageType.ERROR,
                errorReason = errorReason.name,
                promptId = promptId
            )
        )
    }

    private suspend fun touchConversation(conversationId: Long) {
        conversationDao.touch(
            conversationId = conversationId,
            updatedAt = clock.now().toEpochMilliseconds()
        )
    }

    companion object {

        private val CHECKPOINT_INTERVAL = 400.milliseconds
    }
}
