package com.nullo.voidapp.feature.catalog.presentation.ui

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.feature.catalog.presentation.ui.filters.toPriceOrNull
import com.nullo.voidapp.feature.catalog.presentation.ui.models.toPriceString
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val PriceFormattingTest by testSuite {
    testSuite("toPriceString") {
        listOf(
            "3" to "$3",
            "3.500000" to "$3.5",
            "0.000000" to "$0",
            "0.000001" to "$0.000001",
            "100" to "$100",
            "1000000" to "$1000000",
            "10.0" to "$10",
            "0.1" to "$0.1",
            "10.50" to "$10.5",
            "0.0000001" to "$0.0000001",
            "10000000" to "$10000000",
            "123456789.123456" to "$123456789.123456",
            "-3.5" to "$-3.5"
        ).forEach { (input, expected) ->
            test("$input is formatted as $expected") {
                input.toBigDecimal().toPriceString() shouldBe expected
            }
        }
    }

    testSuite("toPriceOrNull") {
        listOf(
            ".5" to "0.5",
            "1." to "1",
            "0" to "0",
            "3.5" to "3.5"
        ).forEach { (input, expected) ->
            test("\"$input\" is parsed as $expected") {
                input.toPriceOrNull()?.compareTo(expected.toBigDecimal()) shouldBe 0
            }
        }

        listOf("abc", "", "   ", "1.2.3").forEach { input ->
            test("\"$input\" is parsed as null") {
                input.toPriceOrNull() shouldBe null
            }
        }
    }
}
