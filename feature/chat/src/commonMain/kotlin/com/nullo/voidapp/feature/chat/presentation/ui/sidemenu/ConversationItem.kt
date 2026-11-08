package com.nullo.voidapp.feature.chat.presentation.ui.sidemenu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.DeleteForever
import com.nullo.voidapp.core.designsystem.icon.default.MoreVert
import com.nullo.voidapp.core.designsystem.icon.default.Stylus
import com.nullo.voidapp.core.model.Conversation
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.delete
import voidapp.feature.chat.generated.resources.rename
import voidapp.feature.chat.generated.resources.template_delete
import voidapp.feature.chat.generated.resources.template_desc_more_conv_actions
import voidapp.feature.chat.generated.resources.template_rename
import voidapp.feature.chat.generated.resources.untitled_conversation

@Composable
internal fun ConversationItem(
    conversation: Conversation,
    activeConversationId: Long?,
    onSelectConversation: (id: Long) -> Unit,
    onEditConversationTitleClick: (id: Long) -> Unit,
    onDeleteConversationClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = conversation.id == activeConversationId
    val title = conversation.title ?: stringResource(Res.string.untitled_conversation)
    var actionMenuExpanded by remember { mutableStateOf(false) }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val renameContentDescription = stringResource(Res.string.template_rename, title)
    val deleteContentDescription = stringResource(Res.string.template_delete, title)

    Row(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = { onSelectConversation(conversation.id) })
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = { actionMenuExpanded = true },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(
                    Res.string.template_desc_more_conv_actions,
                    title
                ),
                tint = contentColor
            )
            DropdownMenu(
                expanded = actionMenuExpanded,
                onDismissRequest = { actionMenuExpanded = false },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                DropdownMenuItem(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .semantics { contentDescription = renameContentDescription },
                    text = { Text(text = stringResource(Res.string.rename)) },
                    onClick = {
                        actionMenuExpanded = false
                        onEditConversationTitleClick(conversation.id)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Stylus,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .semantics { contentDescription = deleteContentDescription },
                    text = { Text(text = stringResource(Res.string.delete)) },
                    onClick = {
                        actionMenuExpanded = false
                        onDeleteConversationClick(conversation.id)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}
