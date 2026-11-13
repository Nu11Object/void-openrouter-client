package com.nullo.voidapp.core.model

import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

private fun encode(value: ImageParameterCapability) = json.encodeToString(value)

private fun encode(value: ImageParameterValue) = json.encodeToString(value)

private fun decodeCapability(text: String) = json.decodeFromString<ImageParameterCapability>(text)

private fun decodeValue(text: String) = json.decodeFromString<ImageParameterValue>(text)

val ImageParameterSerializationTest by testSuite {
    testSuite("capability") {
        test("range is encoded with type discriminator") {
            encode(ImageParameterCapability.Range(min = 0.0, max = 4.0)) shouldBe
                """{"type":"range","min":0.0,"max":4.0}"""
        }

        test("options are encoded with type discriminator") {
            encode(ImageParameterCapability.Options(setOf("1:1", "16:9"))) shouldBe
                """{"type":"options","values":["1:1","16:9"]}"""
        }

        test("flag is encoded with type discriminator only") {
            encode(ImageParameterCapability.Flag) shouldBe """{"type":"flag"}"""
        }

        listOf(
            ImageParameterCapability.Range(min = 0.0, max = 4.0),
            ImageParameterCapability.Options(setOf("1:1", "16:9")),
            ImageParameterCapability.Options(emptySet()),
            ImageParameterCapability.Flag
        ).forEach { capability ->
            test("$capability survives round trip") {
                decodeCapability(encode(capability)) shouldBe capability
            }
        }

        test("unknown keys are ignored when decoding") {
            decodeCapability("""{"type":"range","min":0.0,"max":4.0,"extra":1}""") shouldBe
                ImageParameterCapability.Range(min = 0.0, max = 4.0)
        }

        test("unknown discriminator fails decoding") {
            shouldThrow<SerializationException> {
                decodeCapability("""{"type":"unknown"}""")
            }
        }

        test("missing discriminator fails decoding") {
            shouldThrow<SerializationException> {
                decodeCapability("""{"min":0.0,"max":4.0}""")
            }
        }
    }

    testSuite("value") {
        test("text is encoded with type discriminator") {
            encode(ImageParameterValue.Text("16:9")) shouldBe """{"type":"text","value":"16:9"}"""
        }

        test("number is encoded with type discriminator") {
            encode(ImageParameterValue.Number(2.0)) shouldBe """{"type":"number","value":2.0}"""
        }

        listOf(
            ImageParameterValue.Text("16:9"),
            ImageParameterValue.Text(""),
            ImageParameterValue.Number(2.0),
            ImageParameterValue.Number(0.0)
        ).forEach { value ->
            test("$value survives round trip") {
                decodeValue(encode(value)) shouldBe value
            }
        }

        test("unknown discriminator fails decoding") {
            shouldThrow<SerializationException> {
                decodeValue("""{"type":"unknown","value":1}""")
            }
        }
    }
}
