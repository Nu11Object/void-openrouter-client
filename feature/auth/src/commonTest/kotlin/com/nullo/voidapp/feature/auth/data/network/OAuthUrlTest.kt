package com.nullo.voidapp.feature.auth.data.network

import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.ktor.http.URLBuilder

val OAuthUrlTest by testSuite {

    test("buildOAuthUrl uses the auth endpoint and the PKCE parameters") {
        val pkce = PkceParams(codeVerifier = "verifier", codeChallenge = "challenge")

        val url = buildOAuthUrl("https://callback.example/callback", pkce)

        url.substringBefore("?") shouldBe AuthConfig.AUTH_URL

        val parameters = URLBuilder(url).parameters
        parameters["callback_url"] shouldBe "https://callback.example/callback"
        parameters["code_challenge"] shouldBe "challenge"
        parameters["code_challenge_method"] shouldBe "S256"
    }

    val callbackUrls = listOf(
        "http://localhost:1234/callback",
        "https://nu11object.github.io/void-oauth/oauth-callback.html"
    )

    callbackUrls.forEach { callbackUrl ->
        test("buildOAuthUrl percent-encodes and round-trips $callbackUrl") {
            val url = buildOAuthUrl(callbackUrl, PkceParams("verifier", "challenge"))

            url shouldNotContain callbackUrl
            URLBuilder(url).parameters["callback_url"] shouldBe callbackUrl
        }
    }

    val extractCases = listOf(
        ExtractCase(
            name = "returns the code when it is the only parameter",
            url = "https://callback.example/callback?code=abc",
            expected = "abc"
        ),
        ExtractCase(
            name = "returns the code when it is followed by another parameter",
            url = "https://callback.example/callback?code=abc&state=x",
            expected = "abc"
        ),
        ExtractCase(
            name = "returns the code when it is preceded by another parameter",
            url = "https://callback.example/callback?state=x&code=abc",
            expected = "abc"
        ),
        ExtractCase(
            name = "returns null when there is no query",
            url = "https://callback.example/callback",
            expected = null
        ),
        ExtractCase(
            name = "returns an empty string for an empty code parameter",
            url = "https://callback.example/callback?code=",
            expected = ""
        ),
        ExtractCase(
            name = "returns null for an empty string",
            url = "",
            expected = null
        ),
        ExtractCase(
            name = "returns null for a string that is not a URL",
            url = "not a url",
            expected = null
        )
    )

    extractCases.forEach { case ->
        test("extractAuthCodeParam ${case.name}") {
            case.url.extractAuthCodeParam() shouldBe case.expected
        }
    }
}

private data class ExtractCase(
    val name: String,
    val url: String,
    val expected: String?,
)
