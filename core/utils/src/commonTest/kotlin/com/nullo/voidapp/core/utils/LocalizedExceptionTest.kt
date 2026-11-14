package com.nullo.voidapp.core.utils

import com.nullo.voidapp.core.utils.resources.LocalizedException
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.core.utils.resources.toUiText
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import voidapp.core.utils.generated.resources.Res
import voidapp.core.utils.generated.resources.error_unknown

val LocalizedExceptionTest by testSuite {
    test("toUiText returns the text of a LocalizedException") {
        val text = UiText.Static("localized")
        LocalizedException(text).toUiText() shouldBe text
    }

    test("toUiText wraps a non-blank exception message in Static") {
        Exception("boom").toUiText() shouldBe UiText.Static("boom")
    }

    test("toUiText keeps a message with surrounding whitespace") {
        Exception("  boom  ").toUiText() shouldBe UiText.Static("  boom  ")
    }

    test("toUiText falls back to the unknown error for an empty message") {
        Exception("").toUiText() shouldBe UiText.Resource(Res.string.error_unknown)
    }

    test("toUiText falls back to the unknown error for a blank message") {
        Exception("   ").toUiText() shouldBe UiText.Resource(Res.string.error_unknown)
    }

    test("toUiText falls back to the unknown error for a missing message") {
        RuntimeException().toUiText() shouldBe UiText.Resource(Res.string.error_unknown)
    }

    test("toUiText falls back to the unknown error for a non-Exception throwable") {
        Error("fatal").toUiText() shouldBe UiText.Resource(Res.string.error_unknown)
    }

    test("toUiText prefers LocalizedException over its message") {
        val text = UiText.Static("localized")
        LocalizedException(text, IllegalStateException("cause")).toUiText() shouldBe text
    }
}
