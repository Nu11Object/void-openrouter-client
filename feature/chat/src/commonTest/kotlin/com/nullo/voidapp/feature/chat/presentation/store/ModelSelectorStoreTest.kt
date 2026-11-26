package com.nullo.voidapp.feature.chat.presentation.store

import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.testing.chatModel
import com.nullo.voidapp.core.testing.imageModel
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.ModelSelectorStore.State.SelectorTab
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.first

private fun pinnedCatalogRepository() = FakeCatalogRepository(
    initialChatModels = listOf(
        chatModel(id = "chat-alpha", name = "Alpha"),
        chatModel(id = "chat-beta", name = "Beta")
    ),
    initialImageModels = listOf(
        imageModel(id = "image-alpha", name = "Alpha image"),
        imageModel(id = "image-beta", name = "Beta image")
    ),
    initialPinnedChatModelIds = setOf("chat-alpha", "chat-beta"),
    initialPinnedImageModelIds = setOf("image-alpha", "image-beta")
)

private data class SelectCase(
    val name: String,
    val ref: ActiveModelRef,
    val tab: SelectorTab,
)

private data class UnpinCase(
    val name: String,
    val modelId: String,
    val modelType: ModelType,
    val expectedChatIds: List<String>,
    val expectedImageIds: List<String>,
)

val ModelSelectorStoreTest by testSuite {

    testSuite("observe") {

        test("pinned chat and image models appear in State") {
            withModelSelectorStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = listOf(
                        chatModel(id = "chat-pinned"),
                        chatModel(id = "chat-free")
                    ),
                    initialImageModels = listOf(
                        imageModel(id = "image-pinned"),
                        imageModel(id = "image-free")
                    ),
                    initialPinnedChatModelIds = setOf("chat-pinned"),
                    initialPinnedImageModelIds = setOf("image-pinned")
                )
            ) {
                idle()

                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-pinned")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-pinned")
            }
        }

        test("pinning and unpinning in the fake repository is reflected") {
            withModelSelectorStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = listOf(chatModel(id = "chat-model")),
                    initialImageModels = listOf(imageModel(id = "image-model"))
                )
            ) {
                idle()

                catalogRepository.setPinStatus("chat-model", ModelType.CHAT, pinned = true)
                catalogRepository.setPinStatus("image-model", ModelType.IMAGE, pinned = true)
                idle()

                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-model")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-model")

                catalogRepository.setPinStatus("chat-model", ModelType.CHAT, pinned = false)
                catalogRepository.setPinStatus("image-model", ModelType.IMAGE, pinned = false)
                idle()

                store.state.pinnedChatModels shouldBe emptyList()
                store.state.pinnedImageModels shouldBe emptyList()
            }
        }
    }

    testSuite("visibility") {

        test("OpenModelSelector and CloseModelSelector toggle the visibility state") {
            withModelSelectorStore {
                idle()

                store.state.isModelSelectorOpen shouldBe false

                store.accept(Intent.OpenModelSelector)
                idle()

                store.state.isModelSelectorOpen shouldBe true

                store.accept(Intent.CloseModelSelector)
                idle()

                store.state.isModelSelectorOpen shouldBe false
            }
        }

        test("ChangeModelSelectorTab updates the tab") {
            withModelSelectorStore {
                idle()

                store.state.modelSelectorTab shouldBe SelectorTab.ChatModels

                store.accept(Intent.ChangeModelSelectorTab(SelectorTab.ImageModels))
                idle()

                store.state.modelSelectorTab shouldBe SelectorTab.ImageModels
            }
        }
    }

    testSuite("search") {

        test("ChangeModelSearchQuery updates the query immediately and filters both lists after 300 ms") {
            withModelSelectorStore(catalogRepository = pinnedCatalogRepository()) {
                idle()

                store.accept(Intent.ChangeModelSearchQuery("ALPHA"))
                idle()

                store.state.modelSearchQuery shouldBe "ALPHA"
                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-alpha", "chat-beta")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-alpha", "image-beta")

                advanceBy(299.milliseconds)

                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-alpha", "chat-beta")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-alpha", "image-beta")

                advanceBy(1.milliseconds)

                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-alpha")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-alpha")
            }
        }

        test("an empty query restores both lists immediately") {
            withModelSelectorStore(catalogRepository = pinnedCatalogRepository()) {
                idle()

                store.accept(Intent.ChangeModelSearchQuery("Alpha"))
                idle()
                advanceBy(300.milliseconds)

                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-alpha")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-alpha")

                store.accept(Intent.ChangeModelSearchQuery(""))
                idle()

                store.state.modelSearchQuery shouldBe ""
                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-alpha", "chat-beta")
                store.state.pinnedImageModels.map { it.id } shouldBe listOf("image-alpha", "image-beta")
            }
        }
    }

    testSuite("selectModel") {

        val selectCases = listOf(
            SelectCase(
                name = "Chat",
                ref = ActiveModelRef.Chat("chat-model"),
                tab = SelectorTab.ImageModels
            ),
            SelectCase(
                name = "Image",
                ref = ActiveModelRef.Image("image-model"),
                tab = SelectorTab.ChatModels
            )
        )

        selectCases.forEach { case ->
            test("SelectModel ${case.name} sets the active model without changing the selector") {
                withModelSelectorStore {
                    idle()

                    store.accept(Intent.OpenModelSelector)
                    store.accept(Intent.ChangeModelSelectorTab(case.tab))
                    idle()

                    store.accept(Intent.SelectModel(case.ref))
                    idle()

                    sessionRepository.activeModel.first() shouldBe case.ref
                    store.state.isModelSelectorOpen shouldBe true
                    store.state.modelSelectorTab shouldBe case.tab
                }
            }
        }
    }

    testSuite("unpinModel") {

        val unpinCases = listOf(
            UnpinCase(
                name = "Chat",
                modelId = "chat-model",
                modelType = ModelType.CHAT,
                expectedChatIds = emptyList(),
                expectedImageIds = listOf("image-model")
            ),
            UnpinCase(
                name = "Image",
                modelId = "image-model",
                modelType = ModelType.IMAGE,
                expectedChatIds = listOf("chat-model"),
                expectedImageIds = emptyList()
            )
        )

        unpinCases.forEach { case ->
            test("UnpinModel ${case.name} calls the repository and removes the model from the pinned list") {
                withModelSelectorStore(
                    catalogRepository = FakeCatalogRepository(
                        initialChatModels = listOf(chatModel(id = "chat-model")),
                        initialImageModels = listOf(imageModel(id = "image-model")),
                        initialPinnedChatModelIds = setOf("chat-model"),
                        initialPinnedImageModelIds = setOf("image-model")
                    )
                ) {
                    idle()

                    store.accept(Intent.UnpinModel(case.modelId, case.modelType))
                    idle()

                    catalogRepository.calls shouldBe listOf(
                        "setPinStatus:${case.modelId}:${case.modelType.name}:false"
                    )
                    store.state.pinnedChatModels.map { it.id } shouldBe case.expectedChatIds
                    store.state.pinnedImageModels.map { it.id } shouldBe case.expectedImageIds
                }
            }
        }
    }

    testSuite("failure") {

        test("a SelectModel failure leaves the State unchanged") {
            val sessionRepository = FakeSessionRepository()
            sessionRepository.setActiveModelFailure = Exception("x")
            withModelSelectorStore(sessionRepository = sessionRepository) {
                idle()

                store.accept(Intent.OpenModelSelector)
                store.accept(Intent.ChangeModelSelectorTab(SelectorTab.ImageModels))
                idle()

                val stateBefore = store.state

                store.accept(Intent.SelectModel(ActiveModelRef.Chat("chat-model")))
                idle()

                store.state shouldBe stateBefore
                sessionRepository.activeModel.first() shouldBe null
            }
        }

        test("an UnpinModel failure leaves the State unchanged") {
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel(id = "chat-model")),
                initialPinnedChatModelIds = setOf("chat-model")
            )
            catalogRepository.setPinStatusFailure = Exception("x")
            withModelSelectorStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.OpenModelSelector)
                idle()

                val stateBefore = store.state

                store.accept(Intent.UnpinModel("chat-model", ModelType.CHAT))
                idle()

                store.state shouldBe stateBefore
                store.state.pinnedChatModels.map { it.id } shouldBe listOf("chat-model")
            }
        }
    }
}
