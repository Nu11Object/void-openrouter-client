package com.nullo.voidapp.feature.auth.domain

import com.nullo.voidapp.core.auth.domain.exception.AuthException.CodeExchangeException
import com.nullo.voidapp.core.fakes.FakeAuthRepository
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.FakeOAuthWebLauncher
import com.nullo.voidapp.feature.auth.FakePkceGenerator
import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import com.nullo.voidapp.feature.auth.domain.usecase.SignInViaOpenRouterUseCase
import com.nullo.voidapp.feature.auth.exception.OAuthException
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.http.URLBuilder

val SignInViaOpenRouterUseCaseTest by testSuite {

    test("the auth URL is built from the callback URL and the PKCE challenge") {
        val launcher = FakeOAuthWebLauncher().apply {
            redirectUrl = "http://localhost:1234/callback"
        }
        val pkce = FakePkceGenerator().apply {
            params = PkceParams(codeVerifier = "verifier", codeChallenge = "challenge")
        }
        val useCase = SignInViaOpenRouterUseCase(FakeAuthRepository(), pkce, launcher)

        useCase()

        val parameters = URLBuilder(launcher.authUrls.single()).parameters
        parameters["callback_url"] shouldBe "http://localhost:1234/callback"
        parameters["code_challenge"] shouldBe "challenge"
        parameters["code_challenge_method"] shouldBe "S256"
        pkce.calls shouldBe 1
    }

    test("the callback code and the verifier are passed to the repository") {
        val authRepository = FakeAuthRepository()
        val launcher = FakeOAuthWebLauncher().apply {
            result = { "https://callback.example/callback?code=abc&state=x" }
        }
        val pkce = FakePkceGenerator().apply {
            params = PkceParams(codeVerifier = "verifier", codeChallenge = "challenge")
        }
        val useCase = SignInViaOpenRouterUseCase(authRepository, pkce, launcher)

        useCase()

        authRepository.calls shouldContain "exchangeAuthCodeForApiKey:abc:verifier"
    }

    test("the acquired key is saved") {
        val authRepository = FakeAuthRepository().apply { exchangedApiKey = "acquired-key" }
        val launcher = FakeOAuthWebLauncher().apply {
            result = { "https://callback.example/callback?code=abc" }
        }
        val useCase = SignInViaOpenRouterUseCase(authRepository, FakePkceGenerator(), launcher)

        useCase()

        authRepository.calls shouldContain "saveApiKey:acquired-key"
    }

    test("a callback without a code exchanges null") {
        val authRepository = FakeAuthRepository()
        val launcher = FakeOAuthWebLauncher().apply {
            result = { "https://callback.example/callback" }
        }
        val useCase = SignInViaOpenRouterUseCase(authRepository, FakePkceGenerator(), launcher)

        useCase()

        authRepository.calls shouldContain "exchangeAuthCodeForApiKey:null:test-verifier"
    }

    test("the launcher exception propagates and nothing is exchanged or saved") {
        val authRepository = FakeAuthRepository()
        val launcher = FakeOAuthWebLauncher().apply {
            failure = OAuthException(UiText.Static("browser"))
        }
        val useCase = SignInViaOpenRouterUseCase(authRepository, FakePkceGenerator(), launcher)

        shouldThrow<OAuthException> { useCase() }

        authRepository.calls shouldBe emptyList()
    }

    test("the exchange exception propagates and the key is not saved") {
        val authRepository = FakeAuthRepository().apply {
            exchangeAuthCodeForApiKeyFailure = CodeExchangeException()
        }
        val launcher = FakeOAuthWebLauncher()
        val useCase = SignInViaOpenRouterUseCase(authRepository, FakePkceGenerator(), launcher)

        shouldThrow<CodeExchangeException> { useCase() }

        authRepository.calls shouldContain "exchangeAuthCodeForApiKey:test-code:test-verifier"
        authRepository.calls.filter { it.startsWith("saveApiKey") } shouldBe emptyList()
    }
}
