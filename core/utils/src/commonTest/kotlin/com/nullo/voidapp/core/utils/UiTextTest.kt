package com.nullo.voidapp.core.utils

import com.nullo.voidapp.core.utils.resources.UiText
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import voidapp.core.utils.generated.resources.Res
import voidapp.core.utils.generated.resources.oops
import voidapp.core.utils.generated.resources.template_dismiss_with_timer

val UiTextTest by testSuite {
    test("invoke creates a resource without arguments") {
        UiText(Res.string.oops) shouldBe UiText.Resource(Res.string.oops)
    }

    test("invoke creates a resource with empty arguments") {
        (UiText(Res.string.oops) as UiText.Resource).args shouldBe emptyList()
    }

    test("invoke stores vararg arguments in order") {
        UiText(Res.string.template_dismiss_with_timer, 1, "second") shouldBe
            UiText.Resource(Res.string.template_dismiss_with_timer, listOf(1, "second"))
    }

    test("asStringSuspend returns static text") {
        UiText.Static("hello").asStringSuspend() shouldBe "hello"
    }

    test("asStringSuspend returns an empty string for Empty") {
        UiText.Empty.asStringSuspend() shouldBe ""
    }
}
