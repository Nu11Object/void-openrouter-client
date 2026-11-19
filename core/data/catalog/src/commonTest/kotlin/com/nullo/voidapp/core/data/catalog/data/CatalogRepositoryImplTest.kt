package com.nullo.voidapp.core.data.catalog.data

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException
import com.nullo.voidapp.core.database.mapper.toEntity
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import com.nullo.voidapp.core.model.imagemodel.PricingLine
import com.nullo.voidapp.core.model.imagemodel.PricingUnit
import com.nullo.voidapp.core.testing.chatModel
import com.nullo.voidapp.core.testing.imageModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private fun chat(
    id: String,
    name: String = id,
    pinned: Boolean = false,
) = chatModel(id = id, name = name, pinned = pinned)

private fun image(
    id: String,
    name: String = id,
    endpointsUrl: String = "images/models/$id/endpoints",
    pinned: Boolean = false,
) = imageModel(id = id, name = name, endpointsUrl = endpointsUrl, pinned = pinned)

private fun chatEntity(
    id: String,
    name: String = id,
    pinned: Boolean = false,
) = chat(id = id, name = name, pinned = pinned).toEntity()

private fun imageEntity(
    id: String,
    name: String = id,
    endpointsUrl: String = "images/models/$id/endpoints",
    pinned: Boolean = false,
) = image(id = id, name = name, endpointsUrl = endpointsUrl, pinned = pinned).toEntity()

private class CatalogRepositoryEnv {
    val chatDao = FakeChatModelDao()
    val imageDao = FakeImageModelDao()
    val api = FakeCatalogApiService()
    val repository = CatalogRepositoryImpl(chatDao, imageDao, api)
}

val CatalogRepositoryImplTest by testSuite {
    testFixture { CatalogRepositoryEnv() } asParameterForEach {
        testSuite("refreshCatalog") {
            test("stores the fetched chat and image models") { env ->
                env.api.chatResult = {
                    listOf(chat("acme/chat-a", "Chat A"), chat("acme/chat-b", "Chat B"))
                }
                env.api.imageResult = {
                    listOf(image("acme/image-a", "Image A"), image("acme/image-b", "Image B"))
                }

                env.repository.refreshCatalog()

                val chatModels = env.repository.observeChatModels().first()
                chatModels.map { it.id } shouldBe listOf("acme/chat-a", "acme/chat-b")
                chatModels.map { it.name } shouldBe listOf("Chat A", "Chat B")
                val imageModels = env.repository.observeImageModels().first()
                imageModels.map { it.id } shouldBe listOf("acme/image-a", "acme/image-b")
                imageModels.map { it.name } shouldBe listOf("Image A", "Image B")
                env.api.chatCalls shouldBe 1
                env.api.imageCalls shouldBe 1
            }

            test("a pinned model stays pinned while its fields come from the response") { env ->
                env.chatDao.insertAll(listOf(chatEntity("acme/chat-a", "Old Chat", pinned = true)))
                env.imageDao.insertAll(listOf(imageEntity("acme/image-a", "Old Image", pinned = true)))
                env.api.chatResult = {
                    listOf(chat("acme/chat-a", "New Chat"), chat("acme/chat-new", "New Chat Model"))
                }
                env.api.imageResult = {
                    listOf(image("acme/image-a", "New Image"), image("acme/image-new", "New Image Model"))
                }

                env.repository.refreshCatalog()

                val chatModels = env.repository.observeChatModels().first()
                chatModels.map { it.id } shouldBe listOf("acme/chat-a", "acme/chat-new")
                chatModels[0].name shouldBe "New Chat"
                chatModels[0].pinned shouldBe true
                chatModels[1].pinned shouldBe false
                val imageModels = env.repository.observeImageModels().first()
                imageModels.map { it.id } shouldBe listOf("acme/image-a", "acme/image-new")
                imageModels[0].name shouldBe "New Image"
                imageModels[0].pinned shouldBe true
                imageModels[1].pinned shouldBe false
            }

            test("models missing from the response are removed including pinned ones") { env ->
                env.chatDao.insertAll(
                    listOf(chatEntity("old/chat-pinned", pinned = true), chatEntity("old/chat-plain"))
                )
                env.imageDao.insertAll(
                    listOf(imageEntity("old/image-pinned", pinned = true), imageEntity("old/image-plain"))
                )
                env.api.chatResult = { listOf(chat("new/chat")) }
                env.api.imageResult = { listOf(image("new/image")) }

                env.repository.refreshCatalog()

                env.repository.observeChatModels().first().map { it.id } shouldBe listOf("new/chat")
                env.repository.observePinnedChatModels().first() shouldBe emptyList()
                env.chatDao.getPinnedIds() shouldBe emptyList()
                env.repository.observeImageModels().first().map { it.id } shouldBe listOf("new/image")
                env.repository.observePinnedImageModels().first() shouldBe emptyList()
                env.imageDao.getPinnedIds() shouldBe emptyList()
            }

            test("a NetworkException from fetchChatModels propagates and leaves both DAOs unchanged") { env ->
                env.chatDao.insertAll(listOf(chatEntity("old/chat")))
                env.imageDao.insertAll(listOf(imageEntity("old/image")))
                val error = NetworkException()
                env.api.chatResult = { throw error }
                env.api.imageResult = { listOf(image("new/image")) }

                shouldThrow<NetworkException> { env.repository.refreshCatalog() }

                env.repository.observeChatModels().first().map { it.id } shouldBe listOf("old/chat")
                env.repository.observeImageModels().first().map { it.id } shouldBe listOf("old/image")
            }

            test(
                "a NetworkException from fetchImageModels after a successful chat fetch leaves both DAOs unchanged"
            ) { env ->
                env.chatDao.insertAll(listOf(chatEntity("old/chat")))
                env.imageDao.insertAll(listOf(imageEntity("old/image")))
                val error = NetworkException()
                env.api.chatResult = { listOf(chat("new/chat")) }
                env.api.imageResult = {
                    delay(100.milliseconds)
                    throw error
                }

                shouldThrow<NetworkException> { env.repository.refreshCatalog() }

                env.repository.observeChatModels().first().map { it.id } shouldBe listOf("old/chat")
                env.repository.observeImageModels().first().map { it.id } shouldBe listOf("old/image")
            }

            test(
                "a NetworkException from fetchChatModels after a successful image fetch leaves both DAOs unchanged"
            ) { env ->
                env.chatDao.insertAll(listOf(chatEntity("old/chat")))
                env.imageDao.insertAll(listOf(imageEntity("old/image")))
                val error = NetworkException()
                env.api.chatResult = {
                    delay(100.milliseconds)
                    throw error
                }
                env.api.imageResult = { listOf(image("new/image")) }

                shouldThrow<NetworkException> { env.repository.refreshCatalog() }

                env.repository.observeChatModels().first().map { it.id } shouldBe listOf("old/chat")
                env.repository.observeImageModels().first().map { it.id } shouldBe listOf("old/image")
            }

            test("an empty chat response keeps the existing chat models") { env ->
                env.chatDao.insertAll(
                    listOf(
                        chatEntity("old/chat-a", "Old Chat A"),
                        chatEntity("old/chat-b", "Old Chat B", pinned = true)
                    )
                )
                env.api.chatResult = { emptyList() }
                env.api.imageResult = { listOf(image("new/image")) }

                env.repository.refreshCatalog()

                val chatModels = env.repository.observeChatModels().first()
                chatModels.map { it.id } shouldBe listOf("old/chat-a", "old/chat-b")
                chatModels.map { it.name } shouldBe listOf("Old Chat A", "Old Chat B")
                chatModels[1].pinned shouldBe true
                env.repository.observeImageModels().first().map { it.id } shouldBe listOf("new/image")
            }

            test("an empty image response keeps the existing image models") { env ->
                env.imageDao.insertAll(
                    listOf(
                        imageEntity("old/image-a", "Old Image A"),
                        imageEntity("old/image-b", "Old Image B", pinned = true)
                    )
                )
                env.api.chatResult = { listOf(chat("new/chat")) }
                env.api.imageResult = { emptyList() }

                env.repository.refreshCatalog()

                val imageModels = env.repository.observeImageModels().first()
                imageModels.map { it.id } shouldBe listOf("old/image-a", "old/image-b")
                imageModels.map { it.name } shouldBe listOf("Old Image A", "Old Image B")
                imageModels[1].pinned shouldBe true
                env.repository.observeChatModels().first().map { it.id } shouldBe listOf("new/chat")
            }

            test("empty chat and image responses change nothing") { env ->
                env.chatDao.insertAll(listOf(chatEntity("old/chat")))
                env.imageDao.insertAll(listOf(imageEntity("old/image")))
                env.api.chatResult = { emptyList() }
                env.api.imageResult = { emptyList() }

                env.repository.refreshCatalog()

                env.repository.observeChatModels().first().map { it.id } shouldBe listOf("old/chat")
                env.repository.observeImageModels().first().map { it.id } shouldBe listOf("old/image")
            }

            test("a CancellationException from the API service is rethrown unchanged") { env ->
                val cancellation = CancellationException("cancelled")
                env.api.chatResult = { throw cancellation }
                env.api.imageResult = { listOf(image("new/image")) }

                shouldThrow<CancellationException> { env.repository.refreshCatalog() } shouldBe cancellation
            }
        }

        testSuite("observeChatModels") {
            test("maps entities to domain models in order") { env ->
                env.chatDao.insertAll(
                    listOf(chatEntity("b/chat", "Chat B"), chatEntity("a/chat", "Chat A"))
                )

                val models = env.repository.observeChatModels().first()

                models.map { it.id } shouldBe listOf("b/chat", "a/chat")
                models.map { it.name } shouldBe listOf("Chat B", "Chat A")
            }
        }

        testSuite("observeImageModels") {
            test("maps entities to domain models in order") { env ->
                env.imageDao.insertAll(
                    listOf(imageEntity("b/image", "Image B"), imageEntity("a/image", "Image A"))
                )

                val models = env.repository.observeImageModels().first()

                models.map { it.id } shouldBe listOf("b/image", "a/image")
                models.map { it.name } shouldBe listOf("Image B", "Image A")
            }
        }

        testSuite("observeChatModelById") {
            test("returns null for an unknown id") { env ->
                env.chatDao.insertAll(listOf(chatEntity("acme/chat")))

                val model = env.repository.observeChatModelById("missing/chat").first()

                model shouldBe null
            }

            test("returns the model for a known id") { env ->
                env.chatDao.insertAll(
                    listOf(chatEntity("acme/chat-a", "Chat A"), chatEntity("acme/chat-b", "Chat B"))
                )

                val model = env.repository.observeChatModelById("acme/chat-b").first()

                model?.id shouldBe "acme/chat-b"
                model?.name shouldBe "Chat B"
            }
        }

        testSuite("observeImageModelById") {
            test("returns null for an unknown id") { env ->
                env.imageDao.insertAll(listOf(imageEntity("acme/image")))

                val model = env.repository.observeImageModelById("missing/image").first()

                model shouldBe null
            }

            test("returns the model for a known id") { env ->
                env.imageDao.insertAll(
                    listOf(imageEntity("acme/image-a", "Image A"), imageEntity("acme/image-b", "Image B"))
                )

                val model = env.repository.observeImageModelById("acme/image-b").first()

                model?.id shouldBe "acme/image-b"
                model?.name shouldBe "Image B"
            }
        }

        testSuite("observePinnedChatModels") {
            test("returns only pinned chat models") { env ->
                env.chatDao.insertAll(
                    listOf(chatEntity("pinned/chat", "Pinned Chat", pinned = true), chatEntity("plain/chat"))
                )

                val models = env.repository.observePinnedChatModels().first()

                models.map { it.id } shouldBe listOf("pinned/chat")
            }
        }

        testSuite("observePinnedImageModels") {
            test("returns only pinned image models") { env ->
                env.imageDao.insertAll(
                    listOf(imageEntity("pinned/image", "Pinned Image", pinned = true), imageEntity("plain/image"))
                )

                val models = env.repository.observePinnedImageModels().first()

                models.map { it.id } shouldBe listOf("pinned/image")
            }
        }

        testSuite("setPinStatus") {
            test("pins and unpins a chat model in the chat DAO only") { env ->
                env.chatDao.insertAll(listOf(chatEntity("acme/chat")))
                env.imageDao.insertAll(listOf(imageEntity("acme/image")))

                env.repository.setPinStatus("acme/chat", ModelType.CHAT, true)

                env.chatDao.observeById("acme/chat").first()?.pinned shouldBe true
                env.imageDao.observeById("acme/image").first()?.pinned shouldBe false

                env.repository.setPinStatus("acme/chat", ModelType.CHAT, false)

                env.chatDao.observeById("acme/chat").first()?.pinned shouldBe false
            }

            test("pins and unpins an image model in the image DAO only") { env ->
                env.chatDao.insertAll(listOf(chatEntity("acme/chat")))
                env.imageDao.insertAll(listOf(imageEntity("acme/image")))

                env.repository.setPinStatus("acme/image", ModelType.IMAGE, true)

                env.imageDao.observeById("acme/image").first()?.pinned shouldBe true
                env.chatDao.observeById("acme/chat").first()?.pinned shouldBe false

                env.repository.setPinStatus("acme/image", ModelType.IMAGE, false)

                env.imageDao.observeById("acme/image").first()?.pinned shouldBe false
            }

            test("chat and image models with the same id keep independent pin status") { env ->
                env.chatDao.insertAll(listOf(chatEntity("shared/model")))
                env.imageDao.insertAll(listOf(imageEntity("shared/model")))

                env.repository.setPinStatus("shared/model", ModelType.CHAT, true)
                env.repository.setPinStatus("shared/model", ModelType.IMAGE, true)
                env.repository.setPinStatus("shared/model", ModelType.CHAT, false)

                env.chatDao.observeById("shared/model").first()?.pinned shouldBe false
                env.imageDao.observeById("shared/model").first()?.pinned shouldBe true
            }
        }

        testSuite("fetchImageModelPricing") {
            test("passes the model endpoints URL and returns the API result unchanged") { env ->
                val pricing = listOf(
                    ImagePricing(
                        providerName = "Acme",
                        providerSlug = "acme",
                        lines = listOf(
                            PricingLine(
                                billable = "image",
                                unit = PricingUnit.IMAGE,
                                costUsd = "0.04".toBigDecimal(),
                                variant = null
                            )
                        )
                    )
                )
                env.api.pricingResult = { pricing }
                val model = image(id = "acme/model-x", endpointsUrl = "images/models/acme/model-x/endpoints")

                val result = env.repository.fetchImageModelPricing(model)

                result shouldBe pricing
                env.api.pricingUrls shouldBe listOf("images/models/acme/model-x/endpoints")
            }
        }

        testSuite("deleteAll") {
            test("clears both DAOs") { env ->
                env.chatDao.insertAll(listOf(chatEntity("acme/chat")))
                env.imageDao.insertAll(listOf(imageEntity("acme/image")))

                env.repository.deleteAll()

                env.repository.observeChatModels().first() shouldBe emptyList()
                env.repository.observeImageModels().first() shouldBe emptyList()
            }
        }
    }
}
