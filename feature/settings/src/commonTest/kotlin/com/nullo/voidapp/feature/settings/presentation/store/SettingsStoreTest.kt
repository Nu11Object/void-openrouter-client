package com.nullo.voidapp.feature.settings.presentation.store

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.nullo.voidapp.core.auth.domain.exception.AuthException
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.fakes.FakeAuthRepository
import com.nullo.voidapp.core.fakes.FakeSettingsRepository
import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.Intent
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.Label
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Dialog
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Loading
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import voidapp.core.utils.generated.resources.Res
import voidapp.core.utils.generated.resources.error_unknown
import voidapp.core.utils.generated.resources.exception_internal
import voidapp.core.utils.generated.resources.exception_invalid_api_key
import voidapp.core.utils.generated.resources.exception_secure_storage
import voidapp.core.utils.generated.resources.exception_validation_network
import voidapp.core.utils.generated.resources.exception_validation_unexpected

val SettingsStoreTest by testSuite {

    testSuite("bootstrap") {

        test("initial theme of the repository appears in State after idle") {
            val settingsRepository = FakeSettingsRepository(initialTheme = AppTheme.DARK)
            withSettingsStore(settingsRepository = settingsRepository) {
                idle()
                store.state.appTheme shouldBe AppTheme.DARK
            }
        }

        test("theme changed directly in the fake repository is reflected in State") {
            withSettingsStore {
                idle()
                settingsRepository.changeThemeExternally(AppTheme.LIGHT)
                idle()
                store.state.appTheme shouldBe AppTheme.LIGHT
            }
        }
    }

    testSuite("apiKey") {

        test("ChangeApiKey stores the input and resets isApiKeySaved") {
            withSettingsStore {
                store.accept(Intent.ChangeApiKey("test-key"))
                idle()
                store.state.apiKey shouldBe "test-key"
                store.state.isApiKeySaved shouldBe false
            }
        }

        test("SubmitApiKey sets loading ApiKey and clears after validateDelay") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withSettingsStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()
                store.state.loading shouldBe Loading.ApiKey
                advanceBy(1.seconds)
                store.state.loading shouldBe null
            }
        }

        test("a second SubmitApiKey cancels the validation in progress") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withSettingsStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                advanceBy(500.milliseconds)
                store.accept(Intent.SubmitApiKey)
                idle()

                advanceBy(1.seconds)

                authRepository.calls.filter { it.startsWith("saveApiKey") }.size shouldBe 1
                store.state.apiKey shouldBe ""
                store.state.isApiKeySaved shouldBe true
            }
        }

        test("SubmitApiKey validates the key that was entered when it was submitted") {
            withSettingsStore {
                store.accept(Intent.ChangeApiKey("first"))
                store.accept(Intent.SubmitApiKey)
                store.accept(Intent.ChangeApiKey("second"))
                idle()

                authRepository.calls shouldBe listOf("validateApiKey:first", "saveApiKey:first")
                store.state.apiKey shouldBe ""
                store.state.isApiKeySaved shouldBe true
            }
        }

        test("successful submit validates and saves cleaned key and resets isApiKeySaved after 2 seconds") {
            withSettingsStore {
                store.accept(Intent.ChangeApiKey("  Bearer sk-1  "))
                store.accept(Intent.SubmitApiKey)
                idle()

                authRepository.calls shouldBe listOf("validateApiKey:sk-1", "saveApiKey:sk-1")
                store.state.apiKey shouldBe ""
                store.state.isApiKeySaved shouldBe true
                store.state.loading shouldBe null

                advanceBy(1999.milliseconds)
                store.state.isApiKeySaved shouldBe true

                advanceBy(1.milliseconds)
                store.state.isApiKeySaved shouldBe false
            }
        }

        test("ChangeApiKey after successful submit clears isApiKeySaved immediately and pending reset does not fire") {
            withSettingsStore {
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()
                store.state.isApiKeySaved shouldBe true

                store.accept(Intent.ChangeApiKey("new-key"))
                idle()
                store.state.isApiKeySaved shouldBe false
                store.state.apiKey shouldBe "new-key"

                advanceBy(2.seconds)
                store.state.isApiKeySaved shouldBe false
            }
        }

        val errorCases = listOf(
            ErrorCase(
                name = "InvalidApiKeyException maps to exception_invalid_api_key",
                configure = { it.validateApiKeyFailure = AuthException.InvalidApiKeyException() },
                expected = UiText.Resource(Res.string.exception_invalid_api_key)
            ),
            ErrorCase(
                name = "InternalServerException maps to exception_internal",
                configure = { it.validateApiKeyFailure = AuthException.InternalServerException() },
                expected = UiText.Resource(Res.string.exception_internal)
            ),
            ErrorCase(
                name = "UnexpectedStatusException maps to exception_validation_unexpected",
                configure = { it.validateApiKeyFailure = AuthException.UnexpectedStatusException() },
                expected = UiText.Resource(Res.string.exception_validation_unexpected)
            ),
            ErrorCase(
                name = "ValidationNetworkException maps to exception_validation_network",
                configure = { it.validateApiKeyFailure = AuthException.ValidationNetworkException() },
                expected = UiText.Resource(Res.string.exception_validation_network)
            ),
            ErrorCase(
                name = "SecureStorageException from save maps to exception_secure_storage",
                configure = { it.saveApiKeyFailure = SecureStorageException() },
                expected = UiText.Resource(Res.string.exception_secure_storage)
            ),
            ErrorCase(
                name = "Exception with message maps to Static",
                configure = { it.validateApiKeyFailure = Exception("boom") },
                expected = UiText.Static("boom")
            ),
            ErrorCase(
                name = "Exception without message maps to error_unknown",
                configure = { it.validateApiKeyFailure = Exception() },
                expected = UiText.Resource(Res.string.error_unknown)
            )
        )

        errorCases.forEach { case ->
            test(case.name) {
                withSettingsStore {
                    case.configure(authRepository)
                    store.accept(Intent.ChangeApiKey("test-key"))
                    store.accept(Intent.SubmitApiKey)
                    idle()

                    store.state.dialog shouldBe Dialog.Error(case.expected)
                    store.state.loading shouldBe null
                    store.state.apiKey shouldBe "test-key"
                }
            }
        }

        test("DismissDialog clears the error dialog") {
            withSettingsStore {
                authRepository.validateApiKeyFailure = Exception("fail")
                store.accept(Intent.ChangeApiKey("test-key"))
                store.accept(Intent.SubmitApiKey)
                idle()

                store.state.dialog shouldBe Dialog.Error(UiText.Static("fail"))

                store.accept(Intent.DismissDialog)
                idle()

                store.state.dialog shouldBe null
            }
        }
    }

    testSuite("theme") {

        test("ChangeAppTheme with setAppThemeFailure shows error and theme unchanged") {
            withSettingsStore {
                settingsRepository.setAppThemeFailure = Exception("x")
                idle()
                val initialTheme = store.state.appTheme

                store.accept(Intent.ChangeAppTheme(AppTheme.DARK))
                idle()

                store.state.dialog shouldBe Dialog.Error(UiText.Static("x"))
                store.state.appTheme shouldBe initialTheme
            }
        }
    }

    testSuite("signOut") {

        test("RequestSignOut shows confirmation dialog; DismissDialog clears it") {
            withSettingsStore {
                store.accept(Intent.RequestSignOut)
                idle()

                store.state.dialog shouldBe Dialog.SignOutConfirmation

                store.accept(Intent.DismissDialog)
                idle()

                store.state.dialog shouldBe null
            }
        }

        test("ConfirmSignOut closes dialog and sets loading FullScreen immediately") {
            withSettingsStore {
                store.accept(Intent.RequestSignOut)
                idle()

                store.accept(Intent.ConfirmSignOut)

                store.state.dialog shouldBe null
                store.state.loading shouldBe Loading.FullScreen
            }
        }

        test("successful sign-out calls all five repositories and publishes SignedOut once") {
            withSettingsStore {
                val labels = mutableListOf<Label>()
                store.labels.onEach { labels += it }.launchIn(backgroundScope)
                idle()

                store.accept(Intent.RequestSignOut)
                store.accept(Intent.ConfirmSignOut)
                idle()

                authRepository.calls shouldContain "clearApiKey"
                conversationRepository.calls shouldContain "deleteAll"
                catalogRepository.calls shouldContain "deleteAll"
                sessionRepository.calls shouldContain "clear"
                settingsRepository.calls shouldContain "clear"
                labels shouldBe listOf(Label.SignedOut)
            }
        }

        val failureCases = listOf(
            SignOutFailureCase(
                name = "conversationRepository.deleteAllFailure leaves key and shows error",
                configure = { it.conversationRepository.deleteAllFailure = SecureStorageException() },
                checkKey = { it.shouldNotContain("clearApiKey") }
            ),
            SignOutFailureCase(
                name = "catalogRepository.deleteAllFailure leaves key and shows error",
                configure = { it.catalogRepository.deleteAllFailure = SecureStorageException() },
                checkKey = { it.shouldNotContain("clearApiKey") }
            ),
            SignOutFailureCase(
                name = "sessionRepository.clearFailure leaves key and shows error",
                configure = { it.sessionRepository.clearFailure = SecureStorageException() },
                checkKey = { it.shouldNotContain("clearApiKey") }
            ),
            SignOutFailureCase(
                name = "settingsRepository.clearFailure leaves key and shows error",
                configure = { it.settingsRepository.clearFailure = SecureStorageException() },
                checkKey = { it.shouldNotContain("clearApiKey") }
            ),
            SignOutFailureCase(
                name = "authRepository.clearApiKeyFailure shows error after all earlier steps ran",
                configure = { it.authRepository.clearApiKeyFailure = SecureStorageException() },
                checkKey = { it.shouldContain("clearApiKey") }
            )
        )

        failureCases.forEach { case ->
            test(case.name) {
                withSettingsStore {
                    case.configure(this)
                    val labels = mutableListOf<Label>()
                    store.labels.onEach { labels += it }.launchIn(backgroundScope)
                    idle()

                    store.accept(Intent.RequestSignOut)
                    store.accept(Intent.ConfirmSignOut)
                    idle()

                    store.state.loading shouldBe null
                    store.state.dialog shouldBe Dialog.Error(UiText.Resource(Res.string.exception_secure_storage))
                    case.checkKey(authRepository.calls)
                    labels shouldBe emptyList<Label>()
                }
            }
        }
    }

    testSuite("labels") {

        test("NavigateBack publishes NavigatedBack") {
            withSettingsStore {
                val labels = mutableListOf<Label>()
                store.labels.onEach { labels += it }.launchIn(backgroundScope)
                idle()

                store.accept(Intent.NavigateBack)
                idle()

                labels shouldBe listOf(Label.NavigatedBack)
            }
        }
    }

    testSuite("pinnedBehaviors") {

        test("editing key while validation in progress: validation completes with original key") {
            val authRepository = FakeAuthRepository().apply { validateDelay = 1.seconds }
            withSettingsStore(authRepository = authRepository) {
                store.accept(Intent.ChangeApiKey("first"))
                store.accept(Intent.SubmitApiKey)
                advanceBy(500.milliseconds)

                store.accept(Intent.ChangeApiKey("second"))
                idle()

                advanceBy(500.milliseconds)

                authRepository.calls shouldBe listOf("validateApiKey:first", "saveApiKey:first")
                store.state.apiKey shouldBe ""
                store.state.isApiKeySaved shouldBe true
            }
        }

        test("after successful sign-out loading stays FullScreen") {
            withSettingsStore {
                store.accept(Intent.RequestSignOut)
                store.accept(Intent.ConfirmSignOut)
                idle()

                store.state.loading shouldBe Loading.FullScreen

                advanceBy(1.seconds)
                store.state.loading shouldBe Loading.FullScreen
            }
        }
    }
}

private data class ErrorCase(
    val name: String,
    val configure: (FakeAuthRepository) -> Unit,
    val expected: UiText,
)

private data class SignOutFailureCase(
    val name: String,
    val configure: (StoreTestEnv) -> Unit,
    val checkKey: (List<String>) -> Unit,
)
