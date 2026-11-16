package com.nullo.voidapp.feature.catalog.domain

import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.testing.imageModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

val ImageModelFiltersTest by testSuite {
    testSuite("matches") {
        test("pinnedOnly accepts pinned models and rejects unpinned ones") {
            val filters = ImageModelFilters(pinnedOnly = true)

            imageModel(pinned = false).matches(filters) shouldBe false
            imageModel(pinned = true).matches(filters) shouldBe true
        }

        test("required input modalities match a model with all required modalities") {
            imageModel(inputModalities = setOf(Modality.TEXT, Modality.IMAGE))
                .matches(
                    ImageModelFilters(
                        requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE)
                    )
                ) shouldBe true
        }

        test("required input modalities reject a model missing a required modality") {
            val filters = ImageModelFilters(
                requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE)
            )

            imageModel(inputModalities = setOf(Modality.TEXT)).matches(filters) shouldBe false
        }

        test("output modalities do not satisfy required input modalities") {
            val model = imageModel(
                inputModalities = emptySet(),
                outputModalities = setOf(Modality.IMAGE)
            )

            model.matches(ImageModelFilters(requiredInputModalities = setOf(Modality.IMAGE))) shouldBe false
        }

        test("default filters match any model") {
            val filters = ImageModelFilters()

            imageModel().matches(filters) shouldBe true
            imageModel(pinned = true, inputModalities = setOf(Modality.IMAGE)).matches(filters) shouldBe true
        }

        test("a model matching pinnedOnly and input modalities matches both together") {
            val model = imageModel(pinned = true, inputModalities = setOf(Modality.IMAGE))
            val pinnedFilter = ImageModelFilters(pinnedOnly = true)
            val modalityFilter = ImageModelFilters(requiredInputModalities = setOf(Modality.IMAGE))
            val combinedFilter = ImageModelFilters(
                pinnedOnly = true,
                requiredInputModalities = setOf(Modality.IMAGE)
            )

            model.matches(pinnedFilter) shouldBe true
            model.matches(modalityFilter) shouldBe true
            model.matches(combinedFilter) shouldBe true
        }

        test("a model failing one of two filters does not match both together") {
            val model = imageModel(pinned = false, inputModalities = setOf(Modality.IMAGE))
            val pinnedFilter = ImageModelFilters(pinnedOnly = true)
            val modalityFilter = ImageModelFilters(requiredInputModalities = setOf(Modality.IMAGE))
            val combinedFilter = ImageModelFilters(
                pinnedOnly = true,
                requiredInputModalities = setOf(Modality.IMAGE)
            )

            model.matches(pinnedFilter) shouldBe false
            model.matches(modalityFilter) shouldBe true
            model.matches(combinedFilter) shouldBe false
        }
    }

    testSuite("applyFilters") {
        test("default filters return the original list instance") {
            val modelsList = listOf(imageModel(id = "0"), imageModel(id = "1"))

            modelsList.applyFilters(ImageModelFilters.Default) shouldBeSameInstanceAs modelsList
        }

        test("non-default filters keep matching models in their original order") {
            val modelsList = listOf(
                imageModel(id = "first", pinned = true),
                imageModel(id = "second", pinned = false),
                imageModel(id = "third", pinned = true)
            )

            modelsList.applyFilters(ImageModelFilters(pinnedOnly = true)).map { it.id } shouldBe
                listOf("first", "third")
        }

        test("filters that match nothing return an empty list") {
            val modelsList = listOf(imageModel(id = "0"), imageModel(id = "1"))

            modelsList.applyFilters(ImageModelFilters(requiredInputModalities = setOf(Modality.IMAGE))) shouldBe
                emptyList()
        }
    }

    testSuite("activeCount") {
        test("pinnedOnly counts as one") {
            ImageModelFilters(pinnedOnly = true).activeCount shouldBe 1
        }

        test("each required input modality counts as one") {
            ImageModelFilters(requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE)).activeCount shouldBe 2
        }

        test("pinnedOnly and modalities are summed") {
            val filters = ImageModelFilters(
                pinnedOnly = true,
                requiredInputModalities = setOf(Modality.TEXT, Modality.IMAGE)
            )

            filters.activeCount shouldBe 3
        }
    }

    testSuite("isDefault") {
        test("default filters are default") {
            ImageModelFilters().isDefault shouldBe true
        }

        listOf(
            "pinnedOnly" to ImageModelFilters(pinnedOnly = true),
            "requiredInputModalities" to ImageModelFilters(requiredInputModalities = setOf(Modality.TEXT))
        ).forEach { (field, filters) ->
            test("changed $field makes isDefault false") {
                filters.isDefault shouldBe false
            }
        }
    }
}
