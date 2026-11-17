package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.ConversationEntity
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun conversationEntity(
    id: Long = 1L,
    title: String? = "Conversation",
    createdAt: Long = 100L,
    updatedAt: Long = 200L,
) = ConversationEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

val ConversationMapperTest by testSuite {
    testSuite("toDomain") {
        test("maps all fields") {
            val entity = conversationEntity(
                id = 42L,
                title = "Test Conversation",
                createdAt = 123L,
                updatedAt = 456L
            )

            val conversation = entity.toDomain()

            conversation.id shouldBe 42L
            conversation.title shouldBe "Test Conversation"
            conversation.createdAt shouldBe 123L
            conversation.updatedAt shouldBe 456L
        }

        test("null title stays null") {
            conversationEntity(title = null).toDomain().title shouldBe null
        }
    }
}
