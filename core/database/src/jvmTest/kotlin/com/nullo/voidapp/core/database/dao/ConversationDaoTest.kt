package com.nullo.voidapp.core.database.dao

import androidx.sqlite.SQLiteException
import com.nullo.voidapp.core.database.buildTestDatabase
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

val ConversationDaoTest by testSuite {
    testFixture { buildTestDatabase() } closeWith { close() } asParameterForEach {
        test("insert returns generated ids in increasing order") { database ->
            val dao = database.conversationDao()

            val firstId = dao.insert(conversationEntity())
            val secondId = dao.insert(conversationEntity())

            secondId shouldBeGreaterThan firstId
        }

        test("observeAll is ordered by updatedAt descending") { database ->
            val dao = database.conversationDao()
            dao.insert(conversationEntity(createdAt = 1L, updatedAt = 100L))
            dao.insert(conversationEntity(createdAt = 2L, updatedAt = 300L))
            dao.insert(conversationEntity(createdAt = 3L, updatedAt = 200L))

            dao.observeAll().first().map { it.updatedAt } shouldBe listOf(300L, 200L, 100L)
        }

        test("rename changes only the title") { database ->
            val dao = database.conversationDao()
            val id = dao.insert(conversationEntity(title = "Old title", createdAt = 10L, updatedAt = 20L))

            dao.rename(id, "New title")
            val stored = dao.observeById(id).first()

            stored?.title shouldBe "New title"
            stored?.createdAt shouldBe 10L
            stored?.updatedAt shouldBe 20L
        }

        test("touch changes only updatedAt") { database ->
            val dao = database.conversationDao()
            val id = dao.insert(conversationEntity(title = "Title", createdAt = 10L, updatedAt = 20L))

            dao.touch(id, 99L)
            val stored = dao.observeById(id).first()

            stored?.title shouldBe "Title"
            stored?.createdAt shouldBe 10L
            stored?.updatedAt shouldBe 99L
        }

        test("delete cascades to messages and images of that conversation") { database ->
            val conversationDao = database.conversationDao()
            val messageDao = database.messageDao()
            val firstConversation = conversationDao.insert(conversationEntity())
            val secondConversation = conversationDao.insert(conversationEntity())
            val firstMessage = messageDao.insert(messageEntity(conversationId = firstConversation))
            val secondMessage = messageDao.insert(messageEntity(conversationId = secondConversation))
            messageDao.insertImages(
                listOf(
                    messageImageEntity(messageId = firstMessage, relativePath = "first.png"),
                    messageImageEntity(messageId = secondMessage, relativePath = "second.png")
                )
            )

            conversationDao.delete(firstConversation)

            messageDao.observeByConversation(firstConversation).first() shouldBe emptyList()
            conversationDao.getImagePathsOfConversation(firstConversation) shouldBe emptyList()
            val remaining = messageDao.observeByConversation(secondConversation).first()
            remaining.single().message.id shouldBe secondMessage
            remaining.single().images.map { it.relativePath } shouldBe listOf("second.png")
        }

        test("deleteEmpty removes conversations without messages") { database ->
            val conversationDao = database.conversationDao()
            val messageDao = database.messageDao()
            val emptyConversation = conversationDao.insert(conversationEntity())
            val nonEmptyConversation = conversationDao.insert(conversationEntity())
            messageDao.insert(messageEntity(conversationId = nonEmptyConversation))

            conversationDao.deleteEmpty()

            conversationDao.observeById(emptyConversation).first() shouldBe null
            conversationDao.observeById(nonEmptyConversation).first()?.id shouldBe nonEmptyConversation
        }

        test("clearAll removes all conversations and cascades to messages") { database ->
            val conversationDao = database.conversationDao()
            val messageDao = database.messageDao()
            val firstConversation = conversationDao.insert(conversationEntity())
            val secondConversation = conversationDao.insert(conversationEntity())
            messageDao.insert(messageEntity(conversationId = firstConversation))
            messageDao.insert(messageEntity(conversationId = secondConversation))

            conversationDao.clearAll()

            conversationDao.observeAll().first() shouldBe emptyList()
            messageDao.observeByConversation(firstConversation).first() shouldBe emptyList()
            messageDao.observeByConversation(secondConversation).first() shouldBe emptyList()
        }

        test("getImagePathsOfConversation returns only paths of that conversation") { database ->
            val conversationDao = database.conversationDao()
            val messageDao = database.messageDao()
            val firstConversation = conversationDao.insert(conversationEntity())
            val secondConversation = conversationDao.insert(conversationEntity())
            val firstMessage = messageDao.insert(messageEntity(conversationId = firstConversation))
            val secondMessage = messageDao.insert(messageEntity(conversationId = secondConversation))
            messageDao.insertImages(
                listOf(
                    messageImageEntity(messageId = firstMessage, relativePath = "first-a.png", position = 0),
                    messageImageEntity(messageId = firstMessage, relativePath = "first-b.png", position = 1),
                    messageImageEntity(messageId = secondMessage, relativePath = "second.png", position = 0)
                )
            )

            conversationDao.getImagePathsOfConversation(firstConversation).toSet() shouldBe
                setOf("first-a.png", "first-b.png")
        }

        test("getAllImagePaths returns every image path") { database ->
            val conversationDao = database.conversationDao()
            val messageDao = database.messageDao()
            val firstConversation = conversationDao.insert(conversationEntity())
            val secondConversation = conversationDao.insert(conversationEntity())
            val firstMessage = messageDao.insert(messageEntity(conversationId = firstConversation))
            val secondMessage = messageDao.insert(messageEntity(conversationId = secondConversation))
            messageDao.insertImages(
                listOf(
                    messageImageEntity(messageId = firstMessage, relativePath = "first-a.png", position = 0),
                    messageImageEntity(messageId = firstMessage, relativePath = "first-b.png", position = 1),
                    messageImageEntity(messageId = secondMessage, relativePath = "second.png", position = 0)
                )
            )

            conversationDao.getAllImagePaths().toSet() shouldBe
                setOf("first-a.png", "first-b.png", "second.png")
        }

        test("foreign keys reject a message for an unknown conversation") { database ->
            val messageDao = database.messageDao()

            shouldThrow<SQLiteException> {
                messageDao.insert(messageEntity(conversationId = 999L))
            }
        }
    }
}
