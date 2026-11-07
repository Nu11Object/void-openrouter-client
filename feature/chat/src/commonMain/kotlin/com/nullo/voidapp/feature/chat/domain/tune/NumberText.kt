package com.nullo.voidapp.feature.chat.domain.tune

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

private val NUMBER_REGEX = Regex("""[+-]?(\d+\.?\d*|\.\d+)""")

internal fun parseNumber(raw: String, min: Double, max: Double, isInteger: Boolean): TuneInput {
    val text = raw.trim().replace(',', '.')
    if (text.isEmpty()) return TuneInput.Empty
    if (!NUMBER_REGEX.matches(text)) return TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)
    val number = text.toDoubleOrNull()
        ?: return TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)
    return when {
        isInteger && number != floor(number) -> TuneInput.Invalid(TuneInput.Reason.NOT_AN_INTEGER)
        number < min -> TuneInput.Invalid(TuneInput.Reason.BELOW_MIN)
        number > max -> TuneInput.Invalid(TuneInput.Reason.ABOVE_MAX)
        else -> TuneInput.Valid(number)
    }
}

internal fun formatNumber(value: Double, decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(value * factor) / factor
    if (rounded == 0.0) return "0"
    return if (decimals == 0) rounded.toLong().toString() else rounded.toString().removeSuffix(".0")
}
