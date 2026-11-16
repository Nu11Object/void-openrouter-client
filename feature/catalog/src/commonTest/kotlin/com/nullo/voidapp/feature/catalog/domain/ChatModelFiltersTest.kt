package com.nullo.voidapp.feature.catalog.domain

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.testing.chatModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

private fun pricedModel(input: Int, output: Int) = chatModel(
    pricing = ModelPricing(
        promptPerMillion = input.toBigDecimal(),
        completionPerMillion = output.toBigDecimal()
    )
)

val ChatModelFiltersTest by testSuite {
    testSuite("matches") {
        test("pinnedOnly rejects unpinned models") {
            val filters = ChatModelFilters(pinnedOnly = true)
            chatModel(pinned = false).matches(filters) shouldBe false
            chatModel(pinned = true).matches(filters) shouldBe true
        }

        listOf(0 to false, 1 to true, 2 to true, 3 to true, 4 to false).forEach { (input, accepted) ->
            test("input price $input with range 1..3 is ${if (accepted) "accepted" else "rejected"}") {
                val filters = ChatModelFilters(inputPrice = PriceRange(min = 1.toBigDecimal(), max = 3.toBigDecimal()))
                pricedModel(input, 0).matches(filters) shouldBe accepted
            }
        }

        test("freeOnly ignores price ranges") {
            val filters = ChatModelFilters(
                freeOnly = true,
                inputPrice = PriceRange(1.toBigDecimal(), 1.toBigDecimal()),
                outputPrice = PriceRange(1.toBigDecimal(), 1.toBigDecimal())
            )
            pricedModel(0, 0).matches(filters) shouldBe true
        }

        test("model without context length is rejected by minContextLength") {
            val filters = ChatModelFilters(minContextLength = 256_000)
            chatModel(contextLength = null).matches(filters) shouldBe false
        }

        listOf(0 to false, 1 to true, 2 to true, 3 to true, 4 to false).forEach { (output, accepted) ->
            test("output price $output with range 1..3 is ${if (accepted) "accepted" else "rejected"}") {
                val filters = ChatModelFilters(
                    outputPrice = PriceRange(min = 1.toBigDecimal(), max = 3.toBigDecimal())
                )
                pricedModel(0, output).matches(filters) shouldBe accepted
            }
        }

        test("input price range with only min accepts prices at or above min") {
            val filters = ChatModelFilters(inputPrice = PriceRange(min = 1.toBigDecimal()))
            pricedModel(1, 0).matches(filters) shouldBe true
            pricedModel(2, 0).matches(filters) shouldBe true
            pricedModel(0, 0).matches(filters) shouldBe false
        }

        test("input price range with only max accepts prices at or below max") {
            val filters = ChatModelFilters(inputPrice = PriceRange(max = 3.toBigDecimal()))
            pricedModel(3, 0).matches(filters) shouldBe true
            pricedModel(1, 0).matches(filters) shouldBe true
            pricedModel(4, 0).matches(filters) shouldBe false
        }

        test("freeOnly rejects a paid model") {
            pricedModel(1, 1).matches(ChatModelFilters(freeOnly = true)) shouldBe false
        }

        test("negative input price passes when the range is unbounded") {
            pricedModel(-1, -1).matches(ChatModelFilters(inputPrice = PriceRange())) shouldBe true
        }

        test("negative input price is rejected when the range is bounded") {
            val filters = ChatModelFilters(
                inputPrice = PriceRange(min = 0.toBigDecimal(), max = 100.toBigDecimal())
            )
            pricedModel(-1, -1).matches(filters) shouldBe false
        }

        test("negative output price passes when the range is unbounded") {
            pricedModel(-1, -1).matches(ChatModelFilters(outputPrice = PriceRange())) shouldBe true
        }

        test("negative output price is rejected when the range is bounded") {
            val filters = ChatModelFilters(
                outputPrice = PriceRange(min = 0.toBigDecimal(), max = 100.toBigDecimal())
            )
            pricedModel(-1, -1).matches(filters) shouldBe false
        }

        test("minContextLength accepts the exact boundary") {
            chatModel(contextLength = 256_000)
                .matches(ChatModelFilters(minContextLength = 256_000)) shouldBe true
        }

        test("minContextLength rejects a value below the boundary") {
            chatModel(contextLength = 255_999)
                .matches(ChatModelFilters(minContextLength = 256_000)) shouldBe false
        }

        test("minContextLength accepts a value above the boundary") {
            chatModel(contextLength = 256_001)
                .matches(ChatModelFilters(minContextLength = 256_000)) shouldBe true
        }

        test("required features match a model with all required features") {
            chatModel(supportedFeatures = setOf(ModelFeature.THINKING))
                .matches(ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING))) shouldBe true
        }

        test("required features reject a model missing a required feature") {
            chatModel(supportedFeatures = emptySet())
                .matches(ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING))) shouldBe false
        }

        test("required input modalities match a model with all required modalities") {
            chatModel(inputModalities = setOf(Modality.TEXT, Modality.IMAGE))
                .matches(ChatModelFilters(requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE))) shouldBe true
        }

        test("required input modalities reject a model missing a required modality") {
            val filters = ChatModelFilters(
                requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE)
            )
            chatModel(inputModalities = setOf(Modality.TEXT)).matches(filters) shouldBe false
        }

        test("required output modalities match a model with all required modalities") {
            chatModel(outputModalities = setOf(Modality.TEXT, Modality.IMAGE))
                .matches(
                    ChatModelFilters(
                        requiredOutputModalities = setOf(Modality.TEXT, Modality.IMAGE)
                    )
                ) shouldBe true
        }

        test("required output modalities reject a model missing a required modality") {
            val filters = ChatModelFilters(
                requiredOutputModalities = setOf(Modality.TEXT, Modality.IMAGE)
            )
            chatModel(outputModalities = setOf(Modality.TEXT)).matches(filters) shouldBe false
        }

        test("input modalities do not satisfy required output modalities") {
            chatModel(inputModalities = setOf(Modality.IMAGE), outputModalities = emptySet())
                .matches(ChatModelFilters(requiredOutputModalities = setOf(Modality.IMAGE))) shouldBe false
        }

        test("a model matching two filters separately matches both together") {
            val model = chatModel(
                inputModalities = setOf(Modality.TEXT),
                supportedFeatures = setOf(ModelFeature.THINKING)
            )
            val featureFilter = ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING))
            val modalityFilter = ChatModelFilters(requiredInputModalities = setOf(Modality.TEXT))
            val combinedFilter = ChatModelFilters(
                requiredFeatures = setOf(ModelFeature.THINKING),
                requiredInputModalities = setOf(Modality.TEXT)
            )

            model.matches(featureFilter) shouldBe true
            model.matches(modalityFilter) shouldBe true
            model.matches(combinedFilter) shouldBe true
        }

        test("a model failing one of two filters does not match both together") {
            val model = chatModel(
                inputModalities = emptySet(),
                supportedFeatures = setOf(ModelFeature.THINKING)
            )
            val featureFilter = ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING))
            val modalityFilter = ChatModelFilters(requiredInputModalities = setOf(Modality.TEXT))
            val combinedFilter = ChatModelFilters(
                requiredFeatures = setOf(ModelFeature.THINKING),
                requiredInputModalities = setOf(Modality.TEXT)
            )

            model.matches(featureFilter) shouldBe true
            model.matches(modalityFilter) shouldBe false
            model.matches(combinedFilter) shouldBe false
        }
    }

    testSuite("applyFilters") {
        test("default filters return the original list instance") {
            val filters = ChatModelFilters.Default
            val modelsList = listOf(chatModel(id = "0"), chatModel(id = "1"))
            modelsList.applyFilters(filters) shouldBeSameInstanceAs modelsList
        }

        test("non-default filters keep matching models in their original order") {
            val modelsList = listOf(
                chatModel(id = "first", pinned = true),
                chatModel(id = "second", pinned = false),
                chatModel(id = "third", pinned = true)
            )

            modelsList.applyFilters(ChatModelFilters(pinnedOnly = true)).map { it.id } shouldBe
                listOf("first", "third")
        }

        test("filters that match nothing return an empty list") {
            val modelsList = listOf(chatModel(id = "0"), chatModel(id = "1"))

            modelsList.applyFilters(ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING))) shouldBe
                emptyList()
        }
    }

    testSuite("activeCount") {
        test("freeOnly and pinnedOnly count as two") {
            val filters = ChatModelFilters(freeOnly = true, pinnedOnly = true)
            filters.activeCount shouldBe 2
        }

        test("minContextLength counts as one") {
            ChatModelFilters(minContextLength = 256_000).activeCount shouldBe 1
        }

        test("each required feature counts as one") {
            ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING)).activeCount shouldBe 1
        }

        test("each required input and output modality counts as one") {
            val filters = ChatModelFilters(
                requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                requiredOutputModalities = setOf(Modality.TEXT)
            )
            filters.activeCount shouldBe 3
        }

        test("price ranges are counted when freeOnly is false") {
            val filters = ChatModelFilters(
                inputPrice = PriceRange(min = 0.toBigDecimal(), max = 1.toBigDecimal()),
                outputPrice = PriceRange(min = 0.toBigDecimal(), max = 1.toBigDecimal())
            )
            filters.activeCount shouldBe 2
        }

        test("price ranges are not counted when freeOnly is true") {
            val filters = ChatModelFilters(
                freeOnly = true,
                inputPrice = PriceRange(min = 0.toBigDecimal(), max = 1.toBigDecimal()),
                outputPrice = PriceRange(min = 0.toBigDecimal(), max = 1.toBigDecimal())
            )
            filters.activeCount shouldBe 1
        }
    }

    testSuite("isDefault") {
        test("default filters are default") {
            ChatModelFilters().isDefault shouldBe true
        }

        listOf(
            "freeOnly" to ChatModelFilters(freeOnly = true),
            "pinnedOnly" to ChatModelFilters(pinnedOnly = true),
            "inputPrice" to ChatModelFilters(inputPrice = PriceRange(min = 0.toBigDecimal())),
            "outputPrice" to ChatModelFilters(outputPrice = PriceRange(min = 0.toBigDecimal())),
            "minContextLength" to ChatModelFilters(minContextLength = 0),
            "requiredFeatures" to ChatModelFilters(requiredFeatures = setOf(ModelFeature.THINKING)),
            "requiredInputModalities" to ChatModelFilters(requiredInputModalities = setOf(Modality.TEXT)),
            "requiredOutputModalities" to ChatModelFilters(requiredOutputModalities = setOf(Modality.TEXT))
        ).forEach { (field, filters) ->
            test("changed $field makes isDefault false") {
                filters.isDefault shouldBe false
            }
        }
    }
}
