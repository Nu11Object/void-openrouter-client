package com.nullo.voidapp.feature.settings.domain

import com.nullo.voidapp.core.fakes.FakeAuthRepository
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeConversationRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.fakes.FakeSettingsRepository
import com.nullo.voidapp.core.security.SecureStorageException
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

val SignOutUseCaseTest by testSuite {

    val failureCases = listOf(
        FailureCase(
            name = "failure at conversationRepository.deleteAll leaves key intact and stops subsequent steps",
            configure = { it.conversationRepository.deleteAllFailure = SecureStorageException() },
            checkCalls = {
                it.conversationRepository.calls shouldBe listOf("deleteAll")
                it.catalogRepository.calls shouldBe emptyList()
                it.sessionRepository.calls shouldBe emptyList()
                it.settingsRepository.calls shouldBe emptyList()
                it.authRepository.calls.shouldNotContain("clearApiKey")
            }
        ),
        FailureCase(
            name = "failure at catalogRepository.deleteAll leaves key intact and stops subsequent steps",
            configure = { it.catalogRepository.deleteAllFailure = SecureStorageException() },
            checkCalls = {
                it.conversationRepository.calls shouldBe listOf("deleteAll")
                it.catalogRepository.calls shouldBe listOf("deleteAll")
                it.sessionRepository.calls shouldBe emptyList()
                it.settingsRepository.calls shouldBe emptyList()
                it.authRepository.calls.shouldNotContain("clearApiKey")
            }
        ),
        FailureCase(
            name = "failure at sessionRepository.clear leaves key intact and stops subsequent steps",
            configure = { it.sessionRepository.clearFailure = SecureStorageException() },
            checkCalls = {
                it.conversationRepository.calls shouldBe listOf("deleteAll")
                it.catalogRepository.calls shouldBe listOf("deleteAll")
                it.sessionRepository.calls shouldBe listOf("clear")
                it.settingsRepository.calls shouldBe emptyList()
                it.authRepository.calls.shouldNotContain("clearApiKey")
            }
        ),
        FailureCase(
            name = "failure at settingsRepository.clear leaves key intact and stops subsequent steps",
            configure = { it.settingsRepository.clearFailure = SecureStorageException() },
            checkCalls = {
                it.conversationRepository.calls shouldBe listOf("deleteAll")
                it.catalogRepository.calls shouldBe listOf("deleteAll")
                it.sessionRepository.calls shouldBe listOf("clear")
                it.settingsRepository.calls shouldBe listOf("clear")
                it.authRepository.calls.shouldNotContain("clearApiKey")
            }
        ),
        FailureCase(
            name = "failure at authRepository.clearApiKey runs all earlier steps",
            configure = { it.authRepository.clearApiKeyFailure = SecureStorageException() },
            checkCalls = {
                it.conversationRepository.calls shouldBe listOf("deleteAll")
                it.catalogRepository.calls shouldBe listOf("deleteAll")
                it.sessionRepository.calls shouldBe listOf("clear")
                it.settingsRepository.calls shouldBe listOf("clear")
                it.authRepository.calls.shouldContain("clearApiKey")
            }
        )
    )

    failureCases.forEach { case ->
        test(case.name) {
            val repositories = Repositories().apply(case.configure)
            val useCase = repositories.signOutUseCase()
            shouldThrow<SecureStorageException> { useCase() }
            case.checkCalls(repositories)
        }
    }

    test("success calls every repository once") {
        val repositories = Repositories()
        val useCase = repositories.signOutUseCase()
        useCase()

        repositories.conversationRepository.calls shouldBe listOf("deleteAll")
        repositories.catalogRepository.calls shouldBe listOf("deleteAll")
        repositories.sessionRepository.calls shouldBe listOf("clear")
        repositories.settingsRepository.calls shouldBe listOf("clear")
        repositories.authRepository.calls shouldBe listOf("clearApiKey")
    }
}

private data class FailureCase(
    val name: String,
    val configure: (Repositories) -> Unit,
    val checkCalls: (Repositories) -> Unit,
)

private class Repositories {
    val authRepository = FakeAuthRepository("test-key")
    val conversationRepository = FakeConversationRepository()
    val catalogRepository = FakeCatalogRepository()
    val sessionRepository = FakeSessionRepository()
    val settingsRepository = FakeSettingsRepository()

    fun signOutUseCase() = SignOutUseCase(
        authRepository = authRepository,
        conversationRepository = conversationRepository,
        catalogRepository = catalogRepository,
        sessionRepository = sessionRepository,
        settingsRepository = settingsRepository
    )
}
