package com.nullo.voidapp.feature.chat.domain.tune

sealed interface TuneInput {
    data object Empty : TuneInput
    data class Valid(val value: Double) : TuneInput
    data class Invalid(val reason: Reason) : TuneInput

    enum class Reason { NOT_A_NUMBER, NOT_AN_INTEGER, BELOW_MIN, ABOVE_MAX }
}
