package com.nullo.voidapp.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Configuration for buttons used in [VoidAlertDialog].
 *
 * @param text The button label.
 * @param textColor Custom color for the text. If [Color.Unspecified], the theme's default will be used.
 * @param onClick Action to execute when the button is clicked.
 */
data class VoidDialogButton(
    val text: String,
    val textColor: Color = Color.Unspecified,
    val onClick: () -> Unit
)

/**
 * A reusable alert dialog component with support for one or two action buttons.
 *
 * @param title The main header text of the dialog.
 * @param onDismissRequest Called when the user dismisses the dialog by clicking outside.
 * @param confirmButton The primary action button (always displayed on the right if two buttons exist).
 * @param modifier The modifier to be applied to the dialog layout.
 * @param message Optional supporting text to display below the title.
 * @param dismissButton Optional secondary action button (displayed on the left).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoidAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: VoidDialogButton,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissButton: VoidDialogButton? = null,
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                if (message != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.background)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dismissButton != null) {
                        TextButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerHoverIcon(PointerIcon.Hand),
                            onClick = dismissButton.onClick,
                        ) {
                            Text(
                                text = dismissButton.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (dismissButton.textColor == Color.Unspecified) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    dismissButton.textColor
                                }
                            )
                        }

                        VerticalDivider(color = MaterialTheme.colorScheme.background)
                    }

                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerHoverIcon(PointerIcon.Hand),
                        onClick = confirmButton.onClick,
                    ) {
                        Text(
                            text = confirmButton.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = confirmButton.textColor
                        )
                    }
                }
            }
        }
    }
}
