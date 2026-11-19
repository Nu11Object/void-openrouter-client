package com.nullo.voidapp.core.data.catalog.data.mapper

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.data.catalog.catalogTestJson
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImageEndpointDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImagePricingEntryDto
import com.nullo.voidapp.core.data.catalog.data.service.dto.ImagePricingResponseDto
import com.nullo.voidapp.core.model.imagemodel.PricingUnit
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString

private fun imagePricingEntryDto(
    billable: String = "image",
    unit: String = "image",
    costUsd: Double = 0.04,
    variant: String? = null,
) = ImagePricingEntryDto(
    billable = billable,
    unit = unit,
    costUsd = costUsd,
    variant = variant
)

private fun imageEndpointDto(
    providerName: String = "Provider",
    providerSlug: String = "provider",
    pricing: List<ImagePricingEntryDto> = emptyList(),
) = ImageEndpointDto(
    providerName = providerName,
    providerSlug = providerSlug,
    pricing = pricing
)

private fun imagePricingResponseDto(
    id: String = "test/image-model",
    endpoints: List<ImageEndpointDto> = emptyList(),
) = ImagePricingResponseDto(
    id = id,
    endpoints = endpoints
)

private val IMAGE_PRICING_JSON = """
    {
      "id": "google/imagen-4",
      "endpoints": [
        {
          "provider_name": "Google",
          "provider_slug": "google",
          "pricing": [
            {"billable": "image", "unit": "image", "cost_usd": 0.04},
            {
              "billable": "input_token",
              "unit": "token",
              "cost_usd": 0.002,
              "variant": "standard"
            },
            {"billable": "output_megapixel", "unit": "megapixel", "cost_usd": 0.0001}
          ],
          "unknown_field": "ignored"
        }
      ]
    }
""".trimIndent()

val ImagePriceMapperTest by testSuite {
    testSuite("endpoints") {
        test("maps endpoints in order with provider fields and all lines") {
            val pricing = imagePricingResponseDto(
                endpoints = listOf(
                    imageEndpointDto(
                        providerName = "Google",
                        providerSlug = "google",
                        pricing = listOf(
                            imagePricingEntryDto(
                                billable = "image",
                                unit = "image",
                                costUsd = 0.04,
                                variant = null
                            ),
                            imagePricingEntryDto(
                                billable = "input_token",
                                unit = "token",
                                costUsd = 0.002,
                                variant = "standard"
                            )
                        )
                    ),
                    imageEndpointDto(
                        providerName = "OpenAI",
                        providerSlug = "openai",
                        pricing = listOf(imagePricingEntryDto(billable = "request", unit = "request"))
                    )
                )
            ).toDomain()

            pricing.size shouldBe 2
            pricing[0].providerName shouldBe "Google"
            pricing[0].providerSlug shouldBe "google"
            pricing[0].lines.size shouldBe 2
            pricing[0].lines[0].billable shouldBe "image"
            pricing[0].lines[0].unit shouldBe PricingUnit.IMAGE
            pricing[0].lines[0].costUsd.compareTo("0.04".toBigDecimal()) shouldBe 0
            pricing[0].lines[0].variant shouldBe null
            pricing[0].lines[1].billable shouldBe "input_token"
            pricing[0].lines[1].unit shouldBe PricingUnit.TOKEN
            pricing[0].lines[1].costUsd.compareTo("0.002".toBigDecimal()) shouldBe 0
            pricing[0].lines[1].variant shouldBe "standard"
            pricing[1].providerName shouldBe "OpenAI"
            pricing[1].providerSlug shouldBe "openai"
            pricing[1].lines.single().billable shouldBe "request"
            pricing[1].lines.single().unit shouldBe PricingUnit.REQUEST
        }

        test("empty endpoints give an empty list") {
            imagePricingResponseDto(endpoints = emptyList()).toDomain() shouldBe emptyList()
        }

        test("an endpoint with an empty pricing list gives empty lines") {
            val pricing = imagePricingResponseDto(
                endpoints = listOf(imageEndpointDto(providerName = "Google", providerSlug = "google"))
            ).toDomain()

            pricing.single().lines shouldBe emptyList()
        }
    }

    testSuite("units") {
        listOf(
            "request" to PricingUnit.REQUEST,
            "image" to PricingUnit.IMAGE,
            "megapixel" to PricingUnit.MEGAPIXEL,
            "token" to PricingUnit.TOKEN,
            "IMAGE" to PricingUnit.IMAGE,
            "unknown" to PricingUnit.OTHER
        ).forEach { (raw, expected) ->
            test("unit \"$raw\" maps to $expected") {
                val line = imagePricingResponseDto(
                    endpoints = listOf(
                        imageEndpointDto(
                            pricing = listOf(imagePricingEntryDto(unit = raw, costUsd = 0.0))
                        )
                    )
                ).toDomain().single().lines.single()

                line.unit shouldBe expected
            }
        }
    }

    testSuite("cost conversion") {
        listOf(
            0.04 to "0.04",
            0.0001 to "0.0001",
            0.00001 to "0.00001",
            0.0000001 to "0.0000001",
            12_345_678.9 to "12345678.9",
            1.0E10 to "10000000000",
            0.0 to "0",
            -1.0 to "-1"
        ).forEach { (raw, expected) ->
            test("cost $raw converts to $expected") {
                val line = imagePricingResponseDto(
                    endpoints = listOf(
                        imageEndpointDto(
                            pricing = listOf(imagePricingEntryDto(costUsd = raw))
                        )
                    )
                ).toDomain().single().lines.single()

                line.costUsd.compareTo(expected.toBigDecimal()) shouldBe 0
            }
        }
    }

    testSuite("json") {
        test("decodes a pricing response") {
            val pricing = catalogTestJson
                .decodeFromString<ImagePricingResponseDto>(IMAGE_PRICING_JSON)
                .toDomain()

            pricing.size shouldBe 1
            pricing.single().providerName shouldBe "Google"
            pricing.single().providerSlug shouldBe "google"
            pricing.single().lines.map { it.billable } shouldBe
                listOf("image", "input_token", "output_megapixel")
            pricing.single().lines.map { it.unit } shouldBe
                listOf(PricingUnit.IMAGE, PricingUnit.TOKEN, PricingUnit.MEGAPIXEL)
            pricing.single().lines.map { it.variant } shouldBe listOf(null, "standard", null)
            pricing.single().lines[0].costUsd.compareTo("0.04".toBigDecimal()) shouldBe 0
            pricing.single().lines[1].costUsd.compareTo("0.002".toBigDecimal()) shouldBe 0
            pricing.single().lines[2].costUsd.compareTo("0.0001".toBigDecimal()) shouldBe 0
        }
    }
}
