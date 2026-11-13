package com.nullo.voidapp.core.model

import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val TuningParameterTest by testSuite {
    TuningParameter.entries.forEach { parameter ->
        test("key ${parameter.key} maps to $parameter") {
            TuningParameter.fromKey(parameter.key) shouldBe parameter
        }
    }

    TuningParameter.entries.forEach { parameter ->
        test("uppercase key ${parameter.key.uppercase()} maps to $parameter") {
            TuningParameter.fromKey(parameter.key.uppercase()) shouldBe parameter
        }
    }

    test("mixed case key maps to its parameter") {
        TuningParameter.fromKey("Top_P") shouldBe TuningParameter.TOP_P
    }

    test("empty key maps to null") {
        TuningParameter.fromKey("") shouldBe null
    }

    test("unknown key maps to null") {
        TuningParameter.fromKey("unknown-key") shouldBe null
    }

    test("keys are unique") {
        TuningParameter.entries.map { it.key }.toSet().size shouldBe TuningParameter.entries.size
    }

    test("keys are lowercase") {
        TuningParameter.entries.forEach { parameter ->
            parameter.key shouldBe parameter.key.lowercase()
        }
    }
}
