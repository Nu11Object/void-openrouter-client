package com.nullo.voidapp.core.auth

import com.nullo.voidapp.core.auth.data.repository.AuthRepositoryImpl
import com.nullo.voidapp.core.auth.data.service.AuthApiServiceImpl
import com.nullo.voidapp.core.auth.domain.exception.AuthException.InvalidApiKeyException
import com.nullo.voidapp.core.auth.domain.usecase.ValidateAndSaveApiKeyUseCase
import com.nullo.voidapp.core.testing.FakeApiKeyStorage
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first

private class AuthFlowEnv {
    var status = HttpStatusCode.OK

    val storage = FakeApiKeyStorage()
    val requests = mutableListOf<HttpRequestData>()
    private val httpClient: HttpClient = testHttpClient { request ->
        requests += request
        respond(
            content = "{}",
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }
    private val apiService = AuthApiServiceImpl(httpClient)
    val repository = AuthRepositoryImpl(storage, apiService)
    val useCase = ValidateAndSaveApiKeyUseCase(repository)

    fun close() {
        httpClient.close()
    }
}

val AuthFlowTest by testSuite {
    testFixture { AuthFlowEnv() } closeWith { close() } asParameterForEach {
        test("a valid key saves it to storage and hasApiKey returns true") { env ->
            env.useCase("sk-valid")

            env.storage.savedKeys shouldBe listOf("sk-valid")
            env.storage.getApiKey() shouldBe "sk-valid"
            env.repository.hasApiKey().first() shouldBe true
        }

        test("an invalid key throws InvalidApiKeyException and storage stays empty") { env ->
            env.status = HttpStatusCode.Unauthorized

            shouldThrow<InvalidApiKeyException> { env.useCase("invalid-key") }

            env.storage.savedKeys.isEmpty() shouldBe true
        }

        test("the input Bearer sk-1 sends one validation request with the cleaned key") { env ->
            env.useCase("Bearer sk-1")

            env.requests.single().headers[HttpHeaders.Authorization] shouldBe "Bearer sk-1"
        }

        test("hasApiKey is false before saving and true after") { env ->
            env.repository.hasApiKey().first() shouldBe false

            env.useCase("sk-valid")

            env.repository.hasApiKey().first() shouldBe true
        }

        test("clearApiKey removes the key and hasApiKey returns false") { env ->
            env.useCase("sk-valid")
            env.repository.clearApiKey()

            env.repository.getApiKey() shouldBe null
            env.repository.hasApiKey().first() shouldBe false
        }
    }
}
