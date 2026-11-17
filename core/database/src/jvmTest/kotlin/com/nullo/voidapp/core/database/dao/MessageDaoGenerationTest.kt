package com.nullo.voidapp.core.database.dao

import com.nullo.voidapp.core.database.buildTestDatabase
import com.nullo.voidapp.core.database.entity.MessageType
import com.nullo.voidapp.core.model.ImageAttachment
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

val MessageDaoGenerationTest by testSuite {
    testFixture { buildTestDatabase() } closeWith { close() } asParameterForEach {
        test("markInterrupted does not change a finished message") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "done"
                )
            )

            database.messageDao().markInterrupted(messageId)
            val stored = database.storedMessage(conversationId, messageId)

            stored.messageType shouldBe MessageType.COMPLETION
            stored.isGenerating shouldBe false
            stored.text shouldBe "done"
        }

        test("startGeneration creates a prompt row and a response row") { database ->
            val conversationId = database.newConversation()

            val result = database.messageDao().startGeneration(
                conversationId = conversationId,
                query = "Hello",
                images = emptyList(),
                responseType = MessageType.COMPLETION,
                modelId = "model/chat",
                now = 100L
            )

            val messages = database.messagesOf(conversationId)
            val prompt = messages.single { it.message.id == result.promptId }.message
            val response = messages.single { it.message.id == result.responseId }.message

            prompt.messageType shouldBe MessageType.PROMPT
            prompt.text shouldBe "Hello"
            prompt.searchText shouldBe "hello"
            prompt.createdAt shouldBe 100L
            prompt.isGenerating shouldBe false
            prompt.promptId shouldBe null

            response.messageType shouldBe MessageType.COMPLETION
            response.promptId shouldBe result.promptId
            response.isGenerating shouldBe true
            response.modelId shouldBe "model/chat"
            response.createdAt shouldBe 100L
            response.text shouldBe null

            messages.single { it.message.id == result.responseId }.images shouldBe emptyList()
        }

        test("startGeneration lowercases Cyrillic and mixed case search text") { database ->
            val conversationId = database.newConversation()

            val result = database.messageDao().startGeneration(
                conversationId = conversationId,
                query = "Привет МИР",
                images = emptyList(),
                responseType = MessageType.COMPLETION,
                modelId = null,
                now = 1L
            )

            val prompt = database.storedMessage(conversationId, result.promptId)

            prompt.searchText shouldBe "привет мир"
        }

        test("startGeneration keeps null query text and search text") { database ->
            val conversationId = database.newConversation()

            val result = database.messageDao().startGeneration(
                conversationId = conversationId,
                query = null,
                images = emptyList(),
                responseType = MessageType.COMPLETION,
                modelId = null,
                now = 1L
            )

            val prompt = database.storedMessage(conversationId, result.promptId)

            prompt.text shouldBe null
            prompt.searchText shouldBe null
        }

        test("startGeneration stores prompt images with consecutive positions") { database ->
            val conversationId = database.newConversation()
            val attachments = listOf(
                ImageAttachment("first.png", "image/png"),
                ImageAttachment("second.jpg", "image/jpeg")
            )

            val result = database.messageDao().startGeneration(
                conversationId = conversationId,
                query = "Query",
                images = attachments,
                responseType = MessageType.IMAGES,
                modelId = "model/image",
                now = 1L
            )

            val messages = database.messagesOf(conversationId)
            val promptImages = messages.single { it.message.id == result.promptId }.images.sortedBy { it.position }
            val responseImages = messages.single { it.message.id == result.responseId }.images

            promptImages.map { it.relativePath } shouldBe listOf("first.png", "second.jpg")
            promptImages.map { it.mimeType } shouldBe listOf("image/png", "image/jpeg")
            promptImages.map { it.position } shouldBe listOf(0, 1)
            responseImages shouldBe emptyList()
        }

        test("restartGeneration deletes old responses and creates one generating response") { database ->
            val conversationId = database.newConversation()
            val started = database.messageDao().startGeneration(
                conversationId = conversationId,
                query = "Prompt",
                images = emptyList(),
                responseType = MessageType.COMPLETION,
                modelId = "model/old",
                now = 10L
            )
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    promptId = started.promptId,
                    text = "old completion"
                )
            )
            database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.ERROR,
                    promptId = started.promptId,
                    errorReason = "NETWORK"
                )
            )
            val oldImageResponse = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.IMAGES,
                    promptId = started.promptId
                )
            )
            database.messageDao().insertImages(
                listOf(messageImageEntity(messageId = oldImageResponse, relativePath = "old.png"))
            )
            val otherStarted = database.messageDao().startGeneration(
                conversationId = conversationId,
                query = "Other prompt",
                images = emptyList(),
                responseType = MessageType.COMPLETION,
                modelId = "model/other",
                now = 20L
            )

            val newResponseId = database.messageDao().restartGeneration(
                conversationId = conversationId,
                promptId = started.promptId,
                responseType = MessageType.IMAGES,
                modelId = "model/new",
                now = 300L
            )

            val messages = database.messagesOf(conversationId)
            val promptResponses = messages.filter { it.message.promptId == started.promptId }
            val newResponse = promptResponses.single().message

            promptResponses.size shouldBe 1
            newResponse.id shouldBe newResponseId
            newResponse.messageType shouldBe MessageType.IMAGES
            newResponse.promptId shouldBe started.promptId
            newResponse.isGenerating shouldBe true
            newResponse.modelId shouldBe "model/new"
            newResponse.createdAt shouldBe 300L
            newResponse.text shouldBe null
            promptResponses.single().images shouldBe emptyList()
            database.messageDao().getImagePathsOfResponses(started.promptId) shouldBe emptyList()

            messages.single { it.message.id == started.promptId }.message.text shouldBe "Prompt"
            messages.filter { it.message.promptId == otherStarted.promptId }.size shouldBe 1
        }

        test("updateContent writes fields and overwrites the previous snapshot") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "old",
                    reasoning = "old reasoning",
                    isGenerating = true,
                    searchText = "old"
                )
            )

            database.messageDao().updateContent(
                messageId = messageId,
                text = "New text",
                searchText = "new text",
                reasoning = "New reasoning",
                isGenerating = false
            )
            val first = database.storedMessage(conversationId, messageId)

            first.text shouldBe "New text"
            first.searchText shouldBe "new text"
            first.reasoning shouldBe "New reasoning"
            first.isGenerating shouldBe false

            database.messageDao().updateContent(
                messageId = messageId,
                text = "Second text",
                searchText = "second text",
                reasoning = null,
                isGenerating = true
            )
            val second = database.storedMessage(conversationId, messageId)

            second.text shouldBe "Second text"
            second.searchText shouldBe "second text"
            second.reasoning shouldBe null
            second.isGenerating shouldBe true
        }

        test("updateContent allows null text") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "old",
                    searchText = "old"
                )
            )

            database.messageDao().updateContent(
                messageId = messageId,
                text = null,
                searchText = null,
                reasoning = "Reasoning only",
                isGenerating = false
            )
            val stored = database.storedMessage(conversationId, messageId)

            stored.text shouldBe null
            stored.searchText shouldBe null
            stored.reasoning shouldBe "Reasoning only"
        }

        test("markInterrupted turns a generating response into interrupted") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "partial",
                    isGenerating = true,
                    searchText = "partial"
                )
            )

            database.messageDao().markInterrupted(messageId)
            val stored = database.storedMessage(conversationId, messageId)

            stored.messageType shouldBe MessageType.INTERRUPTED
            stored.isGenerating shouldBe false
            stored.text shouldBe "partial"
            stored.searchText shouldBe "partial"
        }

        test("closeInterruptedGenerations interrupts all generating messages") { database ->
            val firstConversation = database.newConversation()
            val secondConversation = database.newConversation()
            val finishedConversation = database.newConversation()
            val firstId = database.messageDao().insert(
                messageEntity(
                    conversationId = firstConversation,
                    messageType = MessageType.COMPLETION,
                    text = "one",
                    isGenerating = true
                )
            )
            val secondId = database.messageDao().insert(
                messageEntity(
                    conversationId = secondConversation,
                    messageType = MessageType.IMAGES,
                    isGenerating = true
                )
            )
            val finishedId = database.messageDao().insert(
                messageEntity(
                    conversationId = finishedConversation,
                    messageType = MessageType.COMPLETION,
                    text = "done",
                    isGenerating = false
                )
            )

            database.messageDao().closeInterruptedGenerations()

            val first = database.storedMessage(firstConversation, firstId)
            val second = database.storedMessage(secondConversation, secondId)
            val finished = database.storedMessage(finishedConversation, finishedId)

            first.messageType shouldBe MessageType.INTERRUPTED
            first.isGenerating shouldBe false
            first.text shouldBe "one"
            second.messageType shouldBe MessageType.INTERRUPTED
            second.isGenerating shouldBe false
            finished.messageType shouldBe MessageType.COMPLETION
            finished.isGenerating shouldBe false
            finished.text shouldBe "done"
        }

        test("failGeneration deletes an empty response") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = null,
                    isGenerating = true
                )
            )

            database.messageDao().failGeneration(messageId)

            val ids = database.messagesOf(conversationId)
                .map { it.message.id }

            ids shouldNotContain messageId
        }

        test("failGeneration interrupts a response with text") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = "partial",
                    isGenerating = true
                )
            )

            database.messageDao().failGeneration(messageId)
            val stored = database.storedMessage(conversationId, messageId)

            stored.messageType shouldBe MessageType.INTERRUPTED
            stored.isGenerating shouldBe false
            stored.text shouldBe "partial"
        }

        test("failGeneration deletes a response with reasoning but no text") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.COMPLETION,
                    text = null,
                    reasoning = "reasoning",
                    isGenerating = true
                )
            )

            database.messageDao().failGeneration(messageId)

            val ids = database.messagesOf(conversationId)
                .map { it.message.id }

            ids shouldNotContain messageId
        }

        test("completeImageGeneration stops generation and inserts images") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.IMAGES,
                    isGenerating = true
                )
            )
            val attachments = listOf(
                ImageAttachment("first.png", "image/png"),
                ImageAttachment("second.jpg", "image/jpeg")
            )

            database.messageDao().completeImageGeneration(messageId, attachments)

            val stored = database.messagesOf(conversationId)
                .single { it.message.id == messageId }
            val images = stored.images.sortedBy { it.position }

            stored.message.isGenerating shouldBe false
            images.map { it.relativePath } shouldBe listOf("first.png", "second.jpg")
            images.map { it.position } shouldBe listOf(0, 1)
        }

        test("completeImageGeneration does nothing on a second call") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.IMAGES,
                    isGenerating = true
                )
            )

            database.messageDao().completeImageGeneration(
                messageId,
                listOf(ImageAttachment("first.png", "image/png"))
            )
            database.messageDao().completeImageGeneration(
                messageId,
                listOf(ImageAttachment("second.png", "image/png"))
            )

            val stored = database.messagesOf(conversationId)
                .single { it.message.id == messageId }

            stored.images.map { it.relativePath } shouldBe listOf("first.png")
        }

        test("completeImageGeneration inserts nothing for a finished message") { database ->
            val conversationId = database.newConversation()
            val messageId = database.messageDao().insert(
                messageEntity(
                    conversationId = conversationId,
                    messageType = MessageType.IMAGES,
                    isGenerating = false
                )
            )

            database.messageDao().completeImageGeneration(
                messageId,
                listOf(ImageAttachment("first.png", "image/png"))
            )

            val stored = database.messagesOf(conversationId)
                .single { it.message.id == messageId }

            stored.images shouldBe emptyList()
        }
    }
}
