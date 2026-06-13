package com.nullo.voidapp.feature.auth.data.network

import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OAuthUrlBuilderTest {

    @Test
    fun `buildOAuthUrl includes all required query parameters`() {
        val pkce = PkceParams("verifier", "challenge")
        val url = buildOAuthUrl("https://voidapp.test/callback", pkce)

        assertTrue(url.startsWith("https://openrouter.ai/auth"))
        assertTrue(url.contains("callback_url=https%3A%2F%2Fvoidapp.test%2Fcallback"))
        assertTrue(url.contains("code_challenge=challenge"))
        assertTrue(url.contains("code_challenge_method=S256"))
    }

    @Test
    fun `buildOAuthUrl uses provided code_challenge_method`() {
        val pkce = PkceParams("verifier", "challenge", "plain")
        val url = buildOAuthUrl("http://host", pkce)

        assertTrue(url.contains("code_challenge_method=plain"))
    }

    @Test
    fun `extractAuthCodeParam returns code when present in query`() {
        val result = "http://host?code=abc123".extractAuthCodeParam()

        assertEquals("abc123", result)
    }

    @Test
    fun `extractAuthCodeParam returns null when code param missing`() {
        val result = "http://host".extractAuthCodeParam()

        assertNull(result)
    }
}
