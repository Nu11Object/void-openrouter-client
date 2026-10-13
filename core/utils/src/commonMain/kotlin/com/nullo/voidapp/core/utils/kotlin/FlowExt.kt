package com.nullo.voidapp.core.utils.kotlin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Transforms each item in every list emitted by this flow. */
inline fun <T, R> Flow<List<T>>.mapItems(
    crossinline transform: (T) -> R
) = map { list -> list.map(transform) }
