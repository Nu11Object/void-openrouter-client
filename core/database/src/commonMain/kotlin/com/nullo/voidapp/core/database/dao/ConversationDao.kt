package com.nullo.voidapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.nullo.voidapp.core.database.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert
    suspend fun insert(entity: ConversationEntity): Long

    @Query(
        """
        SELECT * FROM conversations
        ORDER BY updatedAt DESC
        """
    )
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT * FROM conversations
        WHERE id = :conversationId
        LIMIT 1
        """
    )
    fun observeById(conversationId: Long): Flow<ConversationEntity?>

    @Query(
        """
        UPDATE conversations
        SET title = :newTitle
        WHERE id = :conversationId
        """
    )
    suspend fun rename(conversationId: Long, newTitle: String)

    @Query(
        """
        UPDATE conversations
        SET updatedAt = :updatedAt
        WHERE id = :conversationId
        """
    )
    suspend fun touch(conversationId: Long, updatedAt: Long)

    @Query(
        """
        DELETE FROM conversations
        WHERE id = :conversationId
        """
    )
    suspend fun delete(conversationId: Long)

    @Query(
        """
        DELETE FROM conversations
        WHERE NOT EXISTS (
            SELECT 1
            FROM messages
            WHERE messages.conversationId = conversations.id
        )
        """
    )
    suspend fun deleteEmpty()

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}
