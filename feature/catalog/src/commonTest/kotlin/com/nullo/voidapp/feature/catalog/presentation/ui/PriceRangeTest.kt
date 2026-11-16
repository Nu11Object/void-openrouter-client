package com.nullo.voidapp.feature.catalog.presentation.ui

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.feature.catalog.domain.PriceRange
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val PriceRangeTest by testSuite {
    testSuite("isUnbounded") {
        test("range without bounds is unbounded") {
            PriceRange().isUnbounded shouldBe true
        }

        test("range with only min is bounded") {
            PriceRange(min = 1.toBigDecimal()).isUnbounded shouldBe false
        }

        test("range with only max is bounded") {
            PriceRange(max = 1.toBigDecimal()).isUnbounded shouldBe false
        }

        test("range with both bounds is bounded") {
            PriceRange(min = 1.toBigDecimal(), max = 2.toBigDecimal()).isUnbounded shouldBe false
        }
    }

    testSuite("contains") {
        test("unbounded range accepts any price") {
            val range = PriceRange()

            listOf("-100".toBigDecimal(), "0".toBigDecimal(), "1".toBigDecimal(), "1000000".toBigDecimal())
                .forEach { range.contains(it) shouldBe true }
        }

        test("range with only min accepts prices at or above min") {
            val range = PriceRange(min = 1.toBigDecimal())

            range.contains(1.toBigDecimal()) shouldBe true
            range.contains(2.toBigDecimal()) shouldBe true
            range.contains("0.999999".toBigDecimal()) shouldBe false
        }

        test("range with only max accepts prices at or below max") {
            val range = PriceRange(max = 3.toBigDecimal())

            range.contains(3.toBigDecimal()) shouldBe true
            range.contains(2.toBigDecimal()) shouldBe true
            range.contains("3.000001".toBigDecimal()) shouldBe false
        }

        test("range with both bounds accepts prices inside the range") {
            val range = PriceRange(min = 1.toBigDecimal(), max = 3.toBigDecimal())

            range.contains(2.toBigDecimal()) shouldBe true
            range.contains(0.toBigDecimal()) shouldBe false
            range.contains(4.toBigDecimal()) shouldBe false
        }

        test("bounds are inclusive") {
            val range = PriceRange(min = 1.toBigDecimal(), max = 3.toBigDecimal())
            val epsilon = "0.000001".toBigDecimal()

            range.contains(1.toBigDecimal()) shouldBe true
            range.contains(3.toBigDecimal()) shouldBe true
            range.contains(1.toBigDecimal() - epsilon) shouldBe false
            range.contains(3.toBigDecimal() + epsilon) shouldBe false
        }
    }
}
