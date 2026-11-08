package com.nullo.voidapp.feature.chat.presentation.ui.sidemenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.component.VoidSearchBar
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.LeftPanelClose
import com.nullo.voidapp.core.designsystem.icon.default.Awesome
import com.nullo.voidapp.core.designsystem.icon.default.Logo
import com.nullo.voidapp.core.designsystem.icon.default.Settings
import com.nullo.voidapp.core.designsystem.icon.default.Stylus
import com.nullo.voidapp.core.model.Conversation
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_collapse_side_menu
import voidapp.feature.chat.generated.resources.model_catalog
import voidapp.feature.chat.generated.resources.new_chat
import voidapp.feature.chat.generated.resources.placeholder_no_conversations
import voidapp.feature.chat.generated.resources.placeholder_no_conversations_found
import voidapp.feature.chat.generated.resources.placeholder_search_chats
import voidapp.feature.chat.generated.resources.settings
import voidapp.feature.chat.generated.resources.void

private val SIDE_MENU_MAX_EXPANDED_WIDTH = 296.dp

@Immutable
internal data class SideMenuActions(
    val onStartNewConversation: () -> Unit = {},
    val onConversationQueryChange: (String) -> Unit = {},
    val onSelectConversation: (id: Long) -> Unit = {},
    val onNavigateToModelCatalog: () -> Unit = {},
    val onNavigateToAppSettings: () -> Unit = {},
    val onEditConversationTitleClick: (id: Long) -> Unit = {},
    val onDeleteConversationClick: (id: Long) -> Unit = {},
    val onDismissRenameDialog: () -> Unit = {},
    val onConversationNameInputChange: (String) -> Unit = {},
    val onConfirmConversationRename: () -> Unit = {},
    val onDismissErrorDialog: () -> Unit = {},
)

@Composable
internal fun SideMenu(
    railState: WideNavigationRailState,
    alwaysExpanded: Boolean,
    conversations: List<Conversation>,
    activeConversationId: Long?,
    conversationQuery: String,
    actions: SideMenuActions,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val collapseSideMenu: () -> Unit = remember(scope, railState) {
        { scope.launch { railState.collapse() } }
    }
    val colors = WideNavigationRailDefaults.colors(
        modalContainerColor = MaterialTheme.colorScheme.surface,
        modalContentColor = MaterialTheme.colorScheme.onBackground
    )

    if (alwaysExpanded) {
        WideNavigationRail(
            state = rememberWideNavigationRailState(WideNavigationRailValue.Expanded),
            modifier = modifier
                .widthIn(max = SIDE_MENU_MAX_EXPANDED_WIDTH)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 32.dp),
            colors = colors,
            shape = RoundedCornerShape(24.dp)
        ) {
            SideMenuContent(
                conversations = conversations,
                activeConversationId = activeConversationId,
                conversationSearchQuery = conversationQuery,
                actions = actions
            )
        }
    } else {
        val modalActions = remember(actions, collapseSideMenu) {
            actions.copy(
                onStartNewConversation = {
                    collapseSideMenu()
                    actions.onStartNewConversation()
                },
                onSelectConversation = { id ->
                    collapseSideMenu()
                    actions.onSelectConversation(id)
                },
                onNavigateToModelCatalog = {
                    collapseSideMenu()
                    actions.onNavigateToModelCatalog()
                },
                onNavigateToAppSettings = {
                    collapseSideMenu()
                    actions.onNavigateToAppSettings()
                }
            )
        }

        ModalWideNavigationRail(
            state = railState,
            colors = colors,
            expandedShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            contentPadding = PaddingValues(start = 16.dp),
            hideOnCollapse = true
        ) {
            SideMenuContent(
                conversations = conversations,
                activeConversationId = activeConversationId,
                conversationSearchQuery = conversationQuery,
                actions = modalActions,
                showHeader = true,
                onCloseSideMenuClicked = collapseSideMenu
            )
        }
    }
}

@Composable
private fun SideMenuContent(
    conversations: List<Conversation>,
    activeConversationId: Long?,
    conversationSearchQuery: String,
    actions: SideMenuActions,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    onCloseSideMenuClicked: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showHeader) {
            item(key = "Header") {
                SideMenuHeader(
                    modifier = Modifier.padding(start = 16.dp),
                    onCloseSideMenuClicked = onCloseSideMenuClicked
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item(key = "NewChat") {
            SideMenuItem(
                icon = Icons.Default.Stylus,
                label = stringResource(Res.string.new_chat),
                onClick = actions.onStartNewConversation
            )
        }
        item(key = "ModelCatalog") {
            SideMenuItem(
                icon = Icons.Default.Awesome,
                label = stringResource(Res.string.model_catalog),
                onClick = actions.onNavigateToModelCatalog
            )
        }
        item(key = "Settings") {
            SideMenuItem(
                icon = Icons.Default.Settings,
                label = stringResource(Res.string.settings),
                onClick = actions.onNavigateToAppSettings
            )
        }

        item(key = "SearchConversations") {
            Spacer(Modifier.height(24.dp))
            VoidSearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = conversationSearchQuery,
                onQueryChange = actions.onConversationQueryChange,
                onClearQuery = { actions.onConversationQueryChange("") },
                placeholder = stringResource(Res.string.placeholder_search_chats)
            )
        }

        items(items = conversations, key = { it.id }) { conversation ->
            ConversationItem(
                conversation = conversation,
                activeConversationId = activeConversationId,
                onSelectConversation = actions.onSelectConversation,
                onEditConversationTitleClick = actions.onEditConversationTitleClick,
                onDeleteConversationClick = actions.onDeleteConversationClick,
                modifier = Modifier.animateItem()
            )
        }

        if (conversations.isEmpty()) {
            item(key = "EmptyConversations") {
                Text(
                    text = stringResource(
                        if (conversationSearchQuery.isEmpty()) {
                            Res.string.placeholder_no_conversations
                        } else {
                            Res.string.placeholder_no_conversations_found
                        }
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SideMenuHeader(
    modifier: Modifier = Modifier,
    onCloseSideMenuClicked: (() -> Unit),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Logo,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(Res.string.void),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { hideFromAccessibility() }
        )
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onCloseSideMenuClicked,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.LeftPanelClose,
                contentDescription = stringResource(Res.string.desc_collapse_side_menu)
            )
        }
    }
}

@Composable
private fun SideMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
