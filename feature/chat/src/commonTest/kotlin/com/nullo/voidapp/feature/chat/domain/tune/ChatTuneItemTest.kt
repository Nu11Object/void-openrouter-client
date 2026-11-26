package com.nullo.voidapp.feature.chat.domain.tune

import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.testing.chatModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private val chatTuneModel = chatModel(
    supportedTuningParameters = setOf(
        TuningParameter.TEMPERATURE,
        TuningParameter.TOP_K,
        TuningParameter.MAX_TOKENS,
        TuningParameter.SEED
    )
)

private val chatTuneSpecs = chatTuneModel.tuneSpecs().associateBy { it.parameter }

private fun chatItem(parameter: TuningParameter, raw: String?) =
    ChatTuneItem(chatTuneSpecs.getValue(parameter), raw)

private data class ItemDisplayCase(
    val parameter: TuningParameter,
    val raw: String?,
    val expected: String,
)

private data class ItemInputCase(
    val parameter: TuningParameter,
    val raw: String?,
    val expected: TuneInput,
)

private data class SessionMapCase(
    val name: String,
    val items: List<ChatTuneItem>,
    val expected: Map<TuningParameter, Double>,
)

private val displayCases = listOf(
    ItemDisplayCase(TuningParameter.TEMPERATURE, null, "1"),
    ItemDisplayCase(TuningParameter.TOP_K, null, "0"),
    ItemDisplayCase(TuningParameter.MAX_TOKENS, null, ""),
    ItemDisplayCase(TuningParameter.TEMPERATURE, "", ""),
    ItemDisplayCase(TuningParameter.TEMPERATURE, "0.3", "0.3"),
    ItemDisplayCase(TuningParameter.TEMPERATURE, "abc", "abc")
)

private val inputCases = listOf(
    ItemInputCase(TuningParameter.TEMPERATURE, "0.3", TuneInput.Valid(0.3)),
    ItemInputCase(TuningParameter.TEMPERATURE, "", TuneInput.Empty),
    ItemInputCase(TuningParameter.TEMPERATURE, null, TuneInput.Empty),
    ItemInputCase(
        TuningParameter.TEMPERATURE,
        "abc",
        TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)
    ),
    ItemInputCase(
        TuningParameter.TOP_K,
        "1.5",
        TuneInput.Invalid(TuneInput.Reason.NOT_AN_INTEGER)
    )
)

private val sessionMapCases = listOf(
    SessionMapCase(
        name = "TEMPERATURE 0.5 with empty TOP_K and SEED",
        items = listOf(
            chatItem(TuningParameter.TEMPERATURE, "0.5"),
            chatItem(TuningParameter.TOP_K, null),
            chatItem(TuningParameter.SEED, "")
        ),
        expected = mapOf(TuningParameter.TEMPERATURE to 0.5)
    ),
    SessionMapCase(
        name = "TEMPERATURE abc is skipped",
        items = listOf(chatItem(TuningParameter.TEMPERATURE, "abc")),
        expected = emptyMap()
    ),
    SessionMapCase(
        name = "TEMPERATURE 0.5, TOP_K 12 and SEED 42",
        items = listOf(
            chatItem(TuningParameter.TEMPERATURE, "0.5"),
            chatItem(TuningParameter.TOP_K, "12"),
            chatItem(TuningParameter.SEED, "42")
        ),
        expected = mapOf(
            TuningParameter.TEMPERATURE to 0.5,
            TuningParameter.TOP_K to 12.0,
            TuningParameter.SEED to 42.0
        )
    )
)

val ChatTuneItemTest by testSuite {

    testSuite("displayText") {

        displayCases.forEach { case ->
            test(
                "${case.parameter} raw ${case.raw?.let { "\"$it\"" } ?: "null"} " +
                    "displays \"${case.expected}\""
            ) {
                chatItem(case.parameter, case.raw).displayText shouldBe case.expected
            }
        }
    }

    testSuite("input") {

        inputCases.forEach { case ->
            test(
                "${case.parameter} raw ${case.raw?.let { "\"$it\"" } ?: "null"} is ${case.expected}"
            ) {
                chatItem(case.parameter, case.raw).input shouldBe case.expected
            }
        }
    }

    testSuite("toTuneItems") {

        test("saved values are restored formatted by the spec and unsaved ones stay null") {
            val model = chatModel(
                supportedTuningParameters = setOf(
                    TuningParameter.TEMPERATURE,
                    TuningParameter.TOP_K,
                    TuningParameter.SEED
                )
            )
            val items = model.toTuneItems(
                mapOf(
                    TuningParameter.TEMPERATURE to 0.75,
                    TuningParameter.SEED to 42.0
                )
            )

            items.map { it.spec.parameter } shouldBe listOf(
                TuningParameter.TEMPERATURE,
                TuningParameter.TOP_K,
                TuningParameter.SEED
            )
            items.map { it.raw } shouldBe listOf("0.75", null, "42")
        }

        test("saved values of unsupported parameters are dropped") {
            val model = chatModel(
                supportedTuningParameters = setOf(TuningParameter.TEMPERATURE)
            )

            model.toTuneItems(mapOf(TuningParameter.TOP_K to 12.0)).map { it.raw } shouldBe
                listOf<String?>(null)
        }
    }

    testSuite("toSessionMap") {

        sessionMapCases.forEach { case ->
            test("${case.name} maps to the expected session map") {
                case.items.toSessionMap() shouldBe case.expected
            }
        }

        test("restoring and mapping saved values is a round trip") {
            val saved = mapOf(
                TuningParameter.TEMPERATURE to 0.5,
                TuningParameter.TOP_K to 12.0,
                TuningParameter.SEED to 42.0
            )

            chatTuneModel.toTuneItems(saved).toSessionMap() shouldBe saved
        }
    }
}
