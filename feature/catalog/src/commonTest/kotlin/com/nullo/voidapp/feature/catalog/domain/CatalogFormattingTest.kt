package com.nullo.voidapp.feature.catalog.domain

import com.nullo.voidapp.feature.catalog.presentation.ui.formatCompact
import com.nullo.voidapp.feature.catalog.presentation.ui.toCompactString
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val CatalogFormattingTest by testSuite {
    listOf(
        0 to "0",
        999 to "999",
        9_999 to "9999",
        10_000 to "10K",
        10_500 to "10.5K",
        10_050 to "10K",
        128_000 to "128K",
        999_999 to "999.9K",
        1_000_000 to "1M",
        1_500_000 to "1.5M",
        1_050_000 to "1M",
        1_999_999 to "1.9M"
    ).forEach { (value, expected) ->
        test("$value is formatted as $expected") {
            value.toCompactString() shouldBe expected
        }
    }

    test("negative values are not compacted") {
        (-1).toCompactString() shouldBe "-1"
        (-10_000).toCompactString() shouldBe "-10000"
        (-1_500_000).toCompactString() shouldBe "-1500000"
    }

    test("formats a value whose remainder is zero") {
        formatCompact(10_000, 1_000, "K") shouldBe "10K"
    }

    test("formats a value whose tenth is zero") {
        formatCompact(10_050, 1_000, "K") shouldBe "10K"
    }

    test("formats a value whose tenth is nine") {
        formatCompact(10_900, 1_000, "K") shouldBe "10.9K"
    }
}
