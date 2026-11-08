package com.nullo.voidapp.feature.catalog.presentation.ui

internal fun Int.toCompactString(): String = when {
    this >= 1_000_000 -> formatCompact(this, 1_000_000, "M")
    this >= 10_000 -> formatCompact(this, 1_000, "K")
    else -> this.toString()
}

internal fun formatCompact(value: Int, divisor: Int, suffix: String): String {
    val whole = value / divisor
    val remainder = value % divisor
    val tenth = (remainder * 10) / divisor

    return if (tenth == 0) {
        "$whole$suffix"
    } else {
        "$whole.$tenth$suffix"
    }
}
