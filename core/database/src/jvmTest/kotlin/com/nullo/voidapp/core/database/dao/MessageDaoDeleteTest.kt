package com.nullo.voidapp.core.database.dao

import com.nullo.voidapp.core.database.buildTestDatabase
import com.nullo.voidapp.core.database.entity.MessageType
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain

val MessageDaoDeleteTest by testSuite {
    testFixture { buildTestDatabase() } closeWith { close() } asParameterForEach {
        test("deleteResponsesOf deletes only responses of the given prompt") { database ->
            val conversationId = database.newConversation()
            val firstPrompt = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "First"
                )
            )
            val secondPrompt = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "Second"
                )
            )
            val firstResponse = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    promptId = firstPrompt
                )
            )
            val secondResponse = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    promptId = secondPrompt
                )
            )

            database.messageDao().deleteResponsesOf(firstPrompt)

            val messages = database.messagesOf(conversationId)
            messages.map { it.message.id } shouldNotContain firstResponse
            messages.map { it.message.id } shouldContain secondResponse
            messages.map { it.message.id } shouldContain firstPrompt
        }

        test("deleteErrorMessage deletes only error rows") { database ->
            val conversationId = database.newConversation()
            val promptId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "Prompt"
                )
            )
            val errorId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.ERROR,
                    errorReason = "NETWORK"
                )
            )

            database.messageDao().deleteErrorMessage(promptId)
            val afterPromptDelete = database.messagesOf(conversationId)

            afterPromptDelete.map { it.message.id } shouldContain promptId

            database.messageDao().deleteErrorMessage(errorId)
            val afterErrorDelete = database.messagesOf(conversationId)

            afterErrorDelete.map { it.message.id } shouldNotContain errorId
            afterErrorDelete.map { it.message.id } shouldContain promptId
        }

        test("deleteIfEmpty deletes null text and keeps non-null text") { database ->
            val conversationId = database.newConversation()
            val nullTextId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = null
                )
            )
            val nonNullTextId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "value"
                )
            )
            val emptyTextId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = ""
                )
            )

            database.messageDao().deleteIfEmpty(nullTextId)
            database.messageDao().deleteIfEmpty(nonNullTextId)
            database.messageDao().deleteIfEmpty(emptyTextId)

            val ids = database.messagesOf(conversationId)
                .map { it.message.id }

            ids shouldNotContain nullTextId
            ids shouldContain nonNullTextId
            ids shouldContain emptyTextId
        }
    }
}
