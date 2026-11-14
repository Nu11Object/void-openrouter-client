package com.nullo.voidapp.core.utils

import com.nullo.voidapp.core.utils.compose.SmoothStepEasing
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe

val EdgeFadingTest by testSuite {
    test("SmoothStepEasing maps zero to zero") {
        SmoothStepEasing(0f) shouldBe 0f
    }

    test("SmoothStepEasing maps one to one") {
        SmoothStepEasing(1f) shouldBe 1f
    }

    test("SmoothStepEasing maps the middle to the middle") {
        SmoothStepEasing(0.5f) shouldBe 0.5f
    }

    test("SmoothStepEasing follows the smoothstep formula") {
        SmoothStepEasing(0.25f) shouldBe 0.15625f
        SmoothStepEasing(0.75f) shouldBe 0.84375f
    }

    test("SmoothStepEasing is monotonic and stays in the unit range") {
        var previous = SmoothStepEasing(0f)

        for (step in 1..100) {
            val value = SmoothStepEasing(step / 100f)
            value shouldBeGreaterThanOrEqualTo previous
            (value in 0f..1f) shouldBe true
            previous = value
        }
    }
}
