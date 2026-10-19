package com.nullo.voidapp.core.utils.kotlin

/**
 * Filters items whose mapped text contains [query], ignoring case.
 * Returns the original list if [query] is blank.
 */
inline fun <T> List<T>.filterWithQuery(
    query: String,
    crossinline filterBy: (T) -> String,
) = if (query.isBlank()) {
    this
} else {
    filter { filterBy(it).contains(query, ignoreCase = true) }
}
