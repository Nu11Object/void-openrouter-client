package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.MessageEntity
import com.nullo.voidapp.core.database.entity.MessageImageEntity
import com.nullo.voidapp.core.database.entity.MessageType
import com.nullo.voidapp.core.database.entity.MessageWithImages
import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private fun messageEntity(
    id: Long = 1L,
    conversationId: Long = 10L,
    messageType: MessageType = MessageType.PROMPT,
    promptId: Long? = null,
    text: String? = null,
    reasoning: String? = null,
    isGenerating: Boolean = false,
    errorReason: String? = null,
    errorRawMessage: String? = null,
    modelId: String? = null,
    createdAt: Long? = null,
    searchText: String? = null,
) = MessageEntity(
    id = id,
    conversationId = conversationId,
    messageType = messageType,
    promptId = promptId,
    text = text,
    reasoning = reasoning,
    isGenerating = isGenerating,
    errorReason = errorReason,
    errorRawMessage = errorRawMessage,
    modelId = modelId,
    createdAt = createdAt,
    searchText = searchText
)

private fun imageEntity(
    messageId: Long = 1L,
    relativePath: String = "image.png",
    mimeType: String = "image/png",
    position: Int = 0,
) = MessageImageEntity(
    messageId = messageId,
    relativePath = relativePath,
    mimeType = mimeType,
    position = position
)

val MessageMapperTest by testSuite {
    testSuite("toDomain") {
        test("maps prompt message") {
            val entity = messageEntity(
                id = 11L,
                messageType = MessageType.PROMPT,
                text = "Question",
                createdAt = 101L
            )
            val images = listOf(
                imageEntity(relativePath = "second.png", mimeType = "image/jpeg", position = 1),
                imageEntity(relativePath = "first.png", mimeType = "image/png", position = 0)
            )

            val message = MessageWithImages(entity, images).toDomain().shouldBeInstanceOf<Message.Prompt>()

            message.id shouldBe 11L
            message.query shouldBe "Question"
            message.images shouldBe listOf(
                ImageAttachment("first.png", "image/png"),
                ImageAttachment("second.png", "image/jpeg")
            )
            message.createdAt shouldBe 101L
        }

        test("maps completion message") {
            val entity = messageEntity(
                id = 22L,
                messageType = MessageType.COMPLETION,
                text = "Answer",
                reasoning = "Reasoning",
                isGenerating = true,
                modelId = "model/chat",
                createdAt = 202L
            )

            val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Completion>()

            message.id shouldBe 22L
            message.text shouldBe "Answer"
            message.reasoning shouldBe "Reasoning"
            message.isStreaming shouldBe true
            message.modelId shouldBe "model/chat"
            message.createdAt shouldBe 202L
        }

        test("maps generated images message") {
            val entity = messageEntity(
                id = 33L,
                messageType = MessageType.IMAGES,
                isGenerating = false,
                modelId = "model/image",
                createdAt = 303L
            )
            val images = listOf(
                imageEntity(relativePath = "one.png", mimeType = "image/png", position = 1),
                imageEntity(relativePath = "zero.png", mimeType = "image/jpeg", position = 0)
            )

            val message = MessageWithImages(entity, images).toDomain().shouldBeInstanceOf<Message.Images>()

            message.id shouldBe 33L
            message.images shouldBe listOf(
                ImageAttachment("zero.png", "image/jpeg"),
                ImageAttachment("one.png", "image/png")
            )
            message.modelId shouldBe "model/image"
            message.createdAt shouldBe 303L
        }

        test("maps generating images message to image loading") {
            val entity = messageEntity(
                id = 44L,
                messageType = MessageType.IMAGES,
                isGenerating = true,
                text = "ignored",
                modelId = "model/image",
                createdAt = 404L
            )

            val message = MessageWithImages(entity, emptyList()).toDomain()
            message.shouldBeInstanceOf<Message.ImageLoading>()
            message.id shouldBe 44L
        }

        test("maps error message") {
            val entity = messageEntity(
                id = 55L,
                messageType = MessageType.ERROR,
                errorReason = ErrorReason.RATE_LIMITED.name,
                errorRawMessage = "Too many requests"
            )

            val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Error>()

            message.id shouldBe 55L
            message.reason shouldBe ErrorReason.RATE_LIMITED
            message.rawMessage shouldBe "Too many requests"
        }

        test("maps interrupted message") {
            val entity = messageEntity(
                id = 66L,
                messageType = MessageType.INTERRUPTED,
                text = "Partial answer"
            )

            val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Interrupted>()

            message.id shouldBe 66L
            message.text shouldBe "Partial answer"
        }

        test("null prompt text becomes empty query") {
            val entity = messageEntity(
                id = 77L,
                messageType = MessageType.PROMPT,
                text = null
            )

            val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Prompt>()

            message.query shouldBe ""
        }

        test("null completion text becomes empty text") {
            val entity = messageEntity(
                id = 78L,
                messageType = MessageType.COMPLETION,
                text = null
            )

            val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Completion>()

            message.text shouldBe ""
        }

        ErrorReason.entries.forEach { reason ->
            test("restores ${reason.name} error reason") {
                val entity = messageEntity(
                    messageType = MessageType.ERROR,
                    errorReason = reason.name
                )

                val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Error>()

                message.reason shouldBe reason
            }
        }

        listOf("unknown", "", null).forEach { raw ->
            test("unknown error reason \"$raw\" falls back to unexpected") {
                val entity = messageEntity(
                    messageType = MessageType.ERROR,
                    errorReason = raw
                )

                val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Error>()

                message.reason shouldBe ErrorReason.UNEXPECTED
            }
        }

        test("preserves raw error message and null") {
            val rawEntity = messageEntity(
                messageType = MessageType.ERROR,
                errorReason = ErrorReason.NETWORK.name,
                errorRawMessage = "Network failed"
            )
            val nullEntity = messageEntity(
                messageType = MessageType.ERROR,
                errorReason = ErrorReason.NETWORK.name,
                errorRawMessage = null
            )

            val rawMessage = MessageWithImages(rawEntity, emptyList()).toDomain().shouldBeInstanceOf<Message.Error>()
            val nullMessage = MessageWithImages(nullEntity, emptyList()).toDomain().shouldBeInstanceOf<Message.Error>()

            rawMessage.rawMessage shouldBe "Network failed"
            nullMessage.rawMessage shouldBe null
        }

        test("preserves interrupted partial text and null") {
            val textEntity = messageEntity(
                messageType = MessageType.INTERRUPTED,
                text = "Partial answer"
            )
            val nullEntity = messageEntity(
                messageType = MessageType.INTERRUPTED,
                text = null
            )

            val textMessage = MessageWithImages(textEntity, emptyList())
                .toDomain()
                .shouldBeInstanceOf<Message.Interrupted>()
            val nullMessage = MessageWithImages(nullEntity, emptyList())
                .toDomain()
                .shouldBeInstanceOf<Message.Interrupted>()

            textMessage.text shouldBe "Partial answer"
            nullMessage.text shouldBe null
        }

        listOf(
            listOf(2, 0, 1) to listOf(
                ImageAttachment("path-0", "mime-0"),
                ImageAttachment("path-1", "mime-1"),
                ImageAttachment("path-2", "mime-2")
            ),
            listOf(1, 0) to listOf(
                ImageAttachment("path-0", "mime-0"),
                ImageAttachment("path-1", "mime-1")
            )
        ).forEach { (positions, expected) ->
            test("restores attachments in position order for $positions") {
                val images = positions.map { position ->
                    imageEntity(
                        relativePath = "path-$position",
                        mimeType = "mime-$position",
                        position = position
                    )
                }
                val entity = messageEntity(id = 88L, messageType = MessageType.PROMPT, text = "Query")

                val message = MessageWithImages(entity, images).toDomain().shouldBeInstanceOf<Message.Prompt>()

                message.images shouldBe expected
            }
        }

        test("message without images gets an empty list") {
            val entity = messageEntity(
                id = 99L,
                messageType = MessageType.PROMPT,
                text = "Query"
            )

            val message = MessageWithImages(entity, emptyList()).toDomain().shouldBeInstanceOf<Message.Prompt>()

            message.images shouldBe emptyList()
        }
    }

    testSuite("toPrompt") {
        test("maps prompt with attachments") {
            val entity = messageEntity(
                id = 111L,
                messageType = MessageType.PROMPT,
                text = "Find this",
                createdAt = 1111L
            )
            val images = listOf(
                imageEntity(relativePath = "later.png", mimeType = "image/jpeg", position = 1),
                imageEntity(relativePath = "earlier.png", mimeType = "image/png", position = 0)
            )

            val prompt = MessageWithImages(entity, images).toPrompt()

            prompt.id shouldBe 111L
            prompt.query shouldBe "Find this"
            prompt.images shouldBe listOf(
                ImageAttachment("earlier.png", "image/png"),
                ImageAttachment("later.png", "image/jpeg")
            )
            prompt.createdAt shouldBe 1111L
        }

        test("maps prompt without attachments") {
            val entity = messageEntity(
                id = 222L,
                messageType = MessageType.PROMPT,
                text = "Find this",
                createdAt = 2222L
            )

            val prompt = MessageWithImages(entity, emptyList()).toPrompt()

            prompt.id shouldBe 222L
            prompt.query shouldBe "Find this"
            prompt.images shouldBe emptyList()
            prompt.createdAt shouldBe 2222L
        }
    }

    testSuite("toEntity") {
        test("attachments are written with consecutive positions starting at zero") {
            val attachments = listOf(
                ImageAttachment("first.png", "image/png"),
                ImageAttachment("second.jpg", "image/jpeg")
            )

            val entities = attachments.toEntities(messageId = 333L)

            entities.map { it.messageId } shouldBe listOf(333L, 333L)
            entities.map { it.relativePath } shouldBe listOf("first.png", "second.jpg")
            entities.map { it.mimeType } shouldBe listOf("image/png", "image/jpeg")
            entities.map { it.position } shouldBe listOf(0, 1)
        }

        test("empty attachments produce no entities") {
            emptyList<ImageAttachment>().toEntities(messageId = 444L) shouldBe emptyList()
        }
    }

    testSuite("attachments round trip") {
        test("prompt attachments survive toEntities and back") {
            val attachments = listOf(
                ImageAttachment("first.png", "image/png"),
                ImageAttachment("second.jpg", "image/jpeg")
            )
            val entity = messageEntity(
                id = 555L,
                messageType = MessageType.PROMPT,
                text = "Query",
                createdAt = 5555L
            )

            val prompt = MessageWithImages(entity, attachments.toEntities(entity.id))
                .toDomain()
                .shouldBeInstanceOf<Message.Prompt>()

            prompt.id shouldBe 555L
            prompt.query shouldBe "Query"
            prompt.images shouldBe attachments
            prompt.createdAt shouldBe 5555L
        }
    }
}
