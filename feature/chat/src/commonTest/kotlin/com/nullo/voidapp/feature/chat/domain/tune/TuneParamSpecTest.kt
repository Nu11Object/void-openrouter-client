package com.nullo.voidapp.feature.chat.domain.tune

import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.testing.chatModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

private val allParameters = setOf(
    TuningParameter.TEMPERATURE,
    TuningParameter.TOP_P,
    TuningParameter.TOP_K,
    TuningParameter.FREQUENCY_PENALTY,
    TuningParameter.PRESENCE_PENALTY,
    TuningParameter.REPETITION_PENALTY,
    TuningParameter.MAX_TOKENS,
    TuningParameter.SEED
)

private val allSpecs = chatModel(
    supportedTuningParameters = allParameters,
    maxCompletionTokens = 256_000,
    contextLength = 1_000_000
).tuneSpecs().associateBy { it.parameter }

private val snapSpecs = chatModel(
    supportedTuningParameters = setOf(TuningParameter.TEMPERATURE, TuningParameter.TOP_K)
).tuneSpecs().associateBy { it.parameter }

private val parseSpecs = chatModel(
    supportedTuningParameters = setOf(TuningParameter.TEMPERATURE, TuningParameter.TOP_K)
).tuneSpecs().associateBy { it.parameter }

private val temperatureSpec = chatModel(
    supportedTuningParameters = setOf(TuningParameter.TEMPERATURE)
).tuneSpecs().single()

private data class SpecCase(
    val parameter: TuningParameter,
    val control: TuneParamSpec.Control,
    val min: Double,
    val max: Double,
    val step: Double,
    val isInteger: Boolean,
    val defaultValue: Double?,
)

private data class MaxTokensCase(
    val maxCompletionTokens: Int?,
    val contextLength: Int?,
    val expectedMax: Double,
)

private data class SnapCase(
    val parameter: TuningParameter,
    val input: Double,
    val expected: Double,
)

private data class FormatCase(
    val parameter: TuningParameter,
    val value: Double,
    val expected: String,
)

private data class ParseCase(
    val parameter: TuningParameter,
    val raw: String,
    val expected: TuneInput,
)

private data class EffectiveValueCase(
    val input: TuneInput,
    val expected: Double?,
)

private val specCases = listOf(
    SpecCase(TuningParameter.TEMPERATURE, TuneParamSpec.Control.SLIDER, 0.0, 2.0, 0.05, false, 1.0),
    SpecCase(TuningParameter.TOP_P, TuneParamSpec.Control.SLIDER, 0.0, 1.0, 0.05, false, 1.0),
    SpecCase(TuningParameter.TOP_K, TuneParamSpec.Control.SLIDER, 0.0, 200.0, 1.0, true, 0.0),
    SpecCase(
        TuningParameter.FREQUENCY_PENALTY,
        TuneParamSpec.Control.SLIDER,
        -2.0,
        2.0,
        0.1,
        false,
        0.0
    ),
    SpecCase(
        TuningParameter.PRESENCE_PENALTY,
        TuneParamSpec.Control.SLIDER,
        -2.0,
        2.0,
        0.1,
        false,
        0.0
    ),
    SpecCase(
        TuningParameter.REPETITION_PENALTY,
        TuneParamSpec.Control.SLIDER,
        0.0,
        2.0,
        0.05,
        false,
        1.0
    ),
    SpecCase(TuningParameter.MAX_TOKENS, TuneParamSpec.Control.FIELD, 1.0, 256_000.0, 1.0, true, null),
    SpecCase(TuningParameter.SEED, TuneParamSpec.Control.FIELD, 0.0, 2_147_483_647.0, 1.0, true, null)
)

private val maxTokensCases = listOf(
    MaxTokensCase(maxCompletionTokens = 500, contextLength = 1_000, expectedMax = 500.0),
    MaxTokensCase(maxCompletionTokens = 0, contextLength = 1_000, expectedMax = 1_000.0),
    MaxTokensCase(maxCompletionTokens = null, contextLength = 1_000, expectedMax = 1_000.0),
    MaxTokensCase(maxCompletionTokens = null, contextLength = null, expectedMax = 2_147_483_647.0),
    MaxTokensCase(maxCompletionTokens = 0, contextLength = null, expectedMax = 2_147_483_647.0)
)

private val snapCases = listOf(
    SnapCase(TuningParameter.TEMPERATURE, 0.73, 0.75),
    SnapCase(TuningParameter.TEMPERATURE, 0.72, 0.70),
    SnapCase(TuningParameter.TEMPERATURE, -1.0, 0.0),
    SnapCase(TuningParameter.TEMPERATURE, 5.0, 2.0),
    SnapCase(TuningParameter.TOP_K, 12.6, 13.0)
)

private val formatCases = listOf(
    FormatCase(TuningParameter.TEMPERATURE, 0.75, "0.75"),
    FormatCase(TuningParameter.TEMPERATURE, 0.7, "0.7"),
    FormatCase(TuningParameter.TEMPERATURE, 2.0, "2"),
    FormatCase(TuningParameter.TEMPERATURE, 0.0, "0"),
    FormatCase(TuningParameter.TOP_K, 13.0, "13"),
    FormatCase(TuningParameter.FREQUENCY_PENALTY, 1.0, "1"),
    FormatCase(TuningParameter.FREQUENCY_PENALTY, 0.5, "0.5"),
    FormatCase(TuningParameter.SEED, 42.0, "42")
)

private val parseCases = listOf(
    ParseCase(TuningParameter.TEMPERATURE, "", TuneInput.Empty),
    ParseCase(TuningParameter.TEMPERATURE, "  ", TuneInput.Empty),
    ParseCase(TuningParameter.TEMPERATURE, "abc", TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)),
    ParseCase(TuningParameter.TOP_K, "1.5", TuneInput.Invalid(TuneInput.Reason.NOT_AN_INTEGER)),
    ParseCase(TuningParameter.TEMPERATURE, "-0.1", TuneInput.Invalid(TuneInput.Reason.BELOW_MIN)),
    ParseCase(TuningParameter.TEMPERATURE, "2.1", TuneInput.Invalid(TuneInput.Reason.ABOVE_MAX)),
    ParseCase(TuningParameter.TEMPERATURE, "0.7", TuneInput.Valid(0.7)),
    ParseCase(TuningParameter.TEMPERATURE, "0,7", TuneInput.Valid(0.7)),
    ParseCase(TuningParameter.TEMPERATURE, "-1", TuneInput.Invalid(TuneInput.Reason.BELOW_MIN))
)

private val effectiveValueCases = listOf(
    EffectiveValueCase(TuneInput.Empty, 1.0),
    EffectiveValueCase(TuneInput.Valid(0.3), 0.3),
    EffectiveValueCase(TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER), null)
)

val TuneParamSpecTest by testSuite {

    testSuite("tuneSpecs") {

        test("all eight parameters are returned in the fixed order despite a scrambled supported set") {
            val model = chatModel(
                supportedTuningParameters = setOf(
                    TuningParameter.SEED,
                    TuningParameter.TOP_K,
                    TuningParameter.MAX_TOKENS,
                    TuningParameter.PRESENCE_PENALTY,
                    TuningParameter.TEMPERATURE,
                    TuningParameter.REPETITION_PENALTY,
                    TuningParameter.TOP_P,
                    TuningParameter.FREQUENCY_PENALTY
                )
            )

            model.tuneSpecs().map { it.parameter } shouldBe listOf(
                TuningParameter.TEMPERATURE,
                TuningParameter.TOP_P,
                TuningParameter.TOP_K,
                TuningParameter.FREQUENCY_PENALTY,
                TuningParameter.PRESENCE_PENALTY,
                TuningParameter.REPETITION_PENALTY,
                TuningParameter.MAX_TOKENS,
                TuningParameter.SEED
            )
        }

        test("a subset returns only the supported parameters in the fixed order") {
            val model = chatModel(
                supportedTuningParameters = setOf(
                    TuningParameter.SEED,
                    TuningParameter.TEMPERATURE,
                    TuningParameter.TOP_K
                )
            )

            model.tuneSpecs().map { it.parameter } shouldBe listOf(
                TuningParameter.TEMPERATURE,
                TuningParameter.TOP_K,
                TuningParameter.SEED
            )
        }

        test("no supported parameters returns empty") {
            chatModel(supportedTuningParameters = emptySet()).tuneSpecs() shouldBe emptyList()
        }
    }

    testSuite("spec") {

        specCases.forEach { case ->
            test(
                "${case.parameter} has ${case.control} ${case.min}..${case.max} " +
                    "step ${case.step} integer=${case.isInteger} default=${case.defaultValue}"
            ) {
                val spec = allSpecs.getValue(case.parameter)

                spec.control shouldBe case.control
                spec.min shouldBe (case.min plusOrMinus 1e-9)
                spec.max shouldBe (case.max plusOrMinus 1e-9)
                spec.step shouldBe (case.step plusOrMinus 1e-9)
                spec.isInteger shouldBe case.isInteger
                spec.defaultValue shouldBe case.defaultValue
            }
        }

        test("model defaults override the standard defaults") {
            val model = chatModel(
                supportedTuningParameters = setOf(TuningParameter.TEMPERATURE)
            ).copy(defaultTuningParameters = DefaultTuningParameters(temperature = 0.7))

            model.tuneSpecs().single().defaultValue shouldBe 0.7
        }

        test("model defaults are clamped into the parameter range") {
            val model = chatModel(
                supportedTuningParameters = setOf(TuningParameter.TEMPERATURE)
            ).copy(defaultTuningParameters = DefaultTuningParameters(temperature = 5.0))

            model.tuneSpecs().single().defaultValue shouldBe 2.0
        }

        maxTokensCases.forEach { case ->
            test(
                "MAX_TOKENS max is ${case.expectedMax} for maxCompletionTokens=" +
                    "${case.maxCompletionTokens} and contextLength=${case.contextLength}"
            ) {
                val spec = chatModel(
                    supportedTuningParameters = setOf(TuningParameter.MAX_TOKENS),
                    maxCompletionTokens = case.maxCompletionTokens,
                    contextLength = case.contextLength
                ).tuneSpecs().single()

                spec.max shouldBe (case.expectedMax plusOrMinus 1e-9)
            }
        }
    }

    testSuite("snap") {

        snapCases.forEach { case ->
            test("${case.parameter} ${case.input} snaps to ${case.expected}") {
                snapSpecs.getValue(case.parameter).snap(case.input) shouldBe
                    (case.expected plusOrMinus 1e-9)
            }
        }
    }

    testSuite("format") {

        formatCases.forEach { case ->
            test("${case.parameter} ${case.value} formats as \"${case.expected}\"") {
                allSpecs.getValue(case.parameter).format(case.value) shouldBe case.expected
            }
        }
    }

    testSuite("parse") {

        parseCases.forEach { case ->
            test("raw \"${case.raw}\" of ${case.parameter} is ${case.expected}") {
                parseSpecs.getValue(case.parameter).parse(case.raw) shouldBe case.expected
            }
        }
    }

    testSuite("effectiveValue") {

        effectiveValueCases.forEach { case ->
            test("${case.input} effective value is ${case.expected}") {
                temperatureSpec.effectiveValue(case.input) shouldBe case.expected
            }
        }
    }
}
