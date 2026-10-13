package com.nullo.voidapp.core.data.chat.domain

import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.Message
import kotlinx.coroutines.flow.Flow

/** Provides access to conversations and their messages. */
interface ConversationRepository {

    /** Observes all conversations. */
    fun observeConversations(): Flow<List<Conversation>>

    /** Observes a conversation by [conversationId], or `null` if it does not exist. */
    fun observeConversation(conversationId: Long): Flow<Conversation?>

    /** Observes messages in the specified conversation. */
    fun observeMessages(conversationId: Long): Flow<List<Message>>

    /** Creates a conversation titled after [firstQuery] and returns its ID. */
    suspend fun createConversation(firstQuery: String): Long

    /**
     * Renames the specified conversation.
     *
     * @throws IllegalArgumentException if [newTitle] is blank.
     */
    suspend fun rename(conversationId: Long, newTitle: String)

    /** Deletes the specified conversation. */
    suspend fun delete(conversationId: Long)

    /**
     * Closes generations interrupted by a process termination
     * and deletes empty conversations.
     * Should be called once when the application starts.
     */
    suspend fun cleanUpAfterPreviousSession()
}
