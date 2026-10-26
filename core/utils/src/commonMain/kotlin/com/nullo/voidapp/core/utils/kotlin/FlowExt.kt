package com.nullo.voidapp.core.utils.kotlin

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

/** Transforms each item in every list emitted by this flow. */
inline fun <T, R> Flow<List<T>>.mapItems(
    crossinline transform: (T) -> R,
) = map { list -> list.map(transform) }

/** Debounces non-empty queries by [duration] and emits empty queries immediately.*/
@OptIn(FlowPreview::class)
fun Flow<String>.debouncedQuery(duration: Duration) = debounce { query ->
    if (query.isEmpty()) 0.milliseconds else duration
}
