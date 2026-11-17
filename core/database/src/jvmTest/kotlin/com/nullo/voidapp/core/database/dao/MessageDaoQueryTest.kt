package com.nullo.voidapp.core.database.dao

import com.nullo.voidapp.core.database.buildTestDatabase
import com.nullo.voidapp.core.database.entity.MessageType
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

val MessageDaoQueryTest by testSuite {
    testFixture { buildTestDatabase() } closeWith { close() } asParameterForEach {
        test("getImagePathsOfResponses returns paths for one prompt only") { database ->
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
                    messageType = MessageType.IMAGES,
                    promptId = firstPrompt
                )
            )
            val secondResponse = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.IMAGES,
                    promptId = secondPrompt
                )
            )
            database.messageDao().insertImages(
                listOf(
                    messageImageEntity(messageId = firstResponse, relativePath = "first-a.png", position = 0),
                    messageImageEntity(messageId = firstResponse, relativePath = "first-b.png", position = 1),
                    messageImageEntity(messageId = secondResponse, relativePath = "second.png", position = 0)
                )
            )

            database.messageDao().getImagePathsOfResponses(firstPrompt).toSet() shouldBe
                setOf("first-a.png", "first-b.png")
        }

        test("findConversationIdsByText finds a substring") { database ->
            val conversationId = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    searchText = "hello world"
                )
            )

            database.messageDao().findConversationIdsByText("ell") shouldBe listOf(conversationId)
        }

        test("findConversationIdsByText finds Cyrillic through search text") { database ->
            val conversationId = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    searchText = "привет мир"
                )
            )

            database.messageDao().findConversationIdsByText("привет") shouldBe listOf(conversationId)
        }

        test("findConversationIdsByText returns one id for two matches in one conversation") { database ->
            val conversationId = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    searchText = "alpha"
                )
            )
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    searchText = "alphabet"
                )
            )

            database.messageDao().findConversationIdsByText("alpha") shouldBe listOf(conversationId)
        }

        test("findConversationIdsByText returns an empty list for no match") { database ->
            val conversationId = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    searchText = "hello"
                )
            )

            database.messageDao().findConversationIdsByText("missing") shouldBe emptyList()
        }

        test("findConversationIdsByText never matches null search text") { database ->
            val conversationId = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    searchText = null
                )
            )

            database.messageDao().findConversationIdsByText("anything") shouldBe emptyList()
        }

        test("findConversationIdsByText handles escaped percent") { database ->
            val matchingConversation = database.newConversation()
            val otherConversation = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = matchingConversation,
                    messageType = MessageType.COMPLETION,
                    searchText = "100%"
                )
            )
            database.messageDao().insert(
                messageEntity(
                    conversationId = otherConversation,
                    messageType = MessageType.COMPLETION,
                    searchText = "1000"
                )
            )

            database.messageDao().findConversationIdsByText("100\\%") shouldBe listOf(matchingConversation)
        }

        test("findConversationIdsByText handles escaped underscore") { database ->
            val matchingConversation = database.newConversation()
            val otherConversation = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = matchingConversation,
                    messageType = MessageType.COMPLETION,
                    searchText = "a_b"
                )
            )
            database.messageDao().insert(
                messageEntity(
                    conversationId = otherConversation,
                    messageType = MessageType.COMPLETION,
                    searchText = "axb"
                )
            )

            database.messageDao().findConversationIdsByText("a\\_b") shouldBe listOf(matchingConversation)
        }

        test("observeByConversation orders by id and includes images") { database ->
            val firstConversation = database.newConversation()
            val secondConversation = database.newConversation()
            val firstMessage = database.messageDao().insert(
                messageEntity(
                    conversationId = firstConversation,
                    messageType = MessageType.PROMPT,
                    text = "First"
                )
            )
            val secondMessage = database.messageDao().insert(
                messageEntity(
                    conversationId = firstConversation,
                    messageType = MessageType.COMPLETION,
                    text = "Second"
                )
            )
            val otherMessage = database.messageDao().insert(
                messageEntity(
                    conversationId = secondConversation,
                    messageType = MessageType.PROMPT,
                    text = "Other"
                )
            )
            database.messageDao().insertImages(
                listOf(messageImageEntity(messageId = firstMessage, relativePath = "first.png"))
            )

            val messages = database.messagesOf(firstConversation)

            messages.map { it.message.id } shouldBe listOf(firstMessage, secondMessage)
            messages.first().images.map { it.relativePath } shouldBe listOf("first.png")
            messages.map { it.message.id } shouldNotContain otherMessage
        }

        test("getMessagesBefore returns strictly earlier messages in ascending order") { database ->
            val conversationId = database.newConversation()
            val firstMessage = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "First"
                )
            )
            val secondMessage = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "Second"
                )
            )
            val thirdMessage = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "Third"
                )
            )

            val messages = database.messageDao().getMessagesBefore(conversationId, thirdMessage)

            messages.map { it.message.id } shouldBe listOf(firstMessage, secondMessage)
        }

        test("getLastPrompt returns the last prompt with images") { database ->
            val conversationId = database.newConversation()
            val firstPrompt = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "First"
                )
            )
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "Completion"
                )
            )
            val lastPrompt = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.PROMPT,
                    text = "Last"
                )
            )
            database.messageDao().insertImages(
                listOf(
                    messageImageEntity(messageId = firstPrompt, relativePath = "first.png"),
                    messageImageEntity(messageId = lastPrompt, relativePath = "last.png")
                )
            )

            val stored = database.messageDao().getLastPrompt(conversationId)

            stored?.message?.id shouldBe lastPrompt
            stored?.images?.map { it.relativePath } shouldBe listOf("last.png")
        }

        test("getLastPrompt returns null when there is no prompt") { database ->
            val conversationId = database.newConversation()
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "Completion"
                )
            )

            database.messageDao().getLastPrompt(conversationId) shouldBe null
        }
    }
}
