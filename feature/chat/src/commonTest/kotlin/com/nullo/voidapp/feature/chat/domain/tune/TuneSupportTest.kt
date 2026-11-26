package com.nullo.voidapp.feature.chat.domain.tune

import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private data class SupportCase(
    val parameter: TuningParameter,
    val value: Double,
    val expected: TuneSupport?,
)

private val supportCases = listOf(
    SupportCase(TuningParameter.TEMPERATURE, 0.3, TuneSupport.TEMPERATURE_PRECISE),
    SupportCase(TuningParameter.TEMPERATURE, 0.31, TuneSupport.TEMPERATURE_BALANCED),
    SupportCase(TuningParameter.TEMPERATURE, 1.1, TuneSupport.TEMPERATURE_BALANCED),
    SupportCase(TuningParameter.TEMPERATURE, 1.11, TuneSupport.TEMPERATURE_WILD),
    SupportCase(TuningParameter.TOP_P, 0.5, TuneSupport.TOP_P_NARROW),
    SupportCase(TuningParameter.TOP_P, 0.51, TuneSupport.TOP_P_BALANCED),
    SupportCase(TuningParameter.TOP_P, 0.94, TuneSupport.TOP_P_BALANCED),
    SupportCase(TuningParameter.TOP_P, 0.95, TuneSupport.TOP_P_FULL),
    SupportCase(TuningParameter.TOP_K, 0.0, TuneSupport.TOP_K_OFF),
    SupportCase(TuningParameter.TOP_K, 1.0, TuneSupport.TOP_K_FOCUSED),
    SupportCase(TuningParameter.TOP_K, 20.0, TuneSupport.TOP_K_FOCUSED),
    SupportCase(TuningParameter.TOP_K, 21.0, TuneSupport.TOP_K_DIVERSE),
    SupportCase(TuningParameter.FREQUENCY_PENALTY, -0.06, TuneSupport.PENALTY_ENCOURAGES_REPEATS),
    SupportCase(TuningParameter.FREQUENCY_PENALTY, -0.05, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.FREQUENCY_PENALTY, 0.05, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.FREQUENCY_PENALTY, 0.06, TuneSupport.PENALTY_MILD),
    SupportCase(TuningParameter.FREQUENCY_PENALTY, 1.0, TuneSupport.PENALTY_MILD),
    SupportCase(TuningParameter.FREQUENCY_PENALTY, 1.01, TuneSupport.PENALTY_STRONG),
    SupportCase(TuningParameter.PRESENCE_PENALTY, -0.06, TuneSupport.PENALTY_ENCOURAGES_REPEATS),
    SupportCase(TuningParameter.PRESENCE_PENALTY, -0.05, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.PRESENCE_PENALTY, 0.05, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.PRESENCE_PENALTY, 0.06, TuneSupport.PENALTY_MILD),
    SupportCase(TuningParameter.PRESENCE_PENALTY, 1.0, TuneSupport.PENALTY_MILD),
    SupportCase(TuningParameter.PRESENCE_PENALTY, 1.01, TuneSupport.PENALTY_STRONG),
    SupportCase(TuningParameter.REPETITION_PENALTY, 0.9, TuneSupport.PENALTY_ENCOURAGES_REPEATS),
    SupportCase(TuningParameter.REPETITION_PENALTY, 0.94, TuneSupport.PENALTY_ENCOURAGES_REPEATS),
    SupportCase(TuningParameter.REPETITION_PENALTY, 0.95, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.REPETITION_PENALTY, 1.0, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.REPETITION_PENALTY, 1.05, TuneSupport.PENALTY_NEUTRAL),
    SupportCase(TuningParameter.REPETITION_PENALTY, 1.06, TuneSupport.PENALTY_MILD),
    SupportCase(TuningParameter.REPETITION_PENALTY, 1.3, TuneSupport.PENALTY_MILD),
    SupportCase(TuningParameter.REPETITION_PENALTY, 1.31, TuneSupport.PENALTY_STRONG),
    SupportCase(TuningParameter.REPETITION_PENALTY, 1.5, TuneSupport.PENALTY_STRONG),
    SupportCase(TuningParameter.MAX_TOKENS, 100.0, null),
    SupportCase(TuningParameter.SEED, 42.0, null)
)

val TuneSupportTest by testSuite {

    supportCases.forEach { case ->
        test("${case.parameter} ${case.value} is ${case.expected}") {
            case.parameter.supportFor(case.value) shouldBe case.expected
        }
    }
}
