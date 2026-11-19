package com.nullo.voidapp.core.data.catalog.data.mapper

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.data.catalog.catalogTestJson
import com.nullo.voidapp.core.data.catalog.data.service.dto.ChatModelsListResponseDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.DefaultTuningParametersDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ModelArchitectureDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ModelDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.PricingDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.TopProviderDto
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

private fun modelDto(
    id: String = "test/model",
    name: String = "Test Model",
    description: String? = null,
    created: Long? = null,
    contextLength: Int? = null,
    architecture: ModelArchitectureDto = ModelArchitectureDto(),
    topProvider: TopProviderDto? = null,
    pricing: PricingDto = PricingDto(prompt = "0", completion = "0"),
    supportedParameters: List<String> = emptyList(),
    defaultParameters: DefaultTuningParametersDto? = null,
) = ModelDto(
    id = id,
    name = name,
    description = description,
    created = created,
    contextLength = contextLength,
    architecture = architecture,
    topProvider = topProvider,
    pricing = pricing,
    supportedParameters = supportedParameters,
    defaultParameters = defaultParameters
)

private val FULL_MODEL_JSON = """
    {
      "data": [
        {
          "id": "openai/gpt-4o",
          "canonical_slug": "openai/gpt-4o-2024-05-13",
          "name": "OpenAI: GPT-4o",
          "description": "GPT-4o is a multimodal model.",
          "created": 1715367049,
          "context_length": 128000,
          "architecture": {
            "modality": "text+image->text",
            "input_modalities": ["text", "image"],
            "output_modalities": ["text"],
            "tokenizer": "GPT"
          },
          "top_provider": {
            "context_length": 128000,
            "max_completion_tokens": 16384,
            "is_moderated": false
          },
          "pricing": {
            "prompt": "0.0000025",
            "completion": "0.00001",
            "image": "0.003613",
            "request": "0",
            "web_search": "0.001",
            "internal_reasoning": "0"
          },
          "supported_parameters": ["temperature", "top_p", "max_tokens", "reasoning", "unknown_param"],
          "default_parameters": {
            "temperature": 0.7,
            "top_p": 0.9,
            "top_k": 40,
            "frequency_penalty": 0.1,
            "presence_penalty": 0.2,
            "repetition_penalty": 1.1,
            "unknown_default": 1
          },
          "links": {"details": "/api/v1/models/openai/gpt-4o/endpoints"}
        }
      ]
    }
""".trimIndent()

private val MINIMAL_MODEL_JSON = """
    {
      "data": [
        {
          "id": "minimal/model",
          "name": "Minimal Model",
          "pricing": {"prompt": "0", "completion": "0"}
        }
      ]
    }
""".trimIndent()

val ChatModelDtoMapperTest by testSuite {
    testSuite("scalar fields") {
        test("maps id, name, description, created and context length") {
            val model = modelDto(
                id = "openai/gpt-4o",
                name = "GPT-4o",
                description = "A multimodal model",
                created = 123L,
                contextLength = 128_000
            ).toDomain()

            model.id shouldBe "openai/gpt-4o"
            model.name shouldBe "GPT-4o"
            model.description shouldBe "A multimodal model"
            model.createdAtTimestamp shouldBe 123L
            model.contextLength shouldBe 128_000
        }

        test("null description becomes an empty string") {
            modelDto(description = null).toDomain().description shouldBe ""
        }

        test("max completion tokens come from the top provider") {
            val model = modelDto(topProvider = TopProviderDto(maxCompletionTokens = 16_384)).toDomain()

            model.maxCompletionTokens shouldBe 16_384
        }

        listOf(
            "null top provider" to null,
            "top provider without max completion tokens" to TopProviderDto(maxCompletionTokens = null)
        ).forEach { (label, topProvider) ->
            test("$label gives null max completion tokens") {
                modelDto(topProvider = topProvider).toDomain().maxCompletionTokens shouldBe null
            }
        }

        test("pinned is always false") {
            modelDto().toDomain().pinned shouldBe false
        }
    }

    testSuite("price scaling") {
        listOf(
            "0.000003" to "3",
            "0.00001" to "10",
            "0.000000150" to "0.15",
            "0" to "0",
            "1" to "1000000",
            "-1" to "-1000000"
        ).forEach { (raw, expected) ->
            test("prompt price \"$raw\" scales to $expected per million") {
                val model = modelDto(
                    pricing = PricingDto(prompt = raw, completion = "0.000001")
                ).toDomain()

                model.pricing.promptPerMillion.compareTo(expected.toBigDecimal()) shouldBe 0
                model.pricing.completionPerMillion.compareTo("1".toBigDecimal()) shouldBe 0
            }
        }

        listOf(
            "0.000003" to "3",
            "-1" to "-1000000"
        ).forEach { (raw, expected) ->
            test("completion price \"$raw\" scales to $expected per million") {
                val model = modelDto(
                    pricing = PricingDto(prompt = "0.000001", completion = raw)
                ).toDomain()

                model.pricing.completionPerMillion.compareTo(expected.toBigDecimal()) shouldBe 0
                model.pricing.promptPerMillion.compareTo("1".toBigDecimal()) shouldBe 0
            }
        }
    }

    testSuite("optional prices") {
        test("image, request and web search prices are not scaled") {
            val model = modelDto(
                pricing = PricingDto(
                    prompt = "0",
                    completion = "0",
                    image = "0.04",
                    request = "0.04",
                    webSearch = "0.04"
                )
            ).toDomain()

            model.pricing.image?.compareTo("0.04".toBigDecimal()) shouldBe 0
            model.pricing.request?.compareTo("0.04".toBigDecimal()) shouldBe 0
            model.pricing.webSearch?.compareTo("0.04".toBigDecimal()) shouldBe 0
        }

        listOf<String?>(null, "", "   ", "abc").forEach { raw ->
            test("optional price \"$raw\" becomes null") {
                val model = modelDto(
                    pricing = PricingDto(
                        prompt = "0",
                        completion = "0",
                        image = raw,
                        request = raw,
                        webSearch = raw
                    )
                ).toDomain()

                model.pricing.image shouldBe null
                model.pricing.request shouldBe null
                model.pricing.webSearch shouldBe null
            }
        }
    }

    testSuite("required prices") {
        listOf("", " ", "abc", "1.2.3").forEach { raw ->
            test("invalid prompt and completion price \"$raw\" becomes zero") {
                val model = modelDto(
                    pricing = PricingDto(prompt = raw, completion = raw)
                ).toDomain()

                model.pricing.promptPerMillion.compareTo("0".toBigDecimal()) shouldBe 0
                model.pricing.completionPerMillion.compareTo("0".toBigDecimal()) shouldBe 0
            }
        }

        test("surrounding whitespace is trimmed") {
            val model = modelDto(
                pricing = PricingDto(prompt = " 0.000002 ", completion = " 0.000003 ")
            ).toDomain()

            model.pricing.promptPerMillion.compareTo("2".toBigDecimal()) shouldBe 0
            model.pricing.completionPerMillion.compareTo("3".toBigDecimal()) shouldBe 0
        }

        listOf("3e-6", "3E-6").forEach { raw ->
            test("scientific notation \"$raw\" scales to 3 per million") {
                val model = modelDto(
                    pricing = PricingDto(prompt = raw, completion = raw)
                ).toDomain()

                model.pricing.promptPerMillion.compareTo("3".toBigDecimal()) shouldBe 0
                model.pricing.completionPerMillion.compareTo("3".toBigDecimal()) shouldBe 0
            }
        }
    }

    testSuite("modalities") {
        listOf(
            listOf("text", "image", "audio") to setOf(Modality.TEXT, Modality.IMAGE),
            listOf("TEXT", "IMAGE") to setOf(Modality.TEXT, Modality.IMAGE),
            listOf("audio", "video") to emptySet()
        ).forEach { (raw, expected) ->
            test("input modalities $raw map to $expected") {
                val model = modelDto(
                    architecture = ModelArchitectureDto(inputModalities = raw)
                ).toDomain()

                model.inputModalities shouldBe expected
            }
        }

        listOf(
            listOf("text", "image") to setOf(Modality.TEXT, Modality.IMAGE),
            listOf("IMAGE") to setOf(Modality.IMAGE),
            listOf("video") to emptySet()
        ).forEach { (raw, expected) ->
            test("output modalities $raw map to $expected") {
                val model = modelDto(
                    architecture = ModelArchitectureDto(outputModalities = raw)
                ).toDomain()

                model.outputModalities shouldBe expected
            }
        }
    }

    testSuite("supported parameters") {
        listOf(
            "temperature" to TuningParameter.TEMPERATURE,
            "top_p" to TuningParameter.TOP_P,
            "max_tokens" to TuningParameter.MAX_TOKENS
        ).forEach { (raw, expected) ->
            test("\"$raw\" becomes $expected and not a feature") {
                val model = modelDto(supportedParameters = listOf(raw)).toDomain()

                model.supportedTuningParameters shouldBe setOf(expected)
                model.supportedFeatures shouldBe emptySet()
            }
        }

        listOf("reasoning", "include_reasoning", "thinking").forEach { raw ->
            test("\"$raw\" becomes THINKING and not a tuning parameter") {
                val model = modelDto(supportedParameters = listOf(raw)).toDomain()

                model.supportedFeatures shouldBe setOf(ModelFeature.THINKING)
                model.supportedTuningParameters shouldBe emptySet()
            }
        }

        test("unknown parameter is dropped from both sets") {
            val model = modelDto(supportedParameters = listOf("unknown")).toDomain()

            model.supportedTuningParameters shouldBe emptySet()
            model.supportedFeatures shouldBe emptySet()
        }
    }

    testSuite("default parameters") {
        test("null default parameters give null defaults") {
            val defaults = modelDto(defaultParameters = null).toDomain().defaultTuningParameters

            defaults.temperature shouldBe null
            defaults.topP shouldBe null
            defaults.topK shouldBe null
            defaults.frequencyPenalty shouldBe null
            defaults.presencePenalty shouldBe null
            defaults.repetitionPenalty shouldBe null
        }

        test("all fields are mapped with distinct values") {
            val defaults = modelDto(
                defaultParameters = DefaultTuningParametersDto(
                    temperature = 0.1,
                    topP = 0.2,
                    topK = 3,
                    frequencyPenalty = 0.4,
                    presencePenalty = 0.5,
                    repetitionPenalty = 0.6
                )
            ).toDomain().defaultTuningParameters

            defaults.temperature shouldBe 0.1
            defaults.topP shouldBe 0.2
            defaults.topK shouldBe 3
            defaults.frequencyPenalty shouldBe 0.4
            defaults.presencePenalty shouldBe 0.5
            defaults.repetitionPenalty shouldBe 0.6
        }

        test("absent fields stay null") {
            val defaults = modelDto(
                defaultParameters = DefaultTuningParametersDto(
                    topP = 0.9,
                    presencePenalty = 0.2
                )
            ).toDomain().defaultTuningParameters

            defaults.temperature shouldBe null
            defaults.topP shouldBe 0.9
            defaults.topK shouldBe null
            defaults.frequencyPenalty shouldBe null
            defaults.presencePenalty shouldBe 0.2
            defaults.repetitionPenalty shouldBe null
        }
    }

    testSuite("json") {
        test("decodes a realistic model") {
            val model = catalogTestJson
                .decodeFromString<ChatModelsListResponseDto>(FULL_MODEL_JSON)
                .data
                .single()
                .toDomain()

            model.id shouldBe "openai/gpt-4o"
            model.name shouldBe "OpenAI: GPT-4o"
            model.description shouldBe "GPT-4o is a multimodal model."
            model.createdAtTimestamp shouldBe 1_715_367_049L
            model.contextLength shouldBe 128_000
            model.maxCompletionTokens shouldBe 16_384
            model.inputModalities shouldBe setOf(Modality.TEXT, Modality.IMAGE)
            model.outputModalities shouldBe setOf(Modality.TEXT)
            model.supportedTuningParameters shouldBe setOf(
                TuningParameter.TEMPERATURE,
                TuningParameter.TOP_P,
                TuningParameter.MAX_TOKENS
            )
            model.supportedFeatures shouldBe setOf(ModelFeature.THINKING)
            model.pricing.promptPerMillion.compareTo("2.5".toBigDecimal()) shouldBe 0
            model.pricing.completionPerMillion.compareTo("10".toBigDecimal()) shouldBe 0
            model.pricing.image?.compareTo("0.003613".toBigDecimal()) shouldBe 0
            model.pricing.request?.compareTo("0".toBigDecimal()) shouldBe 0
            model.pricing.webSearch?.compareTo("0.001".toBigDecimal()) shouldBe 0
            model.defaultTuningParameters.temperature shouldBe 0.7
            model.defaultTuningParameters.topP shouldBe 0.9
            model.defaultTuningParameters.topK shouldBe 40
            model.defaultTuningParameters.frequencyPenalty shouldBe 0.1
            model.defaultTuningParameters.presencePenalty shouldBe 0.2
            model.defaultTuningParameters.repetitionPenalty shouldBe 1.1
            model.pinned shouldBe false
        }

        test("decodes a minimal model") {
            val model = catalogTestJson
                .decodeFromString<ChatModelsListResponseDto>(MINIMAL_MODEL_JSON)
                .data
                .single()
                .toDomain()

            model.id shouldBe "minimal/model"
            model.name shouldBe "Minimal Model"
            model.defaultTuningParameters shouldBe DefaultTuningParameters()
        }

        test("a model without pricing fails with SerializationException") {
            shouldThrow<SerializationException> {
                catalogTestJson.decodeFromString<ChatModelsListResponseDto>(
                    """{"data": [{"id": "no/pricing", "name": "No Pricing"}]}"""
                )
            }
        }

        test("a numeric prompt price fails with SerializationException") {
            shouldThrow<SerializationException> {
                catalogTestJson.decodeFromString<ChatModelsListResponseDto>(
                    """{"data": [{"id": "numeric/price", "name": "Numeric", "pricing": {"prompt": 0.000003, "completion": "0"}}]}"""
                )
            }
        }

        test("an absent data field fails with SerializationException") {
            shouldThrow<SerializationException> {
                catalogTestJson.decodeFromString<ChatModelsListResponseDto>("""{"models": []}""")
            }
        }
    }
}
