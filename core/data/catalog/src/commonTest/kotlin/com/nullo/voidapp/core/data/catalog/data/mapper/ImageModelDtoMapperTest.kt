package com.nullo.voidapp.core.data.catalog.data.mapper

import com.nullo.voidapp.core.data.catalog.catalogTestJson
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageModelArchitectureDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageModelDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageModelsListResponseDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageParameterCapabilityDto
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

private fun imageModelDto(
    id: String = "test/image-model",
    name: String = "Test Image Model",
    description: String = "Image model for testing",
    created: Long = 123L,
    architecture: ImageModelArchitectureDto = ImageModelArchitectureDto(),
    supportedParameters: Map<String, ImageParameterCapabilityDto> = emptyMap(),
    supportsStreaming: Boolean = false,
    endpoints: String = "images/models/test/image-model/endpoints",
) = ImageModelDto(
    id = id,
    name = name,
    description = description,
    created = created,
    architecture = architecture,
    supportedParameters = supportedParameters,
    supportsStreaming = supportsStreaming,
    endpoints = endpoints
)

private val FULL_IMAGE_MODEL_JSON = """
    {
      "data": [
        {
          "id": "google/imagen-4",
          "name": "Google: Imagen 4",
          "description": "Image generation model",
          "created": 1747930371,
          "architecture": {
            "input_modalities": ["text", "image"],
            "output_modalities": ["image"]
          },
          "supported_parameters": {
            "aspect_ratio": {"type": "enum", "values": ["1:1", "16:9", "1:1"]},
            "resolution": {"type": "enum", "values": ["1K", "2K"]},
            "n": {"type": "range", "min": 1.0, "max": 4.0},
            "background": {"type": "boolean"}
          },
          "supports_streaming": true,
          "endpoints": "https://openrouter.ai/api/v1/images/models/google/imagen-4/endpoints",
          "unknown_field": 42
        }
      ]
    }
""".trimIndent()

private val MINIMAL_IMAGE_MODEL_JSON = """
    {
      "data": [
        {
          "id": "minimal/image-model",
          "name": "Minimal Image Model",
          "description": "Minimal",
          "created": 1,
          "architecture": {},
          "supports_streaming": false,
          "endpoints": "images/models/minimal/endpoints"
        }
      ]
    }
""".trimIndent()

val ImageModelDtoMapperTest by testSuite {
    testSuite("scalar fields") {
        test("maps id, name, description, created, streaming and endpoints") {
            val model = imageModelDto(
                id = "google/imagen-4",
                name = "Imagen 4",
                description = "Image generation model",
                created = 456L,
                supportsStreaming = true,
                endpoints = "https://openrouter.ai/api/v1/images/models/google/imagen-4/endpoints"
            ).toDomain()

            model.id shouldBe "google/imagen-4"
            model.name shouldBe "Imagen 4"
            model.description shouldBe "Image generation model"
            model.createdAtTimestamp shouldBe 456L
            model.supportsStreaming shouldBe true
            model.endpointsUrl shouldBe "https://openrouter.ai/api/v1/images/models/google/imagen-4/endpoints"
            model.pinned shouldBe false
        }
    }

    testSuite("capabilities") {
        test("enum capability becomes options") {
            val model = imageModelDto(
                supportedParameters = mapOf(
                    "aspect_ratio" to ImageParameterCapabilityDto.Enum(listOf("1:1", "16:9"))
                )
            ).toDomain()

            model.supportedParameters[ImageParameter.ASPECT_RATIO] shouldBe
                ImageParameterCapability.Options(setOf("1:1", "16:9"))
        }

        test("enum duplicates collapse into a set") {
            val model = imageModelDto(
                supportedParameters = mapOf(
                    "aspect_ratio" to ImageParameterCapabilityDto.Enum(listOf("1:1", "1:1", "16:9"))
                )
            ).toDomain()

            model.supportedParameters[ImageParameter.ASPECT_RATIO] shouldBe
                ImageParameterCapability.Options(setOf("1:1", "16:9"))
        }

        test("range capability keeps min and max") {
            val model = imageModelDto(
                supportedParameters = mapOf(
                    "n" to ImageParameterCapabilityDto.Range(min = 1.0, max = 8.0)
                )
            ).toDomain()

            model.supportedParameters[ImageParameter.N] shouldBe
                ImageParameterCapability.Range(min = 1.0, max = 8.0)
        }

        test("boolean capability becomes a flag") {
            val model = imageModelDto(
                supportedParameters = mapOf(
                    "background" to ImageParameterCapabilityDto.Boolean
                )
            ).toDomain()

            model.supportedParameters[ImageParameter.BACKGROUND] shouldBe ImageParameterCapability.Flag
        }

        test("unknown parameter key is dropped and known keys are kept") {
            val model = imageModelDto(
                supportedParameters = mapOf(
                    "resolution" to ImageParameterCapabilityDto.Enum(listOf("1K", "2K")),
                    "custom_parameter" to ImageParameterCapabilityDto.Boolean
                )
            ).toDomain()

            model.supportedParameters shouldBe mapOf(
                ImageParameter.RESOLUTION to ImageParameterCapability.Options(setOf("1K", "2K"))
            )
        }
    }

    testSuite("modalities") {
        test("known input modalities are mapped and unknown are dropped") {
            val model = imageModelDto(
                architecture = ImageModelArchitectureDto(inputModalities = listOf("text", "image", "audio"))
            ).toDomain()

            model.inputModalities shouldBe setOf(Modality.TEXT, Modality.IMAGE)
        }

        test("known output modalities are mapped and unknown are dropped") {
            val model = imageModelDto(
                architecture = ImageModelArchitectureDto(outputModalities = listOf("image", "video"))
            ).toDomain()

            model.outputModalities shouldBe setOf(Modality.IMAGE)
        }
    }

    testSuite("json") {
        test("decodes a model with enum, range and boolean capabilities") {
            val model = catalogTestJson
                .decodeFromString<ImageModelsListResponseDto>(FULL_IMAGE_MODEL_JSON)
                .data
                .single()
                .toDomain()

            model.id shouldBe "google/imagen-4"
            model.name shouldBe "Google: Imagen 4"
            model.description shouldBe "Image generation model"
            model.createdAtTimestamp shouldBe 1_747_930_371L
            model.inputModalities shouldBe setOf(Modality.TEXT, Modality.IMAGE)
            model.outputModalities shouldBe setOf(Modality.IMAGE)
            model.supportsStreaming shouldBe true
            model.endpointsUrl shouldBe "https://openrouter.ai/api/v1/images/models/google/imagen-4/endpoints"
            model.supportedParameters shouldBe mapOf(
                ImageParameter.ASPECT_RATIO to ImageParameterCapability.Options(setOf("1:1", "16:9")),
                ImageParameter.RESOLUTION to ImageParameterCapability.Options(setOf("1K", "2K")),
                ImageParameter.N to ImageParameterCapability.Range(min = 1.0, max = 4.0),
                ImageParameter.BACKGROUND to ImageParameterCapability.Flag
            )
            model.pinned shouldBe false
        }

        test("an unknown capability type fails with SerializationException") {
            shouldThrow<SerializationException> {
                catalogTestJson.decodeFromString<ImageModelsListResponseDto>(
                    """
                        {
                          "data": [
                            {
                              "id": "unknown/capability",
                              "name": "Unknown Capability",
                              "description": "Unknown",
                              "created": 1,
                              "architecture": {},
                              "supported_parameters": {"quality": {"type": "video"}},
                              "supports_streaming": false,
                              "endpoints": "images/models/unknown/endpoints"
                            }
                          ]
                        }
                    """.trimIndent()
                )
            }
        }

        test("a model without description fails with SerializationException") {
            shouldThrow<SerializationException> {
                catalogTestJson.decodeFromString<ImageModelsListResponseDto>(
                    """
                        {
                          "data": [
                            {
                              "id": "no/description",
                              "name": "No Description",
                              "created": 1,
                              "architecture": {},
                              "supports_streaming": false,
                              "endpoints": "images/models/no-description/endpoints"
                            }
                          ]
                        }
                    """.trimIndent()
                )
            }
        }

        test("an absent supported parameters field gives an empty map") {
            val model = catalogTestJson
                .decodeFromString<ImageModelsListResponseDto>(MINIMAL_IMAGE_MODEL_JSON)
                .data
                .single()
                .toDomain()

            model.supportedParameters shouldBe emptyMap()
        }

        test("an absent data field fails with SerializationException") {
            shouldThrow<SerializationException> {
                catalogTestJson.decodeFromString<ImageModelsListResponseDto>("""{"models": []}""")
            }
        }
    }
}
