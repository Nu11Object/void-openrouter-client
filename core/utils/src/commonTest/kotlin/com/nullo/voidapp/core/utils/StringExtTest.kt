package com.nullo.voidapp.core.utils

import com.nullo.voidapp.core.utils.kotlin.asCleanedApiKey
import com.nullo.voidapp.core.utils.kotlin.removeBearerPrefix
import com.nullo.voidapp.core.utils.kotlin.withBearerPrefix
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val StringExtTest by testSuite {
    test("withBearerPrefix prepends the Bearer prefix") {
        "some-key".withBearerPrefix() shouldBe "Bearer some-key"
    }

    test("withBearerPrefix keeps an empty string as is after the prefix") {
        "".withBearerPrefix() shouldBe "Bearer "
    }

    test("withBearerPrefix keeps surrounding whitespace") {
        " key ".withBearerPrefix() shouldBe "Bearer  key "
    }

    test("withBearerPrefix does not strip an existing Bearer prefix") {
        "Bearer key".withBearerPrefix() shouldBe "Bearer Bearer key"
    }

    test("removeBearerPrefix removes the prefix and keeps the following space") {
        "Bearer some-key".removeBearerPrefix() shouldBe " some-key"
    }

    test("removeBearerPrefix removes only the prefix itself") {
        "Bearer".removeBearerPrefix() shouldBe ""
    }

    test("removeBearerPrefix returns the string unchanged when there is no prefix") {
        "some-key".removeBearerPrefix() shouldBe "some-key"
    }

    test("removeBearerPrefix is case sensitive") {
        "bearer some-key".removeBearerPrefix() shouldBe "bearer some-key"
    }

    test("removeBearerPrefix removes only one prefix") {
        "Bearer Bearer some-key".removeBearerPrefix() shouldBe " Bearer some-key"
    }

    test("asCleanedApiKey trims whitespace") {
        "  some-key  ".asCleanedApiKey() shouldBe "some-key"
    }

    test("asCleanedApiKey removes the Bearer prefix and trims the remainder") {
        "Bearer some-key".asCleanedApiKey() shouldBe "some-key"
    }

    test("asCleanedApiKey handles whitespace around the prefix") {
        "  Bearer some-key  ".asCleanedApiKey() shouldBe "some-key"
    }

    test("asCleanedApiKey keeps keys without a prefix") {
        "some-key".asCleanedApiKey() shouldBe "some-key"
    }

    test("asCleanedApiKey returns an empty string for a blank key") {
        "   ".asCleanedApiKey() shouldBe ""
    }

    test("asCleanedApiKey returns an empty string for a prefix-only key") {
        "Bearer".asCleanedApiKey() shouldBe ""
    }

    test("asCleanedApiKey keeps a key that only looks like a prefixed one") {
        "bearer some-key".asCleanedApiKey() shouldBe "bearer some-key"
    }

    test("asCleanedApiKey removes only one prefix") {
        "  Bearer Bearer some-key  ".asCleanedApiKey() shouldBe "Bearer some-key"
    }

    test("asCleanedApiKey keeps inner spaces") {
        "Bearer key with spaces".asCleanedApiKey() shouldBe "key with spaces"
    }
}
