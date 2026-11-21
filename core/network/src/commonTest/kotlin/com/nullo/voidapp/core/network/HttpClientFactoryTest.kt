package com.nullo.voidapp.core.network

import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.core.testing.FakeApiKeyStorage
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val testJson = Json { ignoreUnknownKeys = true }

@Serializable
private class FactoryPayload(val value: String)

private class HttpClientFactoryEnv {
    val storage = FakeApiKeyStorage()
    val requests = mutableListOf<HttpRequestData>()
    val client = HttpClientFactory(storage, testJson).create(
        MockEngine { request ->
            requests += request
            respond(
                content = """{"value":"ok","unknown":true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    )
}

val HttpClientFactoryTest by testSuite {
    testFixture { HttpClientFactoryEnv() } closeWith { client.close() } asParameterForEach {
        testSuite("authorization") {
            test("a stored key is sent as an Authorization Bearer header on a GET request") { env ->
                env.storage.saveApiKey("secret-key")

                env.client.get("models")

                env.requests.single().headers[HttpHeaders.Authorization] shouldBe "Bearer secret-key"
            }

            test("the key is read on every request so a newly saved key is used") { env ->
                env.storage.saveApiKey("first-key")
                env.client.get("models")
                env.storage.saveApiKey("second-key")
                env.client.get("models")

                env.requests.map { it.headers[HttpHeaders.Authorization] } shouldBe
                    listOf("Bearer first-key", "Bearer second-key")
            }

            test("after clear the next request has no Authorization header") { env ->
                env.storage.saveApiKey("secret-key")
                env.client.get("models")
                env.storage.clear()
                env.client.get("models")

                env.requests.map { it.headers[HttpHeaders.Authorization] } shouldBe listOf("Bearer secret-key", null)
            }

            test("without a stored key the request has no Authorization header and succeeds") { env ->
                val response = env.client.get("models")

                response.status shouldBe HttpStatusCode.OK
                env.requests.single().headers[HttpHeaders.Authorization] shouldBe null
            }

            test("an explicit Authorization header is kept and the storage is not read") { env ->
                env.storage.saveApiKey("stored-key")

                env.client.get("models") {
                    header(HttpHeaders.Authorization, "Custom token")
                }

                env.requests.single().headers[HttpHeaders.Authorization] shouldBe "Custom token"
                env.storage.getCalls shouldBe 0
            }

            listOf(
                SecureStorageException(),
                IllegalStateException("storage unavailable")
            ).forEach { failure ->
                test(
                    "a ${failure::class.simpleName} from getApiKey " +
                        "sends the request without an Authorization header"
                ) { env ->
                    env.storage.getFailure = failure

                    val response = env.client.get("models")

                    env.requests.single().headers[HttpHeaders.Authorization] shouldBe null
                    response.status shouldBe HttpStatusCode.OK
                }
            }
        }

        testSuite("defaults") {
            test("a relative models URL is resolved against the base URL") { env ->
                env.client.get("models")

                env.requests.single().url.toString() shouldBe "https://openrouter.ai/api/v1/models"
            }

            test("a POST request carries the application/json content type") { env ->
                env.client.post("models") {
                    setBody("""{"value":"payload"}""")
                }

                env.requests.single().body.contentType shouldBe ContentType.Application.Json
            }

            test("a JSON response is parsed into the requested type") { env ->
                val payload = env.client.get("models").body<FactoryPayload>()

                payload.value shouldBe "ok"
            }
        }
    }
}
