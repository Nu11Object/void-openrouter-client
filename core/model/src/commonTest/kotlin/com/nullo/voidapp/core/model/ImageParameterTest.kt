package com.nullo.voidapp.core.model

import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ImageParameterTest by testSuite {
    ImageParameter.entries.forEach { parameter ->
        test("key ${parameter.key} maps to $parameter") {
            ImageParameter.fromKey(parameter.key) shouldBe parameter
        }
    }

    ImageParameter.entries.forEach { parameter ->
        test("uppercase key ${parameter.key.uppercase()} maps to $parameter") {
            ImageParameter.fromKey(parameter.key.uppercase()) shouldBe parameter
        }
    }

    test("mixed case key maps to its parameter") {
        ImageParameter.fromKey("Aspect_Ratio") shouldBe ImageParameter.ASPECT_RATIO
    }

    test("empty key maps to null") {
        ImageParameter.fromKey("") shouldBe null
    }

    test("unknown key maps to null") {
        ImageParameter.fromKey("unknown-key") shouldBe null
    }

    test("keys are unique") {
        ImageParameter.entries.map { it.key }.toSet().size shouldBe ImageParameter.entries.size
    }

    test("keys are lowercase") {
        ImageParameter.entries.forEach { parameter ->
            parameter.key shouldBe parameter.key.lowercase()
        }
    }
}
