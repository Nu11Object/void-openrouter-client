package com.nullo.voidapp.feature.chat.domain.tune

import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import kotlin.math.round

internal const val MAX_SEED = 2_147_483_647.0
private const val MAX_TOP_K = 200.0

data class TuneParamSpec(
    val parameter: TuningParameter,
    val control: Control,
    val min: Double,
    val max: Double,
    val step: Double,
    val isInteger: Boolean,
    val defaultValue: Double?,
) {

    enum class Control { SLIDER, FIELD }

    private val decimals: Int
        get() = when {
            isInteger -> 0
            step < 0.1 -> 2
            else -> 1
        }

    fun parse(raw: String): TuneInput = parseNumber(raw, min, max, isInteger)

    fun format(value: Double): String = formatNumber(value, decimals)

    fun snap(value: Double): Double {
        val steps = round((value - min) / step)
        return (min + steps * step).coerceIn(min, max)
    }

    fun effectiveValue(input: TuneInput): Double? = when (input) {
        TuneInput.Empty -> defaultValue
        is TuneInput.Valid -> input.value
        is TuneInput.Invalid -> null
    }
}

internal fun ChatModel.tuneSpecs(): List<TuneParamSpec> =
    TUNE_ORDER.filter(::supports).map(::specFor)

private val TUNE_ORDER = listOf(
    TuningParameter.TEMPERATURE,
    TuningParameter.TOP_P,
    TuningParameter.TOP_K,
    TuningParameter.FREQUENCY_PENALTY,
    TuningParameter.PRESENCE_PENALTY,
    TuningParameter.REPETITION_PENALTY,
    TuningParameter.MAX_TOKENS,
    TuningParameter.SEED
)

private fun ChatModel.specFor(param: TuningParameter): TuneParamSpec {
    val defaults = defaultTuningParameters
    return when (param) {
        TuningParameter.TEMPERATURE ->
            slider(param, 0.0, 2.0, 0.05, defaults.temperature ?: 1.0)

        TuningParameter.TOP_P ->
            slider(param, 0.0, 1.0, 0.05, defaults.topP ?: 1.0)

        TuningParameter.TOP_K ->
            slider(param, 0.0, MAX_TOP_K, 1.0, defaults.topK?.toDouble() ?: 0.0, integer = true)

        TuningParameter.FREQUENCY_PENALTY ->
            slider(param, -2.0, 2.0, 0.1, defaults.frequencyPenalty ?: 0.0)

        TuningParameter.PRESENCE_PENALTY ->
            slider(param, -2.0, 2.0, 0.1, defaults.presencePenalty ?: 0.0)

        TuningParameter.REPETITION_PENALTY ->
            slider(param, 0.0, 2.0, 0.05, defaults.repetitionPenalty ?: 1.0)

        TuningParameter.MAX_TOKENS -> TuneParamSpec(
            parameter = param,
            control = TuneParamSpec.Control.FIELD,
            min = 1.0,
            max = (maxCompletionTokens?.takeIf { it > 0 } ?: contextLength ?: Int.MAX_VALUE)
                .toDouble(),
            step = 1.0,
            isInteger = true,
            defaultValue = null
        )

        TuningParameter.SEED -> TuneParamSpec(
            parameter = param,
            control = TuneParamSpec.Control.FIELD,
            min = 0.0,
            max = MAX_SEED,
            step = 1.0,
            isInteger = true,
            defaultValue = null
        )
    }
}

private fun slider(
    parameter: TuningParameter,
    min: Double,
    max: Double,
    step: Double,
    default: Double,
    integer: Boolean = false,
) = TuneParamSpec(
    parameter = parameter,
    control = TuneParamSpec.Control.SLIDER,
    min = min,
    max = max,
    step = step,
    isInteger = integer,
    defaultValue = default.coerceIn(min, max)
)
