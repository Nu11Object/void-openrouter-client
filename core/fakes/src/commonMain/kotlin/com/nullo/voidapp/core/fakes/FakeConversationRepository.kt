package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeConversationRepository(
    initialConversations: List<Conversation> = emptyList(),
    initialMessages: Map<Long, List<Message>> = emptyMap(),
    initialConversationIdsByText: Set<Long> = emptySet(),
) : ConversationRepository {

    private val conversations = MutableStateFlow(initialConversations)
    private val messages = MutableStateFlow(initialMessages)
    private val conversationIdsByText = MutableStateFlow(initialConversationIdsByText)
    private var nextConversationId = (initialConversations.maxOfOrNull { it.id } ?: 0L) + 1L

    val calls: List<String>
        field = mutableListOf<String>()

    var findConversationIdsByTextFailure: Throwable? = null
    var createConversationFailure: Throwable? = null
    var renameFailure: Throwable? = null
    var deleteFailure: Throwable? = null
    var deleteAllFailure: Throwable? = null
    var cleanUpAfterPreviousSessionFailure: Throwable? = null

    override fun observeConversations(): Flow<List<Conversation>> = conversations

    override fun observeConversationById(conversationId: Long): Flow<Conversation?> =
        conversations.map { list -> list.firstOrNull { it.id == conversationId } }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        messages.map { it[conversationId].orEmpty() }

    override suspend fun findConversationIdsByText(query: String): Set<Long> {
        calls += "findConversationIdsByText:$query"
        findConversationIdsByTextFailure?.let { throw it }
        return conversationIdsByText.value
    }

    override suspend fun createConversation(firstQuery: String): Long {
        calls += "createConversation:$firstQuery"
        createConversationFailure?.let { throw it }
        val id = nextConversationId++
        conversations.update { list ->
            list + Conversation(
                id = id,
                title = firstQuery,
                createdAt = 0L,
                updatedAt = 0L
            )
        }
        return id
    }

    override suspend fun rename(conversationId: Long, newTitle: String) {
        calls += "rename:$conversationId:$newTitle"
        renameFailure?.let { throw it }
        conversations.update { list ->
            list.map { conversation ->
                if (conversation.id == conversationId) conversation.copy(title = newTitle) else conversation
            }
        }
    }

    override suspend fun delete(conversationId: Long) {
        calls += "delete:$conversationId"
        deleteFailure?.let { throw it }
        conversations.update { list -> list.filterNot { it.id == conversationId } }
        messages.update { map -> map - conversationId }
    }

    override suspend fun deleteAll() {
        calls += "deleteAll"
        deleteAllFailure?.let { throw it }
        conversations.update { emptyList() }
        messages.update { emptyMap() }
        conversationIdsByText.update { emptySet() }
    }

    override suspend fun cleanUpAfterPreviousSession() {
        calls += "cleanUpAfterPreviousSession"
        cleanUpAfterPreviousSessionFailure?.let { throw it }
    }
}
