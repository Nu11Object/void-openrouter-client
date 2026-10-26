package com.nullo.voidapp.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.utils.resources.UiText

/**
 * A custom animated snackbar host component designed for handling [UiText] messages.
 *
 * @param message The [UiText] message to display, or null if hidden.
 * @param action Optional action composable slot (e.g. a dismiss or retry button).
 */
@Composable
fun VoidSnackbarHost(
    message: UiText?,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    maxWidth: Dp = 456.dp,
) {
    AnimatedContent(
        targetState = message,
        contentAlignment = Alignment.Center,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SnackbarAnimation",
        modifier = modifier.widthIn(max = maxWidth)
    ) { targetMessage ->
        if (targetMessage != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = action,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = targetMessage.asString()
                )
            }
        }
    }
}
