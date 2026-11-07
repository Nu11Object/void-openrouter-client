package com.nullo.voidapp.core.utils.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Keeps the list pinned to the bottom while its content grows, e.g. during response streaming.
 *
 * Following is paused as soon as the user scrolls backward and resumes once the list
 * reaches the bottom again, or when [resetKey] changes (e.g. a new message arrives).
 *
 * @param resetKey value whose change re-enables following regardless of the current scroll position.
 */
@Composable
fun LazyListState.StickToBottomEffect(resetKey: Any?) {
    var stick by remember { mutableStateOf(true) }

    LaunchedEffect(resetKey) { stick = true }

    LaunchedEffect(this) {
        launch {
            snapshotFlow { isScrollInProgress && lastScrolledBackward }
                .filter { it }
                .collect { stick = false }
        }
        snapshotFlow { canScrollForward }
            .filter { !it }
            .collect { stick = true }
    }

    LaunchedEffect(this, stick) {
        if (!stick) return@LaunchedEffect
        snapshotFlow { canScrollForward }
            .filter { it }
            .collect { scrollToItem(layoutInfo.totalItemsCount - 1, Int.MAX_VALUE) }
    }
}
