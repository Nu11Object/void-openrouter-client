package com.nullo.voidapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.nullo.voidapp.core.database.entity.MessageEntity
import com.nullo.voidapp.core.database.entity.MessageType
import kotlinx.coroutines.flow.Flow

/** IDs of the prompt and response rows created when a generation starts. */
data class StartedGeneration(
    val promptId: Long,
    val responseId: Long,
)

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query(
        """
        SELECT * FROM messages 
        WHERE conversationId = :conversationId 
        ORDER BY id ASC
        """
    )
    fun observeByConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT * FROM messages 
        WHERE conversationId = :conversationId AND id < :messageId 
        ORDER BY id ASC
        """
    )
    suspend fun getMessagesBefore(conversationId: Long, messageId: Long): List<MessageEntity>

    @Query(
        """
        SELECT * FROM messages 
        WHERE conversationId = :conversationId AND messageType = 'PROMPT' 
        ORDER BY id DESC 
        LIMIT 1
        """
    )
    suspend fun getLastPrompt(conversationId: Long): MessageEntity?

    /** Overwrites the content with a cumulative snapshot; pass `isGenerating = false` for the final write. */
    @Query(
        """
        UPDATE messages
        SET 
            text = :text, 
            searchText = :searchText, 
            reasoning = :reasoning, 
            isGenerating = :isGenerating 
        WHERE id = :messageId
        """
    )
    suspend fun updateContent(
        messageId: Long,
        text: String?,
        searchText: String?,
        reasoning: String?,
        isGenerating: Boolean,
    )

    @Query(
        """
        SELECT DISTINCT conversationId FROM messages
        WHERE searchText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
        """
    )
    suspend fun findConversationIdsByText(escapedQuery: String): List<Long>

    /** Turns an unfinished response into an interrupted one (e.g. the user pressed stop). */
    @Query(
        """
        UPDATE messages 
        SET 
            messageType = 'INTERRUPTED', 
            isGenerating = 0 
        WHERE id = :messageId
        """
    )
    suspend fun markInterrupted(messageId: Long)

    @Query(
        """
        UPDATE messages 
        SET 
            messageType = 'INTERRUPTED', 
            isGenerating = 0 
        WHERE isGenerating = 1
        """
    )
    suspend fun closeInterruptedGenerations()

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Query(
        """
        DELETE FROM messages 
        WHERE promptId = :promptId
        """
    )
    suspend fun deleteResponsesOf(promptId: Long)

    @Query(
        """
        DELETE FROM messages 
        WHERE id = :messageId AND messageType = 'ERROR'
        """
    )
    suspend fun deleteErrorMessage(messageId: Long)

    @Query(
        """
        DELETE FROM messages 
        WHERE id = :messageId AND text IS NULL
        """
    )
    suspend fun deleteIfEmpty(messageId: Long)

    /**
     * Closes a failed response: drops it if nothing was generated,
     * otherwise keeps the text as interrupted.
     */
    @Transaction
    suspend fun failGeneration(messageId: Long) {
        deleteIfEmpty(messageId)
        markInterrupted(messageId)
    }

    /**
     * Inserts a new prompt together with an empty response that is being generated.
     *
     * @return the IDs of both messages as a [StartedGeneration]
     */
    @Transaction
    suspend fun startGeneration(
        conversationId: Long,
        query: String?,
        modelId: String?,
        now: Long,
    ): StartedGeneration {
        val promptId = insert(
            MessageEntity(
                conversationId = conversationId,
                messageType = MessageType.PROMPT,
                text = query,
                searchText = query?.lowercase(),
                createdAt = now,
            )
        )
        val responseId = insert(responsePlaceholder(conversationId, promptId, modelId, now))
        return StartedGeneration(promptId, responseId)
    }

    /**
     * Deletes all existing responses associated with [promptId] and creates a new empty response.
     *
     * @return the ID of the newly created response associated with [promptId]
     */
    @Transaction
    suspend fun restartGeneration(
        conversationId: Long,
        promptId: Long,
        modelId: String?,
        now: Long,
    ): Long {
        deleteResponsesOf(promptId)
        return insert(responsePlaceholder(conversationId, promptId, modelId, now))
    }
}

private fun responsePlaceholder(
    conversationId: Long,
    promptId: Long,
    modelId: String?,
    now: Long,
) = MessageEntity(
    conversationId = conversationId,
    messageType = MessageType.COMPLETION,
    promptId = promptId,
    isGenerating = true,
    modelId = modelId,
    createdAt = now,
)
