package com.nullo.voidapp.core.data.chat.data.mapper

import com.nullo.voidapp.core.data.chat.data.dto.chat.ChatCompletionChunkDto
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal fun Flow<ChatCompletionChunkDto>.toCompletionMessageFlow(
    messageId: Long,
): Flow<Message.Completion> {
    val upstream = this

    return flow {
        var current = Message.Completion(
            id = messageId,
            text = "",
            isStreaming = true
        )
        emit(current)

        var failure: Throwable? = null
        upstream
            .catch { failure = it }
            .collect { chunk ->
                val delta = chunk.choices.firstOrNull()?.delta
                if (
                    delta == null
                    || (delta.content.isNullOrEmpty() && delta.reasoning.isNullOrEmpty())
                ) {
                    return@collect // skip service/ping chunks
                }
                current = current.copy(
                    text = current.text + delta.content.orEmpty(),
                    reasoning = current.reasoning.append(delta.reasoning),
                )
                emit(current)
            }

        emit(current.copy(isStreaming = false))
        failure?.let { throw it }
    }
}

private fun String?.append(delta: String?): String? = if (delta == null) this else orEmpty() + delta
