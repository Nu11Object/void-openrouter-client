package com.nullo.voidapp.core.model

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ErrorReasonTest by testSuite {
    ErrorReason.entries.forEach { reason ->
        test("key ${reason.name} maps to $reason") {
            ErrorReason.fromKey(reason.name) shouldBe reason
        }
    }

    ErrorReason.entries.forEach { reason ->
        test("lowercase key ${reason.name.lowercase()} maps to $reason") {
            ErrorReason.fromKey(reason.name.lowercase()) shouldBe reason
        }
    }

    test("null maps to unexpected reason") {
        ErrorReason.fromKey(null) shouldBe ErrorReason.UNEXPECTED
    }

    test("empty key maps to unexpected reason") {
        ErrorReason.fromKey("") shouldBe ErrorReason.UNEXPECTED
    }

    test("unknown key maps to unexpected reason") {
        ErrorReason.fromKey("unknown-key") shouldBe ErrorReason.UNEXPECTED
    }
}
