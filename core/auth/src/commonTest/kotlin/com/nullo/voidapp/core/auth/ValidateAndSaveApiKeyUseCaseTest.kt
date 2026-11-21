package com.nullo.voidapp.core.auth

import com.nullo.voidapp.core.auth.domain.exception.AuthException.InternalServerException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.InvalidApiKeyException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.UnexpectedStatusException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.ValidationNetworkException
import com.nullo.voidapp.core.auth.domain.usecase.ValidateAndSaveApiKeyUseCase
import com.nullo.voidapp.core.security.SecureStorageException
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe

private class ValidateAndSaveApiKeyUseCaseEnv {
    val fakeRepository = FakeAuthRepository()
    val useCase = ValidateAndSaveApiKeyUseCase(fakeRepository)
}

val ValidateAndSaveApiKeyUseCaseTest by testSuite {
    testFixture { ValidateAndSaveApiKeyUseCaseEnv() } asParameterForEach {
        test("validates first and then saves") { env ->
            env.useCase("valid-key")

            env.fakeRepository.calls shouldBe listOf("validate:valid-key", "save:valid-key")
        }

        test("cleans the key before both calls") { env ->
            env.useCase("  Bearer sk-1  ")

            env.fakeRepository.calls shouldBe listOf("validate:sk-1", "save:sk-1")
        }

        listOf(
            InvalidApiKeyException(),
            ValidationNetworkException(),
            InternalServerException(),
            UnexpectedStatusException()
        ).forEach { exception ->
            test("a ${exception::class.simpleName} from validation propagates and save is not called") { env ->
                env.fakeRepository.validateFailure = exception

                shouldThrowAny { env.useCase("valid-key") }::class shouldBe exception::class

                env.fakeRepository.calls shouldBe listOf("validate:valid-key")
            }
        }

        test("a SecureStorageException from saving after successful validation propagates") { env ->
            env.fakeRepository.saveFailure = SecureStorageException()

            shouldThrow<SecureStorageException> { env.useCase("valid-key") }

            env.fakeRepository.calls shouldBe listOf("validate:valid-key", "save:valid-key")
        }

        test("a blank key is validated and saved as an empty string") { env ->
            env.useCase("   ")

            env.fakeRepository.calls shouldBe listOf("validate:", "save:")
        }
    }
}
