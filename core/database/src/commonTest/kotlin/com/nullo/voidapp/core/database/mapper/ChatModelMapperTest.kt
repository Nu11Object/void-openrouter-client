package com.nullo.voidapp.core.database.mapper

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.database.entity.ChatModelEntity
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.testing.chatModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun chatModelEntity(
    id: String = "test/chat-model",
    name: String = "Test Model",
    description: String = "Chat model for testing",
    createdAtTimestamp: Long? = 987654321L,
    contextLength: Int? = 1_000_000,
    maxCompletionTokens: Int? = 256_000,
    pinned: Boolean = false,
    inputModalities: List<String> = emptyList(),
    outputModalities: List<String> = emptyList(),
    supportedTuningParameters: List<String> = emptyList(),
    supportedFeatures: List<String> = emptyList(),
    promptPerMillion: String = "2",
    completionPerMillion: String = "10",
    image: String? = null,
    request: String? = null,
    webSearch: String? = null,
    defaultTemperature: Double? = null,
    defaultTopP: Double? = null,
    defaultTopK: Int? = null,
    defaultFrequencyPenalty: Double? = null,
    defaultPresencePenalty: Double? = null,
    defaultRepetitionPenalty: Double? = null,
) = ChatModelEntity(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = createdAtTimestamp,
    contextLength = contextLength,
    maxCompletionTokens = maxCompletionTokens,
    pinned = pinned,
    inputModalities = inputModalities,
    outputModalities = outputModalities,
    supportedTuningParameters = supportedTuningParameters,
    supportedFeatures = supportedFeatures,
    promptPerMillion = promptPerMillion,
    completionPerMillion = completionPerMillion,
    image = image,
    request = request,
    webSearch = webSearch,
    defaultTemperature = defaultTemperature,
    defaultTopP = defaultTopP,
    defaultTopK = defaultTopK,
    defaultFrequencyPenalty = defaultFrequencyPenalty,
    defaultPresencePenalty = defaultPresencePenalty,
    defaultRepetitionPenalty = defaultRepetitionPenalty
)

private fun assertNullablePrice(expected: BigDecimal?, actual: BigDecimal?) {
    if (expected == null) {
        actual shouldBe null
    } else {
        actual?.compareTo(expected) shouldBe 0
    }
}

private fun assertRoundTrip(expected: ChatModel) {
    val actual = expected.toEntity().toDomain()

    actual.id shouldBe expected.id
    actual.name shouldBe expected.name
    actual.description shouldBe expected.description
    actual.createdAtTimestamp shouldBe expected.createdAtTimestamp
    actual.contextLength shouldBe expected.contextLength
    actual.maxCompletionTokens shouldBe expected.maxCompletionTokens
    actual.inputModalities shouldBe expected.inputModalities
    actual.outputModalities shouldBe expected.outputModalities
    actual.supportedTuningParameters shouldBe expected.supportedTuningParameters
    actual.supportedFeatures shouldBe expected.supportedFeatures
    actual.pinned shouldBe expected.pinned

    actual.pricing.promptPerMillion.compareTo(expected.pricing.promptPerMillion) shouldBe 0
    actual.pricing.completionPerMillion.compareTo(expected.pricing.completionPerMillion) shouldBe 0
    assertNullablePrice(expected.pricing.image, actual.pricing.image)
    assertNullablePrice(expected.pricing.request, actual.pricing.request)
    assertNullablePrice(expected.pricing.webSearch, actual.pricing.webSearch)

    actual.defaultTuningParameters.temperature shouldBe expected.defaultTuningParameters.temperature
    actual.defaultTuningParameters.topP shouldBe expected.defaultTuningParameters.topP
    actual.defaultTuningParameters.topK shouldBe expected.defaultTuningParameters.topK
    actual.defaultTuningParameters.frequencyPenalty shouldBe expected.defaultTuningParameters.frequencyPenalty
    actual.defaultTuningParameters.presencePenalty shouldBe expected.defaultTuningParameters.presencePenalty
    actual.defaultTuningParameters.repetitionPenalty shouldBe expected.defaultTuningParameters.repetitionPenalty
}

val ChatModelMapperTest by testSuite {
    testSuite("toDomain") {
        test("maps all scalar fields") {
            val entity = chatModelEntity(
                id = "model/id",
                name = "Model Name",
                description = "Model Description",
                createdAtTimestamp = 123L,
                contextLength = 456,
                maxCompletionTokens = 789,
                pinned = true,
                defaultTemperature = 0.7,
                defaultTopP = 0.9,
                defaultTopK = 40,
                defaultFrequencyPenalty = 0.1,
                defaultPresencePenalty = 0.2,
                defaultRepetitionPenalty = 1.1
            )

            val model = entity.toDomain()

            model.id shouldBe "model/id"
            model.name shouldBe "Model Name"
            model.description shouldBe "Model Description"
            model.createdAtTimestamp shouldBe 123L
            model.contextLength shouldBe 456
            model.maxCompletionTokens shouldBe 789
            model.pinned shouldBe true
            model.defaultTuningParameters.temperature shouldBe 0.7
            model.defaultTuningParameters.topP shouldBe 0.9
            model.defaultTuningParameters.topK shouldBe 40
            model.defaultTuningParameters.frequencyPenalty shouldBe 0.1
            model.defaultTuningParameters.presencePenalty shouldBe 0.2
            model.defaultTuningParameters.repetitionPenalty shouldBe 1.1
        }

        listOf("3", "10.5", "0.000003").forEach { raw ->
            test("price \"$raw\" is parsed as is") {
                val model = chatModelEntity(
                    promptPerMillion = raw,
                    completionPerMillion = raw
                ).toDomain()
                val expected = raw.toBigDecimal()

                model.pricing.promptPerMillion.compareTo(expected) shouldBe 0
                model.pricing.completionPerMillion.compareTo(expected) shouldBe 0
            }
        }

        listOf("", " ", "abc", "1.2.3").forEach { raw ->
            test("price \"$raw\" becomes zero") {
                val model = chatModelEntity(
                    promptPerMillion = raw,
                    completionPerMillion = raw
                ).toDomain()

                model.pricing.promptPerMillion.compareTo(BigDecimal.ZERO) shouldBe 0
                model.pricing.completionPerMillion.compareTo(BigDecimal.ZERO) shouldBe 0
            }
        }

        test("unknown input modalities are dropped") {
            val model = chatModelEntity(inputModalities = listOf("text", "image", "audio")).toDomain()

            model.inputModalities shouldBe setOf(Modality.TEXT, Modality.IMAGE)
        }

        test("unknown output modalities are dropped") {
            val model = chatModelEntity(outputModalities = listOf("image", "video")).toDomain()

            model.outputModalities shouldBe setOf(Modality.IMAGE)
        }

        test("unknown tuning parameters are dropped") {
            val model = chatModelEntity(
                supportedTuningParameters = listOf("temperature", "unknown", "top_p")
            ).toDomain()

            model.supportedTuningParameters shouldBe setOf(TuningParameter.TEMPERATURE, TuningParameter.TOP_P)
        }

        test("unknown model features are dropped") {
            val model = chatModelEntity(supportedFeatures = listOf("unknown", "thinking")).toDomain()

            model.supportedFeatures shouldBe setOf(ModelFeature.THINKING)
        }

        listOf("reasoning", "include_reasoning", "thinking").forEach { alias ->
            test("feature alias \"$alias\" maps to thinking") {
                val model = chatModelEntity(supportedFeatures = listOf(alias)).toDomain()

                model.supportedFeatures shouldBe setOf(ModelFeature.THINKING)
            }
        }

        test("nullable fields stay null") {
            val model = chatModelEntity(
                createdAtTimestamp = null,
                contextLength = null,
                maxCompletionTokens = null,
                image = null,
                request = null,
                webSearch = null
            ).toDomain()

            model.createdAtTimestamp shouldBe null
            model.contextLength shouldBe null
            model.maxCompletionTokens shouldBe null
            model.pricing.image shouldBe null
            model.pricing.request shouldBe null
            model.pricing.webSearch shouldBe null
        }

        test("invalid optional prices become null") {
            val model = chatModelEntity(
                image = "abc",
                request = "abc",
                webSearch = "abc"
            ).toDomain()

            model.pricing.image shouldBe null
            model.pricing.request shouldBe null
            model.pricing.webSearch shouldBe null
        }
    }

    testSuite("toEntity") {
        test("modalities are written as lowercase names") {
            val model = chatModel(
                inputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                outputModalities = setOf(Modality.TEXT)
            )

            val entity = model.toEntity()

            entity.inputModalities.toSet() shouldBe setOf("text", "image")
            entity.outputModalities.toSet() shouldBe setOf("text")
        }

        test("tuning parameters are written as lowercase names") {
            val model = chatModel(
                supportedTuningParameters = setOf(
                    TuningParameter.TEMPERATURE,
                    TuningParameter.TOP_P,
                    TuningParameter.SEED
                )
            )

            val entity = model.toEntity()

            entity.supportedTuningParameters.toSet() shouldBe setOf("temperature", "top_p", "seed")
        }

        test("features are written as lowercase names") {
            val model = chatModel(supportedFeatures = setOf(ModelFeature.THINKING))

            model.toEntity().supportedFeatures.toSet() shouldBe setOf("thinking")
        }

        test("price fields are written as plain strings") {
            val model = chatModel(
                pricing = ModelPricing(
                    promptPerMillion = "3".toBigDecimal(),
                    completionPerMillion = "10.5".toBigDecimal(),
                    image = "0.04".toBigDecimal(),
                    request = "0.001".toBigDecimal(),
                    webSearch = "0.005".toBigDecimal()
                )
            )

            val entity = model.toEntity()

            entity.promptPerMillion shouldBe "3"
            entity.completionPerMillion shouldBe "10.5"
            entity.image shouldBe "0.04"
            entity.request shouldBe "0.001"
            entity.webSearch shouldBe "0.005"
        }

        test("null optional prices stay null") {
            val model = chatModel(
                pricing = ModelPricing(
                    promptPerMillion = "3".toBigDecimal(),
                    completionPerMillion = "10.5".toBigDecimal()
                )
            )

            val entity = model.toEntity()

            entity.image shouldBe null
            entity.request shouldBe null
            entity.webSearch shouldBe null
        }

        test("pinned flag is written as is") {
            chatModel(pinned = true).toEntity().pinned shouldBe true
            chatModel(pinned = false).toEntity().pinned shouldBe false
        }

        test("default tuning parameters are written as is") {
            val model = chatModel().copy(
                defaultTuningParameters = DefaultTuningParameters(
                    temperature = 0.7,
                    topP = 0.9,
                    topK = 40,
                    frequencyPenalty = 0.1,
                    presencePenalty = 0.2,
                    repetitionPenalty = 1.1
                )
            )

            val entity = model.toEntity()

            entity.defaultTemperature shouldBe 0.7
            entity.defaultTopP shouldBe 0.9
            entity.defaultTopK shouldBe 40
            entity.defaultFrequencyPenalty shouldBe 0.1
            entity.defaultPresencePenalty shouldBe 0.2
            entity.defaultRepetitionPenalty shouldBe 1.1
        }
    }

    testSuite("round trip") {
        test("chatModel() survives round trip") {
            assertRoundTrip(chatModel())
        }

        test("model with all optional fields set survives round trip") {
            val model = chatModel(
                inputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                outputModalities = setOf(Modality.TEXT),
                supportedFeatures = setOf(ModelFeature.THINKING),
                supportedTuningParameters = setOf(TuningParameter.TEMPERATURE, TuningParameter.TOP_P),
                pricing = ModelPricing(
                    promptPerMillion = "3".toBigDecimal(),
                    completionPerMillion = "10.5".toBigDecimal(),
                    image = "0.04".toBigDecimal(),
                    request = "0.001".toBigDecimal(),
                    webSearch = "0.005".toBigDecimal()
                ),
                contextLength = 1_000_000,
                maxCompletionTokens = 256_000,
                pinned = true
            ).copy(
                description = "Full model",
                createdAtTimestamp = 123L,
                defaultTuningParameters = DefaultTuningParameters(
                    temperature = 0.7,
                    topP = 0.9,
                    topK = 40,
                    frequencyPenalty = 0.1,
                    presencePenalty = 0.2,
                    repetitionPenalty = 1.1
                )
            )

            assertRoundTrip(model)
        }

        test("model with all optional fields null survives round trip") {
            val model = chatModel(
                pricing = ModelPricing(
                    promptPerMillion = "2".toBigDecimal(),
                    completionPerMillion = "10".toBigDecimal()
                ),
                contextLength = null,
                maxCompletionTokens = null
            ).copy(
                createdAtTimestamp = null,
                defaultTuningParameters = DefaultTuningParameters()
            )

            assertRoundTrip(model)
        }
    }
}
