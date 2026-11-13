package com.nullo.voidapp.core.model

import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ModelFeatureTest by testSuite {
    listOf("reasoning", "include_reasoning", "thinking").forEach { key ->
        test("key $key maps to thinking") {
            ModelFeature.fromKey(key) shouldBe ModelFeature.THINKING
        }
    }

    test("uppercase key maps to thinking") {
        ModelFeature.fromKey("THINKING") shouldBe ModelFeature.THINKING
    }

    test("unknown key maps to null") {
        ModelFeature.fromKey("unknown-key") shouldBe null
    }
}
