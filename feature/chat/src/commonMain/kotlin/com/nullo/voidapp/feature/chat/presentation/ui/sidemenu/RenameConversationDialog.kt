package com.nullo.voidapp.feature.chat.presentation.ui.sidemenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nullo.voidapp.core.designsystem.component.VoidTextField
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.cancel
import voidapp.feature.chat.generated.resources.placeholder_new_conversation_title
import voidapp.feature.chat.generated.resources.rename
import voidapp.feature.chat.generated.resources.rename_conversation_desc
import voidapp.feature.chat.generated.resources.rename_conversation_title

@Composable
internal fun RenameConversationDialog(
    conversationTitle: String,
    onConversationTitleChange: (String) -> Unit,
    onDismissRenameConversationDialog: () -> Unit,
    onConfirmConversationRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismissRenameConversationDialog) {
        Column(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraExtraLarge
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.rename_conversation_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(Res.string.rename_conversation_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            VoidTextField(
                value = conversationTitle,
                onValueChange = onConversationTitleChange,
                placeholder = {
                    Text(
                        text = stringResource(Res.string.placeholder_new_conversation_title),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.heightIn(min = 56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onConfirmConversationRename,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(text = stringResource(Res.string.rename))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDismissRenameConversationDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        }
    }
}
