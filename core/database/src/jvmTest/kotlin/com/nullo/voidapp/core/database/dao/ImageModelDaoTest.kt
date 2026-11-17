package com.nullo.voidapp.core.database.dao

import com.nullo.voidapp.core.database.buildTestDatabase
import com.nullo.voidapp.core.database.mapper.toEntity
import com.nullo.voidapp.core.testing.imageModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

val ImageModelDaoTest by testSuite {
    testFixture { buildTestDatabase() } closeWith { close() } asParameterForEach {
        test("insertAll stores all models") { database ->
            val dao = database.imageModelDao()

            dao.insertAll(
                listOf(
                    imageModel(id = "model/a").toEntity(),
                    imageModel(id = "model/b").toEntity()
                )
            )

            dao.count() shouldBe 2
        }

        test("insertAll replaces a model with the same id") { database ->
            val dao = database.imageModelDao()

            dao.insertAll(listOf(imageModel(id = "model/a").copy(name = "First").toEntity()))
            dao.insertAll(listOf(imageModel(id = "model/a").copy(name = "Second").toEntity()))

            dao.observeById("model/a").first()?.name shouldBe "Second"
            dao.count() shouldBe 1
        }

        test("setPinned adds and removes a model from observePinned") { database ->
            val dao = database.imageModelDao()
            dao.insertAll(listOf(imageModel(id = "model/a").toEntity()))

            dao.setPinned("model/a", true)
            dao.observePinned().first().map { it.id } shouldBe listOf("model/a")

            dao.setPinned("model/a", false)
            dao.observePinned().first() shouldBe emptyList()
        }

        test("observeById returns null for an unknown id and the model for a known id") { database ->
            val dao = database.imageModelDao()

            dao.observeById("missing").first() shouldBe null

            dao.insertAll(listOf(imageModel(id = "model/a").copy(name = "Known").toEntity()))
            val stored = dao.observeById("model/a").first()

            stored?.id shouldBe "model/a"
            stored?.name shouldBe "Known"
        }

        test("replaceAll keeps pinned for ids that are pinned in the database") { database ->
            val dao = database.imageModelDao()
            dao.insertAll(listOf(imageModel(id = "model/a", pinned = true).toEntity()))

            dao.replaceAll(listOf(imageModel(id = "model/a", pinned = false).toEntity()))

            dao.observeById("model/a").first()?.pinned shouldBe true
        }

        test("replaceAll removes models missing from the new list including pinned ones") { database ->
            val dao = database.imageModelDao()
            dao.insertAll(
                listOf(
                    imageModel(id = "model/pinned", pinned = true).toEntity(),
                    imageModel(id = "model/kept").toEntity()
                )
            )

            dao.replaceAll(listOf(imageModel(id = "model/kept").toEntity()))

            dao.observeById("model/pinned").first() shouldBe null
            dao.count() shouldBe 1
        }

        test("replaceAll loses pin when a removed model returns") { database ->
            val dao = database.imageModelDao()
            dao.insertAll(listOf(imageModel(id = "model/a", pinned = true).toEntity()))

            dao.replaceAll(emptyList())
            dao.replaceAll(listOf(imageModel(id = "model/a", pinned = false).toEntity()))

            dao.observeById("model/a").first()?.pinned shouldBe false
        }

        test("replaceAll adds new models as unpinned") { database ->
            val dao = database.imageModelDao()

            dao.replaceAll(listOf(imageModel(id = "model/new").toEntity()))

            dao.observeById("model/new").first()?.pinned shouldBe false
        }

        test("replaceAll with an empty list clears the table") { database ->
            val dao = database.imageModelDao()
            dao.insertAll(listOf(imageModel(id = "model/a").toEntity()))

            dao.replaceAll(emptyList())

            dao.count() shouldBe 0
        }

        test("replaceAll takes other fields from the incoming pinned model") { database ->
            val dao = database.imageModelDao()
            dao.insertAll(listOf(imageModel(id = "model/a", pinned = true).copy(name = "Old").toEntity()))

            dao.replaceAll(listOf(imageModel(id = "model/a", pinned = false).copy(name = "New").toEntity()))
            val stored = dao.observeById("model/a").first()

            stored?.name shouldBe "New"
            stored?.pinned shouldBe true
        }
    }
}
