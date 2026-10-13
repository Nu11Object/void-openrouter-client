package com.nullo.voidapp.core.data.chat.data.repository

import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.database.dao.ConversationDao
import com.nullo.voidapp.core.database.dao.MessageDao
import com.nullo.voidapp.core.database.entity.ConversationEntity
import com.nullo.voidapp.core.database.mapper.toDomain
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.utils.kotlin.mapItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

internal class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val clock: Clock,
) : ConversationRepository {

    override fun observeConversations(): Flow<List<Conversation>> = conversationDao
        .observeAll()
        .mapItems { it.toDomain() }

    override fun observeConversation(conversationId: Long): Flow<Conversation?> = conversationDao
        .observeById(conversationId)
        .map { it?.toDomain() }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> = messageDao
        .observeByConversation(conversationId)
        .mapItems { it.toDomain() }

    override suspend fun createConversation(firstQuery: String): Long {
        val now = clock.now().toEpochMilliseconds()
        val conversation = ConversationEntity(
            title = firstQuery.asDerivedTitle(),
            createdAt = now,
            updatedAt = now
        )
        return conversationDao.insert(conversation)
    }

    override suspend fun rename(conversationId: Long, newTitle: String) {
        require(newTitle.isNotBlank())
        conversationDao.rename(conversationId, newTitle)
    }

    override suspend fun delete(conversationId: Long) {
        conversationDao.delete(conversationId)
    }

    override suspend fun cleanUpAfterPreviousSession() {
        ignoreFail { messageDao.closeInterruptedGenerations() }
        ignoreFail { conversationDao.deleteEmpty() }
    }

    private suspend fun ignoreFail(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    private fun String.asDerivedTitle() = this
        .trimStart()
        .take(TITLE_MAX_LENGTH)
        .dropLastWhile { it.isHighSurrogate() }
        .trimEnd()
        .takeIf { it.isNotBlank() }

    companion object {

        private const val TITLE_MAX_LENGTH = 50
    }
}
