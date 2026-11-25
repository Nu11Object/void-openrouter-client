package com.nullo.voidapp.feature.catalog.presentation.store

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.nullo.voidapp.core.data.catalog.domain.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.domain.exception.NetworkException
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import com.nullo.voidapp.core.testing.chatModel
import com.nullo.voidapp.core.testing.imageModel
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.catalog.domain.ChatModelFilters
import com.nullo.voidapp.feature.catalog.domain.ImageModelFilters
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.Intent
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.Label
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.error_unknown
import voidapp.core.utils.generated.resources.exception_internal
import voidapp.core.utils.generated.resources.exception_network
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.message_catalog_refreshed
import voidapp.feature.catalog.generated.resources.message_using_model

private val chatModels = listOf(
    chatModel(id = "model-1", name = "Alpha"),
    chatModel(id = "model-2", name = "Beta"),
    chatModel(id = "beta-id", name = "Gamma")
)

private fun imagePricing(providerName: String = "Provider") = ImagePricing(
    providerName = providerName,
    providerSlug = providerName.lowercase(),
    lines = emptyList()
)

val CatalogStoreTest by testSuite {

    testSuite("bootstrap") {

        test("an empty repository triggers exactly one automatic refresh") {
            withCatalogStore {
                idle()

                catalogRepository.calls shouldBe listOf("refreshCatalog")
                store.state.isDbEmpty shouldBe true

                catalogRepository.changeChatModelsExternally(listOf(chatModel(id = "late-model")))
                idle()
                advanceBy(10.seconds)

                catalogRepository.calls shouldBe listOf("refreshCatalog")
                store.state.isDbEmpty shouldBe false
            }
        }

        test("a non-empty repository does not trigger a refresh and its models appear in State") {
            val initialChatModels = listOf(chatModel(id = "chat-1", name = "Chat One"))
            val initialImageModels = listOf(imageModel(id = "image-1", name = "Image One"))
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = initialChatModels,
                    initialImageModels = initialImageModels
                )
            ) {
                idle()

                catalogRepository.calls shouldBe emptyList()
                store.state.chatModels shouldBe initialChatModels
                store.state.imageModels shouldBe initialImageModels
                store.state.isDbEmpty shouldBe false
            }
        }

        test("automatic refresh failing with NetworkException shows the network error and is not retried") {
            val catalogRepository = FakeCatalogRepository()
            catalogRepository.refreshCatalogFailure = NetworkException()
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()
                advanceBy(10.seconds)

                store.state.error shouldBe UiText(CoreRes.string.exception_network)
                store.state.isLoading shouldBe false
                catalogRepository.calls shouldBe listOf("refreshCatalog")
            }
        }

        test("models changed in the repository later are reflected in State and isDbEmpty follows both lists") {
            val changes = listOf(
                ModelsChange(
                    chatModels = emptyList(),
                    imageModels = listOf(imageModel(id = "image-1")),
                    isDbEmpty = false
                ),
                ModelsChange(
                    chatModels = listOf(chatModel(id = "chat-1")),
                    imageModels = listOf(imageModel(id = "image-1")),
                    isDbEmpty = false
                ),
                ModelsChange(
                    chatModels = listOf(chatModel(id = "chat-1")),
                    imageModels = emptyList(),
                    isDbEmpty = false
                ),
                ModelsChange(
                    chatModels = emptyList(),
                    imageModels = emptyList(),
                    isDbEmpty = true
                )
            )
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel(id = "seed")))
            ) {
                idle()

                changes.forEach { change ->
                    catalogRepository.changeChatModelsExternally(change.chatModels)
                    catalogRepository.changeImageModelsExternally(change.imageModels)
                    idle()

                    store.state.chatModels shouldBe change.chatModels
                    store.state.imageModels shouldBe change.imageModels
                    store.state.isDbEmpty shouldBe change.isDbEmpty
                }
            }
        }
    }

    testSuite("search") {

        test("ChangeSearchQuery updates the query immediately and filters by name after 300 ms") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = chatModels)
            ) {
                idle()

                store.accept(Intent.ChangeSearchQuery("BETA"))
                idle()

                store.state.searchQuery shouldBe "BETA"

                advanceBy(299.milliseconds)

                store.state.chatModels.map { it.id } shouldBe listOf("model-1", "model-2", "beta-id")

                advanceBy(1.milliseconds)

                store.state.chatModels.map { it.id } shouldBe listOf("model-2")
            }
        }

        test("two queries typed 200 ms apart are debounced to the second query") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = chatModels)
            ) {
                idle()

                store.accept(Intent.ChangeSearchQuery("Alpha"))
                idle()
                advanceBy(200.milliseconds)

                store.accept(Intent.ChangeSearchQuery("Beta"))
                idle()
                advanceBy(299.milliseconds)

                store.state.searchQuery shouldBe "Beta"
                store.state.chatModels.map { it.id } shouldBe listOf("model-1", "model-2", "beta-id")

                advanceBy(1.milliseconds)

                store.state.chatModels.map { it.id } shouldBe listOf("model-2")
            }
        }

        test("ClearSearchQuery clears the query and restores the lists immediately") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = chatModels)
            ) {
                idle()

                store.accept(Intent.ChangeSearchQuery("Alpha"))
                idle()
                advanceBy(300.milliseconds)

                store.state.chatModels.map { it.id } shouldBe listOf("model-1")

                store.accept(Intent.ClearSearchQuery)
                idle()

                store.state.searchQuery shouldBe ""
                store.state.chatModels.map { it.id } shouldBe listOf("model-1", "model-2", "beta-id")
            }
        }

        test("search applies to image models too and combines with chat filters") {
            val initialChatModels = listOf(
                chatModel(id = "chat-pinned", name = "Alpha Pinned", pinned = true),
                chatModel(id = "chat-free", name = "Alpha Free"),
                chatModel(id = "chat-beta", name = "Beta", pinned = true)
            )
            val initialImageModels = listOf(
                imageModel(id = "image-alpha", name = "Alpha Image"),
                imageModel(id = "image-beta", name = "Beta Image")
            )
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = initialChatModels,
                    initialImageModels = initialImageModels
                )
            ) {
                idle()

                store.accept(Intent.ChangeSearchQuery("alpha"))
                store.accept(Intent.ChangeChatFilters(ChatModelFilters(pinnedOnly = true)))
                idle()
                advanceBy(300.milliseconds)

                store.state.chatModels.map { it.id } shouldBe listOf("chat-pinned")
                store.state.imageModels.map { it.id } shouldBe listOf("image-alpha")
            }
        }

        test("isDbEmpty stays false when the search matches nothing") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = chatModels)
            ) {
                idle()

                store.accept(Intent.ChangeSearchQuery("zzz"))
                idle()
                advanceBy(300.milliseconds)

                store.state.chatModels shouldBe emptyList()
                store.state.isDbEmpty shouldBe false
            }
        }
    }

    testSuite("filters") {

        test("ChangeChatFilters updates the chat filters and the chat list immediately") {
            val initialChatModels = listOf(
                chatModel(id = "chat-pinned", pinned = true),
                chatModel(id = "chat-free")
            )
            val initialImageModels = listOf(
                imageModel(id = "image-pinned", pinned = true),
                imageModel(id = "image-free")
            )
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = initialChatModels,
                    initialImageModels = initialImageModels
                )
            ) {
                idle()

                store.accept(Intent.ChangeChatFilters(ChatModelFilters(pinnedOnly = true)))
                idle()

                store.state.chatFilters shouldBe ChatModelFilters(pinnedOnly = true)
                store.state.chatModels.map { it.id } shouldBe listOf("chat-pinned")
                store.state.imageModels.map { it.id } shouldBe listOf("image-pinned", "image-free")
            }
        }

        test("ChangeImageFilters updates the image filters and the image list immediately") {
            val initialChatModels = listOf(
                chatModel(id = "chat-pinned", pinned = true),
                chatModel(id = "chat-free")
            )
            val initialImageModels = listOf(
                imageModel(id = "image-pinned", pinned = true),
                imageModel(id = "image-free")
            )
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = initialChatModels,
                    initialImageModels = initialImageModels
                )
            ) {
                idle()

                store.accept(Intent.ChangeImageFilters(ImageModelFilters(pinnedOnly = true)))
                idle()

                store.state.imageFilters shouldBe ImageModelFilters(pinnedOnly = true)
                store.state.imageModels.map { it.id } shouldBe listOf("image-pinned")
                store.state.chatModels.map { it.id } shouldBe listOf("chat-pinned", "chat-free")
            }
        }

        test("activeFiltersCount follows the selected tab") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = listOf(chatModel()),
                    initialImageModels = listOf(imageModel())
                )
            ) {
                idle()

                store.accept(Intent.ChangeChatFilters(ChatModelFilters(pinnedOnly = true)))
                store.accept(
                    Intent.ChangeImageFilters(
                        ImageModelFilters(
                            pinnedOnly = true,
                            requiredInputModalities = setOf(Modality.TEXT)
                        )
                    )
                )
                idle()

                listOf(
                    State.CatalogTab.ChatModels to 1,
                    State.CatalogTab.ImageModels to 2,
                    State.CatalogTab.ChatModels to 1
                ).forEach { (tab, expectedCount) ->
                    store.accept(Intent.ChangeCatalogTab(tab))
                    idle()

                    store.state.selectedTab shouldBe tab
                    store.state.activeFiltersCount shouldBe expectedCount
                }
            }
        }

        test("ResetFilters resets only the selected tab") {
            val initialChatModels = listOf(
                chatModel(id = "chat-pinned", pinned = true),
                chatModel(id = "chat-free")
            )
            val initialImageModels = listOf(
                imageModel(id = "image-pinned", pinned = true),
                imageModel(id = "image-free")
            )
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(
                    initialChatModels = initialChatModels,
                    initialImageModels = initialImageModels
                )
            ) {
                idle()

                store.accept(Intent.ChangeChatFilters(ChatModelFilters(pinnedOnly = true)))
                store.accept(Intent.ChangeImageFilters(ImageModelFilters(pinnedOnly = true)))
                idle()

                store.state.chatModels.map { it.id } shouldBe listOf("chat-pinned")
                store.state.imageModels.map { it.id } shouldBe listOf("image-pinned")

                store.accept(Intent.ResetFilters)
                idle()

                store.state.selectedTab shouldBe State.CatalogTab.ChatModels
                store.state.chatFilters shouldBe ChatModelFilters.Default
                store.state.chatModels.map { it.id } shouldBe listOf("chat-pinned", "chat-free")
                store.state.imageFilters shouldBe ImageModelFilters(pinnedOnly = true)
                store.state.imageModels.map { it.id } shouldBe listOf("image-pinned")

                store.accept(Intent.ChangeCatalogTab(State.CatalogTab.ImageModels))
                idle()
                store.accept(Intent.ResetFilters)
                idle()

                store.state.imageFilters shouldBe ImageModelFilters.Default
                store.state.imageModels.map { it.id } shouldBe listOf("image-pinned", "image-free")
                store.state.chatFilters shouldBe ChatModelFilters.Default
            }
        }

        test("ChangeCatalogTab and ToggleVerboseInfo update State") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            ) {
                idle()

                store.state.selectedTab shouldBe State.CatalogTab.ChatModels
                store.state.showVerboseInfo shouldBe true

                store.accept(Intent.ChangeCatalogTab(State.CatalogTab.ImageModels))
                store.accept(Intent.ToggleVerboseInfo(false))
                idle()

                store.state.selectedTab shouldBe State.CatalogTab.ImageModels
                store.state.showVerboseInfo shouldBe false
            }
        }
    }

    testSuite("refresh") {

        test("RefreshCatalog shows loading for the delay and a success message afterwards") {
            val catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            catalogRepository.refreshDelay = 1.seconds
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.isLoading shouldBe true
                store.state.message shouldBe null

                advanceBy(999.milliseconds)

                store.state.isLoading shouldBe true

                advanceBy(1.milliseconds)

                store.state.isLoading shouldBe false
                store.state.message shouldBe UiText(Res.string.message_catalog_refreshed)
            }
        }

        test("the image pricing map is cleared when the refresh finishes") {
            val model = imageModel(id = "image-1")
            val pricing = listOf(imagePricing())
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel()),
                initialImageModels = listOf(model),
                initialImagePricing = pricing
            )
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.LoadImagePricing(model))
                idle()

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loaded(pricing)

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.imagePricing shouldBe emptyMap()
                store.state.isLoading shouldBe false
            }
        }

        val refreshFailureCases = listOf(
            RefreshFailureCase(
                name = "InternalServerException",
                failure = InternalServerException(),
                expected = UiText(CoreRes.string.exception_internal)
            ),
            RefreshFailureCase(
                name = "NetworkException",
                failure = NetworkException(),
                expected = UiText(CoreRes.string.exception_network)
            ),
            RefreshFailureCase(
                name = "Exception with a message",
                failure = Exception("boom"),
                expected = UiText.Static("boom")
            ),
            RefreshFailureCase(
                name = "Exception without a message",
                failure = Exception(),
                expected = UiText(CoreRes.string.error_unknown)
            )
        )

        refreshFailureCases.forEach { case ->
            test("RefreshCatalog failure ${case.name} is shown as an error without a message") {
                val catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
                catalogRepository.refreshCatalogFailure = case.failure
                withCatalogStore(catalogRepository = catalogRepository) {
                    idle()

                    store.accept(Intent.RefreshCatalog)
                    idle()

                    store.state.error shouldBe case.expected
                    store.state.isLoading shouldBe false
                    store.state.message shouldBe null
                }
            }
        }

        test("DismissError clears the error") {
            val catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            catalogRepository.refreshCatalogFailure = NetworkException()
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.error shouldBe UiText(CoreRes.string.exception_network)

                store.accept(Intent.DismissError)
                idle()

                store.state.error shouldBe null
            }
        }

        test("two overlapping refreshes leave isLoading false when the first finishes") {
            val catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            catalogRepository.refreshDelay = 1.seconds
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.RefreshCatalog)
                idle()
                advanceBy(500.milliseconds)

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.isLoading shouldBe true
                catalogRepository.calls.size shouldBe 2

                advanceBy(500.milliseconds)

                store.state.isLoading shouldBe false
                catalogRepository.calls.size shouldBe 2
            }
        }
    }

    testSuite("pin") {

        val pinCases = listOf(
            PinCase("chat-1", ModelType.CHAT, true, "setPinStatus:chat-1:CHAT:true"),
            PinCase("chat-1", ModelType.CHAT, false, "setPinStatus:chat-1:CHAT:false"),
            PinCase("image-1", ModelType.IMAGE, true, "setPinStatus:image-1:IMAGE:true"),
            PinCase("image-1", ModelType.IMAGE, false, "setPinStatus:image-1:IMAGE:false")
        )

        pinCases.forEach { case ->
            test("SetPinState sends ${case.modelType} ${case.pinned} for ${case.modelId}") {
                withCatalogStore(
                    catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
                ) {
                    idle()

                    store.accept(Intent.SetPinState(case.modelId, case.modelType, case.pinned))
                    idle()

                    catalogRepository.calls shouldBe listOf(case.expectedCall)
                }
            }
        }

        test("a pin failure is shown as an error") {
            val catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            catalogRepository.setPinStatusFailure = Exception("x")
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.SetPinState("chat-1", ModelType.CHAT, true))
                idle()

                store.state.error shouldBe UiText.Static("x")
            }
        }
    }

    testSuite("useModel") {

        val useModelCases = listOf(
            UseModelCase(
                name = "Chat",
                ref = ActiveModelRef.Chat("m"),
                expectedMessage = UiText(Res.string.message_using_model, "m")
            ),
            UseModelCase(
                name = "Image",
                ref = ActiveModelRef.Image("m"),
                expectedMessage = UiText(Res.string.message_using_model, "m")
            )
        )

        useModelCases.forEach { case ->
            test("UseModel ${case.name} sets the active model and shows a message") {
                withCatalogStore(
                    catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
                ) {
                    idle()

                    store.accept(Intent.UseModel(case.ref))
                    idle()

                    sessionRepository.activeModel.first() shouldBe case.ref
                    store.state.message shouldBe case.expectedMessage
                }
            }
        }

        test("a use model failure is shown as an error without a using model message") {
            val sessionRepository = FakeSessionRepository()
            sessionRepository.setActiveModelFailure = Exception("x")
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel())),
                sessionRepository = sessionRepository
            ) {
                idle()

                store.accept(Intent.UseModel(ActiveModelRef.Chat("m")))
                idle()

                store.state.error shouldBe UiText.Static("x")
                store.state.message shouldBe null
                sessionRepository.calls shouldBe listOf("setActiveModel:m")
            }
        }

        test("a second UseModel replaces the message and restarts the countdown") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            ) {
                idle()

                store.accept(Intent.UseModel(ActiveModelRef.Chat("first")))
                idle()

                store.state.message shouldBe UiText(Res.string.message_using_model, "first")
                store.state.messageDismissalTimer shouldBe 5

                advanceBy(2.seconds)

                store.state.messageDismissalTimer shouldBe 3

                store.accept(Intent.UseModel(ActiveModelRef.Image("second")))
                idle()

                store.state.message shouldBe UiText(Res.string.message_using_model, "second")
                store.state.messageDismissalTimer shouldBe 5

                advanceBy(4.seconds)

                store.state.message shouldBe UiText(Res.string.message_using_model, "second")
                store.state.messageDismissalTimer shouldBe 1

                advanceBy(1.seconds)

                store.state.message shouldBe null
            }
        }
    }

    testSuite("messages") {

        test("a success message counts down from five and is dismissed after five seconds") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            ) {
                idle()

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.message shouldBe UiText(Res.string.message_catalog_refreshed)
                store.state.messageDismissalTimer shouldBe 5

                listOf(4, 3, 2, 1).forEach { expectedTimer ->
                    advanceBy(1.seconds)

                    store.state.message shouldBe UiText(Res.string.message_catalog_refreshed)
                    store.state.messageDismissalTimer shouldBe expectedTimer
                }

                advanceBy(1.seconds)

                store.state.message shouldBe null
                store.state.messageDismissalTimer shouldBe 1
            }
        }

        test("DismissMessage clears the message, stops the countdown and leaves the timer value") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            ) {
                idle()

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.messageDismissalTimer shouldBe 5

                store.accept(Intent.DismissMessage)
                idle()

                store.state.message shouldBe null
                store.state.messageDismissalTimer shouldBe 5

                advanceBy(5.seconds)

                store.state.message shouldBe null
                store.state.messageDismissalTimer shouldBe 5
            }
        }

        test("a new message replaces the previous one and restarts the countdown") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            ) {
                idle()

                store.accept(Intent.RefreshCatalog)
                idle()
                advanceBy(2.seconds)

                store.state.messageDismissalTimer shouldBe 3

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.message shouldBe UiText(Res.string.message_catalog_refreshed)
                store.state.messageDismissalTimer shouldBe 5

                advanceBy(4.seconds)

                store.state.message shouldBe UiText(Res.string.message_catalog_refreshed)
                store.state.messageDismissalTimer shouldBe 1

                advanceBy(1.seconds)

                store.state.message shouldBe null
            }
        }
    }

    testSuite("imagePricing") {

        test("LoadImagePricing shows Loading immediately and Loaded after the fetch delay") {
            val model = imageModel(id = "image-1")
            val pricing = listOf(imagePricing())
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel()),
                initialImageModels = listOf(model),
                initialImagePricing = pricing
            )
            catalogRepository.fetchImageModelPricingDelay = 1.seconds
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.LoadImagePricing(model))
                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loading
                idle()

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loading

                advanceBy(999.milliseconds)

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loading

                advanceBy(1.milliseconds)

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loaded(pricing)
            }
        }

        test("two LoadImagePricing intents for the same model while loading make exactly one call") {
            val model = imageModel(id = "image-1")
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel()),
                initialImageModels = listOf(model)
            )
            catalogRepository.fetchImageModelPricingDelay = 1.seconds
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.LoadImagePricing(model))
                store.accept(Intent.LoadImagePricing(model))
                idle()

                advanceBy(1.seconds)

                catalogRepository.calls shouldBe listOf("fetchImageModelPricing:${model.id}")
            }
        }

        test("different image models load independently") {
            val first = imageModel(id = "image-1")
            val second = imageModel(id = "image-2")
            val pricing = listOf(imagePricing())
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel()),
                initialImageModels = listOf(first, second),
                initialImagePricing = pricing
            )
            catalogRepository.fetchImageModelPricingDelay = 1.seconds
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.LoadImagePricing(first))
                store.accept(Intent.LoadImagePricing(second))
                idle()

                store.state.imagePricing[first.id] shouldBe State.ImagePricingState.Loading
                store.state.imagePricing[second.id] shouldBe State.ImagePricingState.Loading

                advanceBy(1.seconds)

                store.state.imagePricing[first.id] shouldBe State.ImagePricingState.Loaded(pricing)
                store.state.imagePricing[second.id] shouldBe State.ImagePricingState.Loaded(pricing)
            }
        }

        test("a failed pricing request can be retried") {
            val model = imageModel(id = "image-1")
            val pricing = listOf(imagePricing())
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel()),
                initialImageModels = listOf(model)
            )
            catalogRepository.fetchImageModelPricingDelay = 1.seconds
            catalogRepository.fetchImageModelPricingFailure = Exception("boom")
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.LoadImagePricing(model))
                idle()
                advanceBy(1.seconds)

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Failed

                catalogRepository.fetchImageModelPricingFailure = null
                catalogRepository.changeImagePricing(pricing)
                store.accept(Intent.LoadImagePricing(model))
                idle()

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loading

                advanceBy(1.seconds)

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loaded(pricing)
            }
        }

        test("pricing in flight when a refresh finishes is written after the map was cleared") {
            val model = imageModel(id = "image-1")
            val pricing = listOf(imagePricing())
            val catalogRepository = FakeCatalogRepository(
                initialChatModels = listOf(chatModel()),
                initialImageModels = listOf(model),
                initialImagePricing = pricing
            )
            catalogRepository.fetchImageModelPricingDelay = 1.seconds
            withCatalogStore(catalogRepository = catalogRepository) {
                idle()

                store.accept(Intent.LoadImagePricing(model))
                idle()

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loading

                store.accept(Intent.RefreshCatalog)
                idle()

                store.state.imagePricing shouldBe emptyMap()

                advanceBy(1.seconds)

                store.state.imagePricing[model.id] shouldBe State.ImagePricingState.Loaded(pricing)
            }
        }
    }

    testSuite("labels") {

        test("NavigateBack publishes NavigatedBack") {
            withCatalogStore(
                catalogRepository = FakeCatalogRepository(initialChatModels = listOf(chatModel()))
            ) {
                val labels = mutableListOf<Label>()
                store.labels.onEach { labels += it }.launchIn(backgroundScope)
                idle()

                store.accept(Intent.NavigateBack)
                idle()

                labels shouldBe listOf(Label.NavigatedBack)
            }
        }
    }
}

private data class ModelsChange(
    val chatModels: List<ChatModel>,
    val imageModels: List<ImageModel>,
    val isDbEmpty: Boolean,
)

private data class RefreshFailureCase(
    val name: String,
    val failure: Exception,
    val expected: UiText,
)

private data class PinCase(
    val modelId: String,
    val modelType: ModelType,
    val pinned: Boolean,
    val expectedCall: String,
)

private data class UseModelCase(
    val name: String,
    val ref: ActiveModelRef,
    val expectedMessage: UiText,
)
