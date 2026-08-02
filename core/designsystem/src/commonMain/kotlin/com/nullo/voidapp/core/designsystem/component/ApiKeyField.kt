package com.nullo.voidapp.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.ArrowForward
import com.nullo.voidapp.core.designsystem.icon.default.Check

/**
 * A specialized text field component designed for entering and submitting API keys,
 * featuring password masking, state management (saved/unsaved), and a submission action.
 *
 * @param submitContentDescription Accessibility content description for the submit button.
 * @param isSaved Indicates whether the API key has been successfully saved, altering the container and icon state.
 * @param savedText Optional text or message to display when the API key is in the saved state.
 */
@Composable
fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
    submitContentDescription: String,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    savedText: String = "",
) {
    VoidTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { if (submitEnabled) onSubmit() }
        ),
        containerColor = if (isSaved) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        placeholder = {
            Text(
                text = if (isSaved) savedText else placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSaved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        },
        trailingIcon = {
            AnimatedContent(
                targetState = isSaved,
                label = "ApiKeyTrailingIconAnimation"
            ) { saved ->
                if (saved) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = savedText,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    )
                } else {
                    IconButton(
                        enabled = submitEnabled,
                        onClick = onSubmit,
                        colors = IconButtonDefaults.filledIconButtonColors(),
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.ArrowForward,
                            contentDescription = submitContentDescription,
                        )
                    }
                }
            }
        }
    )
}
