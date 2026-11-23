package com.nullo.voidapp.feature.auth.presentation.store

import com.nullo.voidapp.core.auth.domain.exception.AuthException
import com.nullo.voidapp.core.fakes.FakeAuthRepository
import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.FakeOAuthWebLauncher
import com.nullo.voidapp.feature.auth.FakePkceGenerator
import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import com.nullo.voidapp.feature.auth.exception.OAuthCancelledException
import com.nullo.voidapp.feature.auth.exception.OAuthException
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore.Intent
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.exception_internal
import voidapp.core.utils.generated.resources.exception_invalid_api_key
import voidapp.core.utils.generated.resources.exception_secure_storage
import voidapp.core.utils.generated.resources.exception_validation_network
import voidapp.core.utils.generated.resources.exception_validation_unexpected
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_code_exchange

val AuthStoreTest by testSuite {

    testSuite("bootstrap") {

        test("an existing key completes authentication after idle") {
            withAuthStore(authRepository = FakeAuthRepository(initialApiKey = "k")) {
                idle()
                store.state.isAuthCompleted shouldBe true
            }
        }

        test("no key keeps authentication incomplete") {
            withAuthStore {
                idle()
                store.state.isAuthCompleted shouldBe false
            }
        }

        test("a key saved later through the repository completes authentication") {
            withAuthStore {
                idle()
                store.state.isAuthCompleted shouldBe false

                authRepository.saveApiKey("k")
                idle()

                store.state.isAuthCompleted shouldBe true
            }
        }
    }

    testSuite("apiKey") {

        test("ChangeApiKey stores the input") {
            withAuthStore {
                store.accept(Intent.ChangeApiKey("test-key"))
                idle()
                store.state.apiKey shouldBe "test-key"
            }
        }

        test("SubmitApiKey sets isLoading and validation completes after validateDelay") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withAuthStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                store.state.isLoading shouldBe true
                authRepository.calls shouldBe listOf("validateApiKey:test-key")

                advanceBy(1.seconds)

                authRepository.calls shouldBe listOf("validateApiKey:test-key", "saveApiKey:test-key")
            }
        }

        test("successful submit validates and saves the cleaned key and completes authentication") {
            withAuthStore {
                store.accept(Intent.ChangeApiKey("  Bearer sk-1  "))
                store.accept(Intent.SubmitApiKey)
                idle()

                authRepository.calls shouldBe listOf("validateApiKey:sk-1", "saveApiKey:sk-1")
                store.state.isAuthCompleted shouldBe true
            }
        }

        test("after a successful submit isLoading stays true") {
            withAuthStore {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                store.state.isLoading shouldBe true
            }
        }

        test("a second SubmitApiKey cancels the validation in progress") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withAuthStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                advanceBy(500.milliseconds)
                store.accept(Intent.SubmitApiKey)
                idle()

                advanceBy(2.seconds)

                authRepository.calls.filter { it.startsWith("saveApiKey") }.size shouldBe 1
                store.state.isAuthCompleted shouldBe true
            }
        }

        test("SubmitApiKey validates the key that was entered when it was submitted") {
            withAuthStore {
                store.accept(Intent.ChangeApiKey("first"))
                store.accept(Intent.SubmitApiKey)
                store.accept(Intent.ChangeApiKey("second"))
                idle()

                authRepository.calls shouldBe listOf("validateApiKey:first", "saveApiKey:first")
                store.state.apiKey shouldBe "second"
            }
        }

        test("editing the key while validation is in progress does not stop it") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withAuthStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("first"))
                store.accept(Intent.SubmitApiKey)
                idle()

                advanceBy(500.milliseconds)
                store.accept(Intent.ChangeApiKey("second"))
                idle()

                advanceBy(500.milliseconds)

                authRepository.calls shouldBe listOf("validateApiKey:first", "saveApiKey:first")
                store.state.apiKey shouldBe "second"
            }
        }
    }

    testSuite("oauth") {

        test("SignInViaOpenRouter sets loading flags until the launch finishes") {
            val launcher = FakeOAuthWebLauncher().apply { launchDelay = 1.seconds }
            withAuthStore(launcher = launcher) {
                store.accept(Intent.SignInViaOpenRouter)
                idle()

                store.state.isLoading shouldBe true
                store.state.isOAuthInProgress shouldBe true

                advanceBy(500.milliseconds)

                store.state.isLoading shouldBe true
                store.state.isOAuthInProgress shouldBe true

                advanceBy(500.milliseconds)

                store.state.isAuthCompleted shouldBe true
            }
        }

        test("successful OAuth flow exchanges the callback code and saves the key") {
            val pkce = FakePkceGenerator().apply {
                params = PkceParams(codeVerifier = "verifier", codeChallenge = "challenge")
            }
            val launcher = FakeOAuthWebLauncher().apply {
                redirectUrl = "https://callback.example/callback"
                result = { "https://callback.example/callback?code=abc" }
            }
            withAuthStore(launcher = launcher, pkce = pkce) {
                store.accept(Intent.SignInViaOpenRouter)
                idle()

                launcher.authUrls.size shouldBe 1
                pkce.calls shouldBe 1
                authRepository.calls shouldBe listOf(
                    "exchangeAuthCodeForApiKey:abc:verifier",
                    "saveApiKey:exchanged-api-key"
                )
                store.state.isAuthCompleted shouldBe true
                store.state.isLoading shouldBe true
                store.state.isOAuthInProgress shouldBe true
            }
        }

        test("CancelOAuth while the launch is pending stops the flow") {
            val launcher = FakeOAuthWebLauncher().apply { launchDelay = 1.seconds }
            withAuthStore(launcher = launcher) {
                store.accept(Intent.SignInViaOpenRouter)
                idle()

                store.accept(Intent.CancelOAuth)
                idle()

                store.state.isLoading shouldBe false
                store.state.isOAuthInProgress shouldBe false

                advanceBy(2.seconds)

                authRepository.calls.filter { it.startsWith("exchangeAuthCodeForApiKey") } shouldBe emptyList()
                store.state.isAuthCompleted shouldBe false
            }
        }

        test("CancelOAuth without a running OAuth flow clears loading while API key validation is pending") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withAuthStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                store.state.isLoading shouldBe true

                store.accept(Intent.CancelOAuth)
                idle()

                store.state.isLoading shouldBe false
                store.state.isOAuthInProgress shouldBe false
                authRepository.calls shouldBe listOf("validateApiKey:test-key")

                advanceBy(1.seconds)

                authRepository.calls shouldContain "saveApiKey:test-key"
                store.state.isLoading shouldBe false
            }
        }

        test("a second SignInViaOpenRouter cancels the first") {
            val launcher = FakeOAuthWebLauncher().apply {
                launchDelay = 1.seconds
                result = { "https://callback.example/callback?code=abc" }
            }
            withAuthStore(launcher = launcher) {
                store.accept(Intent.SignInViaOpenRouter)
                idle()

                advanceBy(500.milliseconds)

                store.accept(Intent.SignInViaOpenRouter)
                idle()

                advanceBy(2.seconds)

                launcher.authUrls.size shouldBe 2
                authRepository.calls.filter { it.startsWith("exchangeAuthCodeForApiKey") }.size shouldBe 1
                authRepository.calls.filter { it.startsWith("saveApiKey") }.size shouldBe 1
                store.state.isAuthCompleted shouldBe true
            }
        }

        test("OAuthCancelledException from the launcher clears loading flags without an error") {
            val launcher = FakeOAuthWebLauncher().apply { failure = OAuthCancelledException() }
            withAuthStore(launcher = launcher) {
                store.accept(Intent.SignInViaOpenRouter)
                idle()

                store.state.error shouldBe null
                store.state.isLoading shouldBe false
                store.state.isOAuthInProgress shouldBe false
            }
        }
    }

    testSuite("errors") {

        val errorCases = listOf(
            ErrorCase(
                name = "InvalidApiKeyException maps to exception_invalid_api_key",
                configure = { it.authRepository.validateApiKeyFailure = AuthException.InvalidApiKeyException() },
                trigger = {
                    it.store.accept(Intent.ChangeApiKey("test-key"))
                    it.store.accept(Intent.SubmitApiKey)
                },
                expected = UiText(CoreRes.string.exception_invalid_api_key)
            ),
            ErrorCase(
                name = "InternalServerException maps to exception_internal",
                configure = { it.authRepository.validateApiKeyFailure = AuthException.InternalServerException() },
                trigger = {
                    it.store.accept(Intent.ChangeApiKey("test-key"))
                    it.store.accept(Intent.SubmitApiKey)
                },
                expected = UiText(CoreRes.string.exception_internal)
            ),
            ErrorCase(
                name = "UnexpectedStatusException maps to exception_validation_unexpected",
                configure = { it.authRepository.validateApiKeyFailure = AuthException.UnexpectedStatusException() },
                trigger = {
                    it.store.accept(Intent.ChangeApiKey("test-key"))
                    it.store.accept(Intent.SubmitApiKey)
                },
                expected = UiText(CoreRes.string.exception_validation_unexpected)
            ),
            ErrorCase(
                name = "ValidationNetworkException maps to exception_validation_network",
                configure = { it.authRepository.validateApiKeyFailure = AuthException.ValidationNetworkException() },
                trigger = {
                    it.store.accept(Intent.ChangeApiKey("test-key"))
                    it.store.accept(Intent.SubmitApiKey)
                },
                expected = UiText(CoreRes.string.exception_validation_network)
            ),
            ErrorCase(
                name = "CodeExchangeException maps to exception_code_exchange",
                configure = {
                    it.authRepository.exchangeAuthCodeForApiKeyFailure = AuthException.CodeExchangeException()
                },
                trigger = { it.store.accept(Intent.SignInViaOpenRouter) },
                expected = UiText(Res.string.exception_code_exchange)
            ),
            ErrorCase(
                name = "SecureStorageException from save maps to exception_secure_storage",
                configure = { it.authRepository.saveApiKeyFailure = SecureStorageException() },
                trigger = {
                    it.store.accept(Intent.ChangeApiKey("test-key"))
                    it.store.accept(Intent.SubmitApiKey)
                },
                expected = UiText(CoreRes.string.exception_secure_storage)
            ),
            ErrorCase(
                name = "Exception with a message maps to Static",
                configure = { it.authRepository.validateApiKeyFailure = Exception("boom") },
                trigger = {
                    it.store.accept(Intent.ChangeApiKey("test-key"))
                    it.store.accept(Intent.SubmitApiKey)
                },
                expected = UiText.Static("boom")
            ),
            ErrorCase(
                name = "OAuthException from the launcher maps to its UiText",
                configure = { it.launcher.failure = OAuthException(UiText.Static("browser")) },
                trigger = { it.store.accept(Intent.SignInViaOpenRouter) },
                expected = UiText.Static("browser")
            )
        )

        errorCases.forEach { case ->
            test(case.name) {
                withAuthStore {
                    case.configure(this)
                    case.trigger(this)
                    idle()

                    store.state.error shouldBe case.expected
                    store.state.isLoading shouldBe false
                    store.state.isOAuthInProgress shouldBe false
                }
            }
        }

        test("DismissError clears the error and isLoading") {
            withAuthStore {
                authRepository.validateApiKeyFailure = Exception("boom")
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                store.state.error shouldBe UiText.Static("boom")

                store.accept(Intent.DismissError)
                idle()

                store.state.error shouldBe null
                store.state.isLoading shouldBe false
            }
        }
    }
}

private data class ErrorCase(
    val name: String,
    val configure: (AuthStoreTestEnv) -> Unit,
    val trigger: (AuthStoreTestEnv) -> Unit,
    val expected: UiText,
)
