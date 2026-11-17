package com.nullo.voidapp.core.database.mapper

import com.nullo.voidapp.core.database.entity.ImageModelEntity
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import com.nullo.voidapp.core.testing.imageModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun imageModelEntity(
    id: String = "test/image-model",
    name: String = "Test Model",
    description: String = "Model for testing",
    createdAtTimestamp: Long = 123456789L,
    pinned: Boolean = false,
    endpointsUrl: String = "image/test/endpoint",
    inputModalities: List<String> = emptyList(),
    outputModalities: List<String> = emptyList(),
    supportsStreaming: Boolean = true,
    supportedParameters: Map<String, ImageParameterCapability> = emptyMap(),
) = ImageModelEntity(
    id = id,
    name = name,
    description = description,
    createdAtTimestamp = createdAtTimestamp,
    pinned = pinned,
    endpointsUrl = endpointsUrl,
    inputModalities = inputModalities,
    outputModalities = outputModalities,
    supportsStreaming = supportsStreaming,
    supportedParameters = supportedParameters
)

private fun assertRoundTrip(expected: ImageModel) {
    val actual = expected.toEntity().toDomain()

    actual.id shouldBe expected.id
    actual.name shouldBe expected.name
    actual.description shouldBe expected.description
    actual.createdAtTimestamp shouldBe expected.createdAtTimestamp
    actual.inputModalities shouldBe expected.inputModalities
    actual.outputModalities shouldBe expected.outputModalities
    actual.supportsStreaming shouldBe expected.supportsStreaming
    actual.supportedParameters shouldBe expected.supportedParameters
    actual.endpointsUrl shouldBe expected.endpointsUrl
    actual.pinned shouldBe expected.pinned
}

val ImageModelMapperTest by testSuite {
    testSuite("toDomain") {
        test("maps all scalar fields") {
            val entity = imageModelEntity(
                id = "image/model",
                name = "Image Model",
                description = "Image Description",
                createdAtTimestamp = 123L,
                pinned = true,
                endpointsUrl = "image/model/endpoint",
                supportsStreaming = false
            )

            val model = entity.toDomain()

            model.id shouldBe "image/model"
            model.name shouldBe "Image Model"
            model.description shouldBe "Image Description"
            model.createdAtTimestamp shouldBe 123L
            model.pinned shouldBe true
            model.endpointsUrl shouldBe "image/model/endpoint"
            model.supportsStreaming shouldBe false
        }

        test("pinned is kept for true and false") {
            imageModelEntity(pinned = true).toDomain().pinned shouldBe true
            imageModelEntity(pinned = false).toDomain().pinned shouldBe false
        }

        test("unknown input modalities are dropped") {
            val model = imageModelEntity(inputModalities = listOf("text", "image", "audio")).toDomain()

            model.inputModalities shouldBe setOf(Modality.TEXT, Modality.IMAGE)
        }

        test("unknown output modalities are dropped") {
            val model = imageModelEntity(outputModalities = listOf("image", "video")).toDomain()

            model.outputModalities shouldBe setOf(Modality.IMAGE)
        }

        test("unknown supported parameter keys are dropped") {
            val model = imageModelEntity(
                supportedParameters = mapOf(
                    "n" to ImageParameterCapability.Range(min = 1.0, max = 8.0),
                    "quality" to ImageParameterCapability.Options(setOf("low", "high")),
                    "unknown" to ImageParameterCapability.Flag
                )
            ).toDomain()

            model.supportedParameters shouldBe mapOf(
                ImageParameter.N to ImageParameterCapability.Range(min = 1.0, max = 8.0),
                ImageParameter.QUALITY to ImageParameterCapability.Options(setOf("low", "high"))
            )
        }
    }

    testSuite("toEntity") {
        test("pinned is written for true and false") {
            imageModel(pinned = true).toEntity().pinned shouldBe true
            imageModel(pinned = false).toEntity().pinned shouldBe false
        }

        test("modalities are written as lowercase names") {
            val model = imageModel(
                inputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                outputModalities = setOf(Modality.TEXT)
            )

            val entity = model.toEntity()

            entity.inputModalities.toSet() shouldBe setOf("text", "image")
            entity.outputModalities.toSet() shouldBe setOf("text")
        }

        test("supported parameters are written as lowercase names") {
            val model = imageModel(
                supportedParameters = mapOf(
                    ImageParameter.N to ImageParameterCapability.Range(min = 1.0, max = 8.0),
                    ImageParameter.QUALITY to ImageParameterCapability.Options(setOf("low", "high")),
                    ImageParameter.BACKGROUND to ImageParameterCapability.Flag
                )
            )

            model.toEntity().supportedParameters.keys shouldBe setOf("n", "quality", "background")
        }
    }

    testSuite("round trip") {
        test("imageModel() survives round trip") {
            assertRoundTrip(imageModel())
        }

        test("fully populated image model survives round trip") {
            val model = imageModel(
                id = "test/full-image-model",
                supportedParameters = mapOf(
                    ImageParameter.N to ImageParameterCapability.Range(min = 1.0, max = 8.0),
                    ImageParameter.QUALITY to ImageParameterCapability.Options(setOf("low", "high")),
                    ImageParameter.BACKGROUND to ImageParameterCapability.Flag
                ),
                inputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                outputModalities = setOf(Modality.TEXT),
                pinned = true
            ).copy(
                name = "Full Image Model",
                description = "Full image model",
                createdAtTimestamp = 987654321L,
                supportsStreaming = true,
                endpointsUrl = "image/full/endpoint"
            )

            assertRoundTrip(model)
        }
    }
}
