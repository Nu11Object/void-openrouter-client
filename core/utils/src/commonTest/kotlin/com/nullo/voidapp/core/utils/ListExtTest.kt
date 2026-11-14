package com.nullo.voidapp.core.utils

import com.nullo.voidapp.core.utils.kotlin.filterWithQuery
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

val ListExtTest by testSuite {
    val items = listOf("Apple", "banana", "Cherry")

    test("blank query returns the original list instance") {
        items.filterWithQuery("  ") { it } shouldBeSameInstanceAs items
    }

    test("empty query returns the original list instance") {
        items.filterWithQuery("") { it } shouldBeSameInstanceAs items
    }

    test("query filters items by their mapped text") {
        items.filterWithQuery("an") { it } shouldBe listOf("banana")
    }

    test("query matching is case insensitive") {
        items.filterWithQuery("APP") { it } shouldBe listOf("Apple")
    }

    test("query matches a substring in the middle of the text") {
        items.filterWithQuery("err") { it } shouldBe listOf("Cherry")
    }

    test("query uses the mapped value instead of the item itself") {
        listOf(1, 22, 333).filterWithQuery("2") { it.toString() } shouldBe listOf(22)
    }

    test("query is not trimmed") {
        items.filterWithQuery(" Apple") { it } shouldBe emptyList()
    }

    test("query with no matches returns an empty list") {
        items.filterWithQuery("z") { it } shouldBe emptyList()
    }

    test("query over an empty list returns an empty list") {
        emptyList<String>().filterWithQuery("a") { it } shouldBe emptyList()
    }

    test("query preserves the order of matching items") {
        listOf("ab", "ax", "bx").filterWithQuery("a") { it } shouldBe listOf("ab", "ax")
    }

    test("mapping is not called for a blank query") {
        var calls = 0
        items.filterWithQuery(" ") {
            calls++
            it
        }
        calls shouldBe 0
    }
}
