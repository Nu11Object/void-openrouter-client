package com.nullo.voidapp.core.model

import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun imageModel(
    supportedParameters: Map<ImageParameter, ImageParameterCapability> = emptyMap(),
) = ImageModel(
    id = "test/image-model",
    supportedParameters = supportedParameters,
    name = "Test Model",
    description = "Model for testing",
    createdAtTimestamp = 123456789,
    inputModalities = emptySet(),
    outputModalities = emptySet(),
    supportsStreaming = true,
    endpointsUrl = "image/test/endpoint",
    pinned = false
)

val ImageModelTest by testSuite {
    test("maxInputReferences returns range max") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(min = 0.0, max = 4.0))
        )
        model.maxInputReferences shouldBe 4
    }

    test("maxInputReferences truncates fractional range max") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(min = 0.0, max = 4.9))
        )
        model.maxInputReferences shouldBe 4
    }

    test("supportsInputReferences returns false when range max is less than one") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(min = 0.0, max = 0.9))
        )
        model.supportsInputReferences shouldBe false
    }

    test("supportsInputReferences returns false when range max is zero") {
        val model = imageModel(
            mapOf(
                ImageParameter.INPUT_REFERENCES to
                    ImageParameterCapability.Range(min = 0.0, max = 0.0)
            )
        )
        model.supportsInputReferences shouldBe false
    }

    test("maxInputReferences returns zero for options capability") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Options(emptySet()))
        )
        model.maxInputReferences shouldBe 0
    }

    test("maxInputReferences returns zero for flag capability") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Flag)
        )
        model.maxInputReferences shouldBe 0
    }

    test("maxInputReferences returns zero when parameter is missing") {
        imageModel().maxInputReferences shouldBe 0
    }

    test("supportsInputReferences returns false when parameter is missing") {
        imageModel().supportsInputReferences shouldBe false
    }

    test("supportsInputReferences returns true when range max is positive") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(min = 0.0, max = 2.0))
        )
        model.supportsInputReferences shouldBe true
    }

    test("supports() returns true for declared parameter") {
        val model = imageModel(
            mapOf(ImageParameter.N to ImageParameterCapability.Range(min = 0.0, max = 8.0))
        )
        model.supports(ImageParameter.N) shouldBe true
    }

    test("supports() returns false for undeclared parameter") {
        val model = imageModel(
            mapOf(ImageParameter.N to ImageParameterCapability.Range(min = 0.0, max = 8.0))
        )
        model.supports(ImageParameter.ASPECT_RATIO) shouldBe false
    }

    test("capabilityOf() returns capability of declared parameter") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(min = 0.0, max = 8.0))
        )
        model.capabilityOf(ImageParameter.INPUT_REFERENCES) shouldBe
            ImageParameterCapability.Range(min = 0.0, max = 8.0)
    }

    test("capabilityOf() returns null for undeclared parameter") {
        val model = imageModel(
            mapOf(ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(min = 0.0, max = 8.0))
        )
        model.capabilityOf(ImageParameter.ASPECT_RATIO) shouldBe null
    }
}
