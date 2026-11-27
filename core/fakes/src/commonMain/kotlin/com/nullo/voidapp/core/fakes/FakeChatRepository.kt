package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.data.chat.domain.ChatRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ImageAttachment
import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class FakeChatRepository : ChatRepository {

    val calls: List<String>
        field = mutableListOf<String>()

    var sendPromptFailure: Throwable? = null
    var regenerateFailure: Throwable? = null
    var dismissErrorFailure: Throwable? = null

    var sendPromptDelay: Duration = Duration.ZERO
    var regenerateDelay: Duration = Duration.ZERO

    var sendPromptCompletions: Int = 0
        private set

    var sendPromptCancellations: Int = 0
        private set

    override suspend fun sendPrompt(
        conversationId: Long,
        query: String,
        images: List<ImageAttachment>,
        activeModel: ActiveModelRef,
    ) {
        calls += "sendPrompt:$conversationId:$query:${images.size}:${activeModel.id}"
        try {
            delay(sendPromptDelay)
        } catch (e: CancellationException) {
            sendPromptCancellations++
            throw e
        }
        sendPromptFailure?.let { throw it }
        sendPromptCompletions++
    }

    override suspend fun regenerateLastResponse(
        conversationId: Long,
        activeModel: ActiveModelRef,
    ) {
        calls += "regenerateLastResponse:$conversationId:${activeModel.id}"
        delay(regenerateDelay)
        regenerateFailure?.let { throw it }
    }

    override suspend fun dismissError(messageId: Long) {
        calls += "dismissError:$messageId"
        dismissErrorFailure?.let { throw it }
    }
}
