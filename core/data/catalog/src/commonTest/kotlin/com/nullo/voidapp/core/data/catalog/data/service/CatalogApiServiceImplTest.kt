package com.nullo.voidapp.core.data.catalog.data.service

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.data.catalog.data.service.exception.InternalServerException
import com.nullo.voidapp.core.data.catalog.data.service.exception.NetworkException
import com.nullo.voidapp.core.model.imagemodel.PricingUnit
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException

private fun catalogApi(handler: MockRequestHandler) = CatalogApiServiceImpl(testHttpClient(handler))

private val CLIENT_ERROR_STATUSES = listOf(
    HttpStatusCode.BadRequest,
    HttpStatusCode.Unauthorized,
    HttpStatusCode.PaymentRequired,
    HttpStatusCode.Forbidden,
    HttpStatusCode.NotFound,
    HttpStatusCode.TooManyRequests
)

private val SERVER_ERROR_STATUSES = listOf(
    HttpStatusCode.InternalServerError,
    HttpStatusCode.BadGateway,
    HttpStatusCode.ServiceUnavailable,
    HttpStatusCode.GatewayTimeout
)

private val SAMPLE_CLIENT_ERROR_STATUSES = listOf(
    HttpStatusCode.Unauthorized,
    HttpStatusCode.TooManyRequests
)

private val SAMPLE_SERVER_ERROR_STATUSES = listOf(
    HttpStatusCode.InternalServerError,
    HttpStatusCode.ServiceUnavailable
)

private val CHAT_MODELS_JSON = """
    {
      "data": [
        {
          "id": "acme/chat-a",
          "name": "Chat A",
          "pricing": {"prompt": "0.000003", "completion": "0.00001"}
        },
        {
          "id": "acme/chat-b",
          "name": "Chat B",
          "pricing": {"prompt": "0.000001", "completion": "0"}
        }
      ]
    }
""".trimIndent()

private val IMAGE_MODELS_JSON = """
    {
      "data": [
        {
          "id": "acme/image-a",
          "name": "Image A",
          "description": "Image model",
          "created": 1,
          "architecture": {
            "input_modalities": ["text"],
            "output_modalities": ["image"]
          },
          "supports_streaming": false,
          "endpoints": "images/models/acme/image-a/endpoints"
        }
      ]
    }
""".trimIndent()

private val UNKNOWN_CAPABILITY_IMAGE_MODELS_JSON = """
    {
      "data": [
        {
          "id": "acme/image-unknown",
          "name": "Unknown Capability",
          "description": "Image model",
          "created": 1,
          "architecture": {},
          "supported_parameters": {"quality": {"type": "video"}},
          "supports_streaming": false,
          "endpoints": "images/models/acme/image-unknown/endpoints"
        }
      ]
    }
""".trimIndent()

private val IMAGE_PRICING_JSON = """
    {
      "id": "acme/model-x",
      "endpoints": [
        {
          "provider_name": "Acme",
          "provider_slug": "acme",
          "pricing": [
            {"billable": "image", "unit": "image", "cost_usd": 0.04},
            {"billable": "input_token", "unit": "token", "cost_usd": 0.0001, "variant": "standard"}
          ]
        }
      ]
    }
""".trimIndent()

private fun errorJson(code: Int) = """{"error":{"message":"x","code":$code}}"""

val CatalogApiServiceImplTest by testSuite {
    testSuite("fetchChatModels") {
        test("sends GET to the chat models endpoint") {
            val requests = mutableListOf<HttpRequestData>()
            val api = catalogApi { request ->
                requests += request
                respondJson(CHAT_MODELS_JSON)
            }

            api.fetchChatModels()

            requests.single().method shouldBe HttpMethod.Get
            requests.single().url.toString() shouldBe "https://openrouter.ai/api/v1/models"
        }

        test("returns models in order with prices scaled per million") {
            val models = catalogApi { respondJson(CHAT_MODELS_JSON) }.fetchChatModels()

            models.map { it.id } shouldBe listOf("acme/chat-a", "acme/chat-b")
            models[0].pricing.promptPerMillion.compareTo("3".toBigDecimal()) shouldBe 0
            models[0].pricing.completionPerMillion.compareTo("10".toBigDecimal()) shouldBe 0
        }

        test("returns an empty list for an empty data array") {
            val models = catalogApi { respondJson("""{"data":[]}""") }.fetchChatModels()

            models shouldBe emptyList()
        }

        listOf(
            "{}",
            """{"error":{"message":"x","code":200}}"""
        ).forEach { body ->
            test("a 200 body $body fails with NetworkException") {
                val api = catalogApi { respondJson(body) }

                shouldThrow<NetworkException> { api.fetchChatModels() }
            }
        }

        test("a malformed body fails with NetworkException") {
            val api = catalogApi { respondJson("not json") }

            shouldThrow<NetworkException> { api.fetchChatModels() }
        }

        CLIENT_ERROR_STATUSES.forEach { status ->
            test("status ${status.value} fails with NetworkException") {
                val api = catalogApi { respondJson(errorJson(status.value), status) }

                shouldThrow<NetworkException> { api.fetchChatModels() }
            }
        }

        SERVER_ERROR_STATUSES.forEach { status ->
            test("status ${status.value} fails with InternalServerException") {
                val api = catalogApi { respondJson(errorJson(status.value), status) }

                shouldThrow<InternalServerException> { api.fetchChatModels() }
            }
        }

        test("an IOException fails with NetworkException") {
            val api = catalogApi { throw IOException("boom") }

            shouldThrow<NetworkException> { api.fetchChatModels() }
        }

        test("a CancellationException is rethrown unchanged") {
            val cancellation = CancellationException("cancelled")
            val api = catalogApi { throw cancellation }

            shouldThrow<CancellationException> { api.fetchChatModels() } shouldBe cancellation
        }
    }

    testSuite("fetchImageModels") {
        test("sends GET to the image models endpoint") {
            val requests = mutableListOf<HttpRequestData>()
            val api = catalogApi { request ->
                requests += request
                respondJson(IMAGE_MODELS_JSON)
            }

            api.fetchImageModels()

            requests.single().method shouldBe HttpMethod.Get
            requests.single().url.toString() shouldBe "https://openrouter.ai/api/v1/images/models"
        }

        test("returns the mapped image model") {
            val models = catalogApi { respondJson(IMAGE_MODELS_JSON) }.fetchImageModels()

            models.single().id shouldBe "acme/image-a"
            models.single().name shouldBe "Image A"
            models.single().endpointsUrl shouldBe "images/models/acme/image-a/endpoints"
        }

        test("returns an empty list for an empty data array") {
            val models = catalogApi { respondJson("""{"data":[]}""") }.fetchImageModels()

            models shouldBe emptyList()
        }

        test("a malformed body fails with NetworkException") {
            val api = catalogApi { respondJson("not json") }

            shouldThrow<NetworkException> { api.fetchImageModels() }
        }

        SAMPLE_CLIENT_ERROR_STATUSES.forEach { status ->
            test("status ${status.value} fails with NetworkException") {
                val api = catalogApi { respondJson(errorJson(status.value), status) }

                shouldThrow<NetworkException> { api.fetchImageModels() }
            }
        }

        SAMPLE_SERVER_ERROR_STATUSES.forEach { status ->
            test("status ${status.value} fails with InternalServerException") {
                val api = catalogApi { respondJson(errorJson(status.value), status) }

                shouldThrow<InternalServerException> { api.fetchImageModels() }
            }
        }

        test("an IOException fails with NetworkException") {
            val api = catalogApi { throw IOException("boom") }

            shouldThrow<NetworkException> { api.fetchImageModels() }
        }

        test("a CancellationException is rethrown unchanged") {
            val cancellation = CancellationException("cancelled")
            val api = catalogApi { throw cancellation }

            shouldThrow<CancellationException> { api.fetchImageModels() } shouldBe cancellation
        }

        test("an unknown capability type fails with NetworkException") {
            val api = catalogApi { respondJson(UNKNOWN_CAPABILITY_IMAGE_MODELS_JSON) }

            shouldThrow<NetworkException> { api.fetchImageModels() }
        }
    }

    testSuite("fetchImageModelPricing") {
        listOf(
            "https://openrouter.ai/api/v1/images/models/acme/model-x/endpoints",
            "/images/models/acme/model-x/endpoints",
            "images/models/acme/model-x/endpoints"
        ).forEach { endpointsUrl ->
            test("resolves \"$endpointsUrl\" to the images models path") {
                val requests = mutableListOf<HttpRequestData>()
                val api = catalogApi { request ->
                    requests += request
                    respondJson(IMAGE_PRICING_JSON)
                }

                api.fetchImageModelPricing(endpointsUrl)

                requests.single().url.encodedPath shouldBe
                    "/api/v1/images/models/acme/model-x/endpoints"
            }
        }

        test("an endpoints URL without the images models segment fails with NetworkException") {
            val api = catalogApi { respondJson(IMAGE_PRICING_JSON) }

            shouldThrow<NetworkException> {
                api.fetchImageModelPricing("https://openrouter.ai/api/v1/models/acme/model-x/endpoints")
            }
        }

        test("returns the mapped pricing list") {
            val pricing = catalogApi { respondJson(IMAGE_PRICING_JSON) }
                .fetchImageModelPricing("images/models/acme/model-x/endpoints")

            pricing.single().providerName shouldBe "Acme"
            pricing.single().providerSlug shouldBe "acme"
            pricing.single().lines.size shouldBe 2
            pricing.single().lines[0].unit shouldBe PricingUnit.IMAGE
            pricing.single().lines[0].costUsd.compareTo("0.04".toBigDecimal()) shouldBe 0
            pricing.single().lines[0].variant shouldBe null
            pricing.single().lines[1].unit shouldBe PricingUnit.TOKEN
            pricing.single().lines[1].costUsd.compareTo("0.0001".toBigDecimal()) shouldBe 0
            pricing.single().lines[1].variant shouldBe "standard"
        }

        SAMPLE_CLIENT_ERROR_STATUSES.forEach { status ->
            test("status ${status.value} fails with NetworkException") {
                val api = catalogApi { respondJson(errorJson(status.value), status) }

                shouldThrow<NetworkException> {
                    api.fetchImageModelPricing("images/models/acme/model-x/endpoints")
                }
            }
        }

        SAMPLE_SERVER_ERROR_STATUSES.forEach { status ->
            test("status ${status.value} fails with InternalServerException") {
                val api = catalogApi { respondJson(errorJson(status.value), status) }

                shouldThrow<InternalServerException> {
                    api.fetchImageModelPricing("images/models/acme/model-x/endpoints")
                }
            }
        }

        test("a malformed body fails with NetworkException") {
            val api = catalogApi { respondJson("not json") }

            shouldThrow<NetworkException> {
                api.fetchImageModelPricing("images/models/acme/model-x/endpoints")
            }
        }

        test("an IOException fails with NetworkException") {
            val api = catalogApi { throw IOException("boom") }

            shouldThrow<NetworkException> {
                api.fetchImageModelPricing("images/models/acme/model-x/endpoints")
            }
        }

        test("a CancellationException is rethrown unchanged") {
            val cancellation = CancellationException("cancelled")
            val api = catalogApi { throw cancellation }

            shouldThrow<CancellationException> {
                api.fetchImageModelPricing("images/models/acme/model-x/endpoints")
            } shouldBe cancellation
        }
    }
}
