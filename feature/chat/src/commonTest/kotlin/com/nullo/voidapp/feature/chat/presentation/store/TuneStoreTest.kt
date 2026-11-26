package com.nullo.voidapp.feature.chat.presentation.store

import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import com.nullo.voidapp.core.testing.chatModel
import com.nullo.voidapp.core.testing.imageModel
import com.nullo.voidapp.feature.chat.domain.tune.ChatTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.ImageTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.TuneInput
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore.State
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore.State.Content
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first

private val chatTuneModel = chatModel(
    id = "chat-model",
    name = "Chat Model",
    supportedTuningParameters = setOf(
        TuningParameter.TEMPERATURE,
        TuningParameter.TOP_K,
        TuningParameter.MAX_TOKENS,
        TuningParameter.SEED
    ),
    maxCompletionTokens = 1000
)

private val imageTuneModel = imageModel(
    id = "image-model",
    name = "Image Model",
    supportedParameters = mapOf(
        ImageParameter.RESOLUTION to ImageParameterCapability.Options(
            setOf("1024x1024", "512x512")
        ),
        ImageParameter.SIZE to ImageParameterCapability.Range(256.0, 1024.0),
        ImageParameter.QUALITY to ImageParameterCapability.Flag,
        ImageParameter.OUTPUT_COMPRESSION to ImageParameterCapability.Range(0.0, 100.0),
        ImageParameter.N to ImageParameterCapability.Range(1.0, 4.0),
        ImageParameter.SEED to ImageParameterCapability.Range(0.0, 5_000_000_000.0)
    )
)

private fun chatCatalog(model: ChatModel = chatTuneModel) =
    FakeCatalogRepository(initialChatModels = listOf(model))

private fun chatSession(model: ChatModel = chatTuneModel) =
    FakeSessionRepository(initialActiveModel = ActiveModelRef.Chat(model.id))

private fun imageCatalog(model: ImageModel = imageTuneModel) =
    FakeCatalogRepository(initialImageModels = listOf(model))

private fun imageSession(model: ImageModel = imageTuneModel) =
    FakeSessionRepository(initialActiveModel = ActiveModelRef.Image(model.id))

private fun State.chatItem(parameter: TuningParameter): ChatTuneItem =
    content.shouldBeInstanceOf<Content.Chat>().items.single { it.spec.parameter == parameter }

private fun State.imageItem(parameter: ImageParameter): ImageTuneItem =
    content.shouldBeInstanceOf<Content.Image>().items.single { it.parameter == parameter }

private fun State.imageNumeric(parameter: ImageParameter): ImageTuneItem.Numeric =
    imageItem(parameter).shouldBeInstanceOf()

private fun State.imageChoice(parameter: ImageParameter): ImageTuneItem.Choice =
    imageItem(parameter).shouldBeInstanceOf()

private data class MaxTokensCase(
    val name: String,
    val model: ChatModel,
    val expectedLimit: Int?,
)

private data class ImageSliderCase(
    val parameter: ImageParameter,
    val value: Double,
    val expectedRaw: String,
)

private val maxTokensCases = listOf(
    MaxTokensCase(
        name = "maxTokensLimit is 1000 when maxCompletionTokens is 1000",
        model = chatModel(
            id = "limit-chat",
            supportedTuningParameters = setOf(TuningParameter.TEMPERATURE),
            maxCompletionTokens = 1000
        ),
        expectedLimit = 1000
    ),
    MaxTokensCase(
        name = "maxTokensLimit is null when maxCompletionTokens is 0",
        model = chatModel(
            id = "limit-chat",
            supportedTuningParameters = setOf(TuningParameter.TEMPERATURE),
            maxCompletionTokens = 0,
            contextLength = 5000
        ),
        expectedLimit = null
    ),
    MaxTokensCase(
        name = "maxTokensLimit is null when maxCompletionTokens is null",
        model = chatModel(
            id = "limit-chat",
            supportedTuningParameters = setOf(TuningParameter.TEMPERATURE),
            maxCompletionTokens = null,
            contextLength = 5000
        ),
        expectedLimit = null
    )
)

private val imageSliderCases = listOf(
    ImageSliderCase(ImageParameter.OUTPUT_COMPRESSION, 50.6, "51"),
    ImageSliderCase(ImageParameter.SIZE, 512.6, "513")
)

val TuneStoreTest by testSuite {

    testSuite("bootstrap") {

        test("no active model shows NoModel without a name") {
            withTuneStore {
                store.state.content shouldBe Content.NoModel
                store.state.modelName shouldBe null
            }
        }

        test("an active chat model exposes its tune specs and name") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.state.modelName shouldBe "Chat Model"
                store.state.content.shouldBeInstanceOf<Content.Chat>().items
                    .map { it.spec.parameter } shouldBe listOf(
                    TuningParameter.TEMPERATURE,
                    TuningParameter.TOP_K,
                    TuningParameter.MAX_TOKENS,
                    TuningParameter.SEED
                )
            }
        }

        test("an active model missing from the catalog shows NoModel") {
            val sessionRepository = FakeSessionRepository(
                initialActiveModel = ActiveModelRef.Chat("missing-chat")
            )
            withTuneStore(FakeCatalogRepository(), sessionRepository) {
                store.state.content shouldBe Content.NoModel
                store.state.modelName shouldBe null
            }
        }

        test("a chat model without tunable parameters shows Unsupported") {
            val model = chatModel(
                id = "plain-chat",
                name = "Plain Model",
                supportedTuningParameters = emptySet()
            )
            withTuneStore(chatCatalog(model), chatSession(model)) {
                store.state.content shouldBe Content.Unsupported
                store.state.modelName shouldBe "Plain Model"
            }
        }

        test("an active image model exposes its image items and name") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.state.modelName shouldBe "Image Model"
                store.state.content.shouldBeInstanceOf<Content.Image>().items
                    .map { it.parameter } shouldBe listOf(
                    ImageParameter.RESOLUTION,
                    ImageParameter.SIZE,
                    ImageParameter.OUTPUT_COMPRESSION,
                    ImageParameter.N,
                    ImageParameter.SEED
                )
            }
        }

        test("switching the active model updates the content and the name") {
            withTuneStore(
                FakeCatalogRepository(
                    initialChatModels = listOf(chatTuneModel),
                    initialImageModels = listOf(imageTuneModel)
                ),
                chatSession()
            ) {
                store.state.content.shouldBeInstanceOf<Content.Chat>()

                sessionRepository.setActiveModel(ActiveModelRef.Image("image-model"))
                idle()

                store.state.modelName shouldBe "Image Model"
                store.state.content.shouldBeInstanceOf<Content.Image>()
            }
        }

        test("saved session parameters appear as formatted raw text") {
            val sessionRepository = FakeSessionRepository(
                initialActiveModel = ActiveModelRef.Chat("chat-model"),
                initialChatTuningParameters = mapOf(TuningParameter.TEMPERATURE to 0.5)
            )
            withTuneStore(chatCatalog(), sessionRepository) {
                store.state.content.shouldBeInstanceOf<Content.Chat>().items
                    .map { it.raw } shouldBe listOf("0.5", null, null, null)
                sessionRepository.calls shouldBe emptyList()
            }
        }

        maxTokensCases.forEach { case ->
            test(case.name) {
                withTuneStore(chatCatalog(case.model), chatSession(case.model)) {
                    store.state.content.shouldBeInstanceOf<Content.Chat>()
                        .maxTokensLimit shouldBe case.expectedLimit
                }
            }
        }
    }

    testSuite("panel") {

        test("OpenPanel and ClosePanel toggle the panel state") {
            withTuneStore {
                store.state.isPanelOpen shouldBe false

                store.accept(Intent.OpenPanel)
                idle()

                store.state.isPanelOpen shouldBe true

                store.accept(Intent.ClosePanel)
                idle()

                store.state.isPanelOpen shouldBe false
            }
        }

        test("ChangeHintsVisibility updates the hints flag") {
            withTuneStore {
                store.state.hintsVisible shouldBe true

                store.accept(Intent.ChangeHintsVisibility(false))
                idle()

                store.state.hintsVisible shouldBe false

                store.accept(Intent.ChangeHintsVisibility(true))
                idle()

                store.state.hintsVisible shouldBe true
            }
        }
    }

    testSuite("chat values") {

        test("ChangeChatValue with a valid raw writes the session map once") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatValue(TuningParameter.TEMPERATURE, "0.5"))
                idle()

                store.state.chatItem(TuningParameter.TEMPERATURE).raw shouldBe "0.5"
                sessionRepository.calls shouldBe listOf("setChatTuningParameters")
                sessionRepository.chatTuningParameters.first() shouldBe mapOf(
                    TuningParameter.TEMPERATURE to 0.5
                )
            }
        }

        test("an invalid raw marks the item invalid and writes nothing") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatValue(TuningParameter.TEMPERATURE, "abc"))
                idle()

                store.state.chatItem(TuningParameter.TEMPERATURE).input shouldBe
                    TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)
                sessionRepository.calls shouldBe emptyList()
                sessionRepository.chatTuningParameters.first() shouldBe emptyMap()
            }
        }

        test("an empty raw after a valid one removes the key from the session map") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatValue(TuningParameter.TEMPERATURE, "0.5"))
                idle()
                store.accept(Intent.ChangeChatValue(TuningParameter.TEMPERATURE, ""))
                idle()

                store.state.chatItem(TuningParameter.TEMPERATURE).input shouldBe TuneInput.Empty
                sessionRepository.calls shouldBe listOf(
                    "setChatTuningParameters",
                    "setChatTuningParameters"
                )
                sessionRepository.chatTuningParameters.first() shouldBe emptyMap()
            }
        }

        test("ChangeChatSlider snaps to the step and writes the snapped value") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.73))
                idle()

                store.state.chatItem(TuningParameter.TEMPERATURE).raw shouldBe "0.75"
                sessionRepository.chatTuningParameters.first() shouldBe mapOf(
                    TuningParameter.TEMPERATURE to 0.75
                )
            }
        }

        test("writing the same slider value again writes nothing more") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.73))
                idle()
                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.73))
                idle()

                store.state.chatItem(TuningParameter.TEMPERATURE).raw shouldBe "0.75"
                sessionRepository.calls shouldBe listOf("setChatTuningParameters")
            }
        }

        test("two quick slider changes end with the last value in the session") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.5))
                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.9))
                idle()

                store.state.chatItem(TuningParameter.TEMPERATURE).raw shouldBe "0.9"
                sessionRepository.calls shouldBe listOf(
                    "setChatTuningParameters",
                    "setChatTuningParameters"
                )
                sessionRepository.chatTuningParameters.first() shouldBe mapOf(
                    TuningParameter.TEMPERATURE to 0.9
                )
            }
        }

        test("SetMaxTokensToLimit writes the model limit") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.SetMaxTokensToLimit)
                idle()

                store.state.chatItem(TuningParameter.MAX_TOKENS).raw shouldBe "1000"
                sessionRepository.chatTuningParameters.first() shouldBe mapOf(
                    TuningParameter.MAX_TOKENS to 1000.0
                )
            }
        }

        test("SetMaxTokensToLimit does nothing without a positive limit") {
            val model = chatModel(
                id = "chat-model",
                name = "Chat Model",
                supportedTuningParameters = setOf(TuningParameter.MAX_TOKENS),
                maxCompletionTokens = 0,
                contextLength = 5000
            )
            withTuneStore(chatCatalog(model), chatSession(model)) {
                val stateBefore = store.state

                store.accept(Intent.SetMaxTokensToLimit)
                idle()

                store.state shouldBe stateBefore
                sessionRepository.calls shouldBe emptyList()
            }
        }

        test("RandomizeChatSeed writes a valid seed within the allowed range") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.RandomizeChatSeed)
                idle()

                val value = store.state.chatItem(TuningParameter.SEED).input
                    .shouldBeInstanceOf<TuneInput.Valid>()
                    .value

                value shouldBeGreaterThanOrEqualTo 0.0
                value shouldBeLessThanOrEqualTo 2_147_483_647.0
                sessionRepository.chatTuningParameters.first() shouldBe mapOf(
                    TuningParameter.SEED to value
                )
            }
        }

        test("ResetAll clears every chat item and writes an empty session map") {
            withTuneStore(chatCatalog(), chatSession()) {
                store.accept(Intent.ChangeChatValue(TuningParameter.TEMPERATURE, "0.5"))
                idle()
                store.accept(Intent.ChangeChatValue(TuningParameter.MAX_TOKENS, "500"))
                idle()

                store.accept(Intent.ResetAll)
                idle()

                store.state.content.shouldBeInstanceOf<Content.Chat>().items
                    .map { it.raw } shouldBe listOf(null, null, null, null)
                sessionRepository.chatTuningParameters.first() shouldBe emptyMap()
            }
        }
    }

    testSuite("image values") {

        test("SelectImageOption selects and deselects the same option") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.accept(
                    Intent.SelectImageOption(ImageParameter.RESOLUTION, "1024x1024")
                )
                idle()

                store.state.imageChoice(ImageParameter.RESOLUTION).selected shouldBe
                    "1024x1024"
                sessionRepository.imageParameters.first() shouldBe mapOf(
                    ImageParameter.RESOLUTION to ImageParameterValue.Text("1024x1024")
                )

                store.accept(
                    Intent.SelectImageOption(ImageParameter.RESOLUTION, "1024x1024")
                )
                idle()

                store.state.imageChoice(ImageParameter.RESOLUTION).selected shouldBe null
                sessionRepository.imageParameters.first() shouldBe emptyMap()
                sessionRepository.calls shouldBe listOf(
                    "setImageParameters",
                    "setImageParameters"
                )
            }
        }

        test("ChangeImageValue with a valid raw writes the session map once") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, "512"))
                idle()

                store.state.imageNumeric(ImageParameter.SIZE).raw shouldBe "512"
                sessionRepository.calls shouldBe listOf("setImageParameters")
                sessionRepository.imageParameters.first() shouldBe mapOf(
                    ImageParameter.SIZE to ImageParameterValue.Number(512.0)
                )
            }
        }

        test("an invalid image raw marks the item invalid and writes nothing") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, "abc"))
                idle()

                store.state.imageNumeric(ImageParameter.SIZE).input shouldBe
                    TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)
                sessionRepository.calls shouldBe emptyList()
                sessionRepository.imageParameters.first() shouldBe emptyMap()
            }
        }

        test("an empty image raw after a valid one removes the key from the session map") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, "512"))
                idle()
                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, ""))
                idle()

                store.state.imageNumeric(ImageParameter.SIZE).input shouldBe TuneInput.Empty
                sessionRepository.calls shouldBe listOf(
                    "setImageParameters",
                    "setImageParameters"
                )
                sessionRepository.imageParameters.first() shouldBe emptyMap()
            }
        }

        imageSliderCases.forEach { case ->
            test(
                "ChangeImageSlider ${case.parameter} ${case.value} writes ${case.expectedRaw}"
            ) {
                withTuneStore(imageCatalog(), imageSession()) {
                    store.accept(Intent.ChangeImageSlider(case.parameter, case.value))
                    idle()

                    store.state.imageNumeric(case.parameter).raw shouldBe case.expectedRaw
                    sessionRepository.imageParameters.first() shouldBe mapOf(
                        case.parameter to ImageParameterValue.Number(case.expectedRaw.toDouble())
                    )
                }
            }
        }

        test("RandomizeImageSeed writes a valid seed within the allowed range") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.accept(Intent.RandomizeImageSeed)
                idle()

                val value = store.state.imageNumeric(ImageParameter.SEED).input
                    .shouldBeInstanceOf<TuneInput.Valid>()
                    .value

                value shouldBeGreaterThanOrEqualTo 0.0
                value shouldBeLessThanOrEqualTo 2_147_483_647.0
                sessionRepository.imageParameters.first() shouldBe mapOf(
                    ImageParameter.SEED to ImageParameterValue.Number(value)
                )
            }
        }

        test("ResetAll clears every image selection and number") {
            withTuneStore(imageCatalog(), imageSession()) {
                store.accept(
                    Intent.SelectImageOption(ImageParameter.RESOLUTION, "1024x1024")
                )
                idle()
                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, "512"))
                idle()

                store.accept(Intent.ResetAll)
                idle()

                val items = store.state.content.shouldBeInstanceOf<Content.Image>().items

                items.filterIsInstance<ImageTuneItem.Choice>().single().selected shouldBe null
                items.filterIsInstance<ImageTuneItem.Numeric>().map { it.raw } shouldBe
                    listOf(null, null, null, null)
                sessionRepository.imageParameters.first() shouldBe emptyMap()
            }
        }
    }

    testSuite("failure") {

        test("a failed chat write can be retried with the same slider value") {
            val sessionRepository = chatSession()
            withTuneStore(chatCatalog(), sessionRepository) {
                sessionRepository.setChatTuningParametersFailure = Exception("x")

                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.5))
                idle()

                sessionRepository.calls shouldBe listOf("setChatTuningParameters")
                sessionRepository.chatTuningParameters.first() shouldBe emptyMap()

                sessionRepository.setChatTuningParametersFailure = null
                store.accept(Intent.ChangeChatSlider(TuningParameter.TEMPERATURE, 0.5))
                idle()

                sessionRepository.calls shouldBe listOf(
                    "setChatTuningParameters",
                    "setChatTuningParameters"
                )
                sessionRepository.chatTuningParameters.first() shouldBe mapOf(
                    TuningParameter.TEMPERATURE to 0.5
                )
            }
        }

        test("a failed image write can be retried with the same value") {
            val sessionRepository = imageSession()
            withTuneStore(imageCatalog(), sessionRepository) {
                sessionRepository.setImageParametersFailure = Exception("x")

                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, "512"))
                idle()

                sessionRepository.calls shouldBe listOf("setImageParameters")
                sessionRepository.imageParameters.first() shouldBe emptyMap()

                sessionRepository.setImageParametersFailure = null
                store.accept(Intent.ChangeImageValue(ImageParameter.SIZE, "512"))
                idle()

                sessionRepository.calls shouldBe listOf(
                    "setImageParameters",
                    "setImageParameters"
                )
                sessionRepository.imageParameters.first() shouldBe mapOf(
                    ImageParameter.SIZE to ImageParameterValue.Number(512.0)
                )
            }
        }
    }
}
