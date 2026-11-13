package com.nullo.voidapp.core.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun pricing(
    prompt: BigDecimal = BigDecimal.ZERO,
    completion: BigDecimal = BigDecimal.ZERO,
) = ModelPricing(
    promptPerMillion = prompt,
    completionPerMillion = completion
)

val ModelPricingTest by testSuite {
    test("zero prompt and zero completion is free") {
        pricing().isFree shouldBe true
    }

    test("non-zero prompt and zero completion is not free") {
        pricing(prompt = 1.toBigDecimal()).isFree shouldBe false
    }

    test("zero prompt and non-zero completion is not free") {
        pricing(completion = 1.toBigDecimal()).isFree shouldBe false
    }

    test("negative prompt price is not free") {
        pricing(prompt = (-1).toBigDecimal()).isFree shouldBe false
    }

    test("negative completion price is not free") {
        pricing(completion = (-1).toBigDecimal()).isFree shouldBe false
    }
}
