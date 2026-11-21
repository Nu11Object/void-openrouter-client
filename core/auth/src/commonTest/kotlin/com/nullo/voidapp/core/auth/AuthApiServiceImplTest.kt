package com.nullo.voidapp.core.auth

import com.nullo.voidapp.core.auth.data.dto.ExchangeCodeRequest
import com.nullo.voidapp.core.auth.data.service.AuthApiServiceImpl
import com.nullo.voidapp.core.auth.domain.exception.AuthException.CodeExchangeException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.InternalServerException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.InvalidApiKeyException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.UnexpectedStatusException
import com.nullo.voidapp.core.auth.domain.exception.AuthException.ValidationNetworkException
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException

private class AuthApiServiceImplEnv {
    var status = HttpStatusCode.OK
    var responseBody = """{"key":"exchanged-key"}"""
    var contentType = ContentType.Application.Json
    var engineFailure: Throwable? = null

    val requests = mutableListOf<HttpRequestData>()
    val service = AuthApiServiceImpl(
        testHttpClient { request ->
            requests += request
            engineFailure?.let { throw it }
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, contentType.toString())
            )
        }
    )
}

val AuthApiServiceImplTest by testSuite {
    testFixture { AuthApiServiceImplEnv() } asParameterForEach {
        testSuite("validateApiKey") {
            test("sends GET to key endpoint with Authorization Bearer header") { env ->
                env.service.validateApiKey("test-key")

                val request = env.requests.single()
                request.method.value shouldBe "GET"
                request.url.toString() shouldBe "https://openrouter.ai/api/v1/key"
                request.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }

            test("200 completes without an exception") { env ->
                env.service.validateApiKey("valid-key")
            }

            listOf(
                HttpStatusCode.Unauthorized to InvalidApiKeyException::class,
                HttpStatusCode.InternalServerError to InternalServerException::class
            ).forEach { (status, exceptionClass) ->
                test("${status.value} -> ${exceptionClass.simpleName}") { env ->
                    env.status = status

                    shouldThrowAny { env.service.validateApiKey("test-key") }::class shouldBe exceptionClass
                }
            }

            listOf(
                HttpStatusCode.BadRequest,
                HttpStatusCode.PaymentRequired,
                HttpStatusCode.Forbidden,
                HttpStatusCode.NotFound,
                HttpStatusCode.TooManyRequests,
                HttpStatusCode.BadGateway,
                HttpStatusCode.ServiceUnavailable
            ).forEach { status ->
                test("${status.value} -> UnexpectedStatusException") { env ->
                    env.status = status

                    shouldThrow<UnexpectedStatusException> { env.service.validateApiKey("test-key") }
                }
            }

            test("an IOException from the engine throws ValidationNetworkException with that cause") { env ->
                env.engineFailure = IOException("network down")

                val exception = shouldThrow<ValidationNetworkException> { env.service.validateApiKey("test-key") }

                exception.cause shouldBe instanceOf<IOException>()
            }

            test("a CancellationException from the engine is rethrown unchanged") { env ->
                env.engineFailure = CancellationException("cancelled")

                shouldThrow<CancellationException> { env.service.validateApiKey("test-key") }
            }
        }

        testSuite("exchangeCode") {
            test("a null code throws CodeExchangeException and no request is sent") { env ->
                shouldThrow<CodeExchangeException> { env.service.exchangeCode(null, "verifier") }

                env.requests.isEmpty() shouldBe true
            }

            test("sends POST to auth/keys with code, code_verifier and challenge_method S256") { env ->
                env.service.exchangeCode("auth-code", "verifier")

                val request = env.requests.single()
                request.method.value shouldBe "POST"
                request.url.toString() shouldBe "https://openrouter.ai/api/v1/auth/keys"
                val body = authTestJson.decodeFromString<ExchangeCodeRequest>(
                    request.body.toByteArray().decodeToString()
                )
                body.code shouldBe "auth-code"
                body.codeVerifier shouldBe "verifier"
                body.codeChallengeMethod shouldBe "S256"
            }

            test("200 with the key field returns the key") { env ->
                val key = env.service.exchangeCode("auth-code", "verifier")

                key shouldBe "exchanged-key"
            }

            test("200 with a missing key field throws CodeExchangeException") { env ->
                env.responseBody = "{}"

                shouldThrow<CodeExchangeException> { env.service.exchangeCode("auth-code", "verifier") }
            }

            test("200 with a malformed body throws CodeExchangeException") { env ->
                env.responseBody = "not json"

                shouldThrow<CodeExchangeException> { env.service.exchangeCode("auth-code", "verifier") }
            }

            listOf(
                HttpStatusCode.BadRequest,
                HttpStatusCode.Forbidden,
                HttpStatusCode.InternalServerError
            ).forEach { status ->
                test("${status.value} with a JSON error body throws CodeExchangeException") { env ->
                    env.status = status
                    env.responseBody = """{"error":{"message":"x"}}"""

                    shouldThrow<CodeExchangeException> { env.service.exchangeCode("auth-code", "verifier") }
                }
            }

            test("500 with an HTML body throws CodeExchangeException") { env ->
                env.status = HttpStatusCode.InternalServerError
                env.responseBody = "<html>error</html>"
                env.contentType = ContentType.Text.Html

                shouldThrow<CodeExchangeException> { env.service.exchangeCode("auth-code", "verifier") }
            }

            test("an IOException throws CodeExchangeException with that cause") { env ->
                env.engineFailure = IOException("network down")

                val exception = shouldThrow<CodeExchangeException> { env.service.exchangeCode("auth-code", "verifier") }

                exception.cause shouldBe instanceOf<IOException>()
            }

            test("a CancellationException is rethrown unchanged") { env ->
                env.engineFailure = CancellationException("cancelled")

                shouldThrow<CancellationException> { env.service.exchangeCode("auth-code", "verifier") }
            }
        }
    }
}
