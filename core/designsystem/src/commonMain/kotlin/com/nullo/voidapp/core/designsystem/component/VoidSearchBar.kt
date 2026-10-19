package com.nullo.voidapp.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Close
import com.nullo.voidapp.core.designsystem.icon.default.Search
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.Res
import voidapp.core.utils.generated.resources.desc_clear_search_query

/**
 * A reusable search field with a leading search icon
 * and a trailing button for clearing the query.
 *
 * @param query The current search query.
 * @param onQueryChange Callback invoked when the search query changes.
 * @param onClearQuery Callback invoked when the clear button is clicked.
 * @param placeholder Text displayed as the search field placeholder.
 * @param modifier Modifier to be applied to the search field.
 * @param height Desired height of the search field. Defaults to 64.dp.
 */
@Composable
fun VoidSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
) {
    val secondaryOnSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    VoidTextField(
        modifier = modifier.height(height),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                color = secondaryOnSurfaceColor
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                tint = secondaryOnSurfaceColor,
                contentDescription = null
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = secondaryOnSurfaceColor,
                        contentDescription = stringResource(Res.string.desc_clear_search_query)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        )
    )
}
