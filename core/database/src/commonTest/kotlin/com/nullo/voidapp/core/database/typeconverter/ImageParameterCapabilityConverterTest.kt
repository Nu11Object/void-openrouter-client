package com.nullo.voidapp.core.database.typeconverter

import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ImageParameterCapabilityConverterTest by testSuite {
    test("round trips a map with all capability types") {
        val map = mapOf(
            "n" to ImageParameterCapability.Range(min = 1.0, max = 8.0),
            "quality" to ImageParameterCapability.Options(setOf("low", "high")),
            "background" to ImageParameterCapability.Flag
        )

        ImageParameterCapabilityConverter.fromString(
            ImageParameterCapabilityConverter.toString(map)
        ) shouldBe map
    }

    test("round trips an empty map") {
        ImageParameterCapabilityConverter.fromString(
            ImageParameterCapabilityConverter.toString(emptyMap())
        ) shouldBe emptyMap()
    }

    listOf("", "   ").forEach { raw ->
        test("blank string \"$raw\" decodes to an empty map") {
            ImageParameterCapabilityConverter.fromString(raw) shouldBe emptyMap()
        }
    }

    listOf("not json", "[1,").forEach { raw ->
        test("malformed JSON \"$raw\" decodes to an empty map") {
            ImageParameterCapabilityConverter.fromString(raw) shouldBe emptyMap()
        }
    }

    test("unknown keys inside a capability are ignored") {
        val raw = """{"n":{"type":"range","min":1.0,"max":8.0,"extra":"ignored"}}"""

        ImageParameterCapabilityConverter.fromString(raw) shouldBe
            mapOf("n" to ImageParameterCapability.Range(min = 1.0, max = 8.0))
    }

    test("converter keeps unknown parameter keys") {
        val raw = """{"custom":{"type":"flag"}}"""

        ImageParameterCapabilityConverter.fromString(raw) shouldBe
            mapOf("custom" to ImageParameterCapability.Flag)
    }

    test("unknown type discriminator decodes to an empty map") {
        val raw = """{"n":{"type":"video"}}"""

        ImageParameterCapabilityConverter.fromString(raw) shouldBe emptyMap()
    }

    test("pins range storage format") {
        val map = mapOf("n" to ImageParameterCapability.Range(min = 1.0, max = 8.0))

        ImageParameterCapabilityConverter.toString(map) shouldBe
            """{"n":{"type":"range","min":1.0,"max":8.0}}"""
    }

    test("pins options storage format") {
        val map = mapOf("quality" to ImageParameterCapability.Options(setOf("low", "high")))

        ImageParameterCapabilityConverter.toString(map) shouldBe
            """{"quality":{"type":"options","values":["low","high"]}}"""
    }

    test("pins flag storage format") {
        val map = mapOf("background" to ImageParameterCapability.Flag)

        ImageParameterCapabilityConverter.toString(map) shouldBe
            """{"background":{"type":"flag"}}"""
    }

    test("decodes pretty-printed JSON with a different key order") {
        val raw = """
            {
                "quality": {"type": "options", "values": ["low", "high"]},
                "n": {"type": "range", "min": 1.0, "max": 8.0},
                "background": {"type": "flag"}
            }
        """.trimIndent()

        ImageParameterCapabilityConverter.fromString(raw) shouldBe mapOf(
            "quality" to ImageParameterCapability.Options(setOf("low", "high")),
            "n" to ImageParameterCapability.Range(min = 1.0, max = 8.0),
            "background" to ImageParameterCapability.Flag
        )
    }
}
