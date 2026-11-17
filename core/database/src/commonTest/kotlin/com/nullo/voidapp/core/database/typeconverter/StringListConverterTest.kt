package com.nullo.voidapp.core.database.typeconverter

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val StringListConverterTest by testSuite {
    test("round trips a list") {
        val list = listOf("a", "b")

        StringListConverter.fromString(StringListConverter.toString(list)) shouldBe list
    }

    test("round trips an empty list") {
        val list = emptyList<String>()

        StringListConverter.fromString(StringListConverter.toString(list)) shouldBe list
    }

    listOf("", "   ").forEach { raw ->
        test("blank string \"$raw\" decodes to an empty list") {
            StringListConverter.fromString(raw) shouldBe emptyList()
        }
    }

    listOf("not json", "[1,").forEach { raw ->
        test("malformed JSON \"$raw\" decodes to an empty list") {
            StringListConverter.fromString(raw) shouldBe emptyList()
        }
    }

    test("special strings survive round trip") {
        val list = listOf("quote\"inside", "comma,inside", "юникод", "[brackets]")

        StringListConverter.fromString(StringListConverter.toString(list)) shouldBe list
    }

    test("storage format is pinned") {
        StringListConverter.toString(listOf("a", "b")) shouldBe "[\"a\",\"b\"]"
    }
}
