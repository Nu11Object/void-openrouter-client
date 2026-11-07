package com.nullo.voidapp.feature.chat.presentation.ui.tune

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.Check
import com.nullo.voidapp.core.designsystem.icon.default.Tune
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.select_model
import voidapp.feature.chat.generated.resources.tune_hints
import voidapp.feature.chat.generated.resources.tune_model
import voidapp.feature.chat.generated.resources.tune_no_model
import voidapp.feature.chat.generated.resources.tune_reset_all
import voidapp.feature.chat.generated.resources.tune_unsupported

@Immutable
internal class TuneActions(
    val onClose: () -> Unit = {},
    val onHintsVisibilityChange: (Boolean) -> Unit = {},
    val onReset: () -> Unit = {},
    val onChatValueChange: (TuningParameter, String) -> Unit = { _, _ -> },
    val onChatSliderChange: (TuningParameter, Double) -> Unit = { _, _ -> },
    val onMaxTokensSetToLimit: () -> Unit = {},
    val onChatSeedRandomize: () -> Unit = {},
    val onImageOptionSelect: (ImageParameter, String) -> Unit = { _, _ -> },
    val onImageValueChange: (ImageParameter, String) -> Unit = { _, _ -> },
    val onImageSliderChange: (ImageParameter, Double) -> Unit = { _, _ -> },
    val onImageSeedRandomize: () -> Unit = {},
)

@Composable
internal fun TuneDrawerHost(
    state: TuneStore.State,
    actions: TuneActions,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val layoutDirection = LocalLayoutDirection.current
    val mirroredDirection = if (layoutDirection == LayoutDirection.Ltr) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    val drawerState = rememberDrawerState(
        initialValue = if (state.isPanelOpen) DrawerValue.Open else DrawerValue.Closed
    )
    val onClose by rememberUpdatedState(actions.onClose)

    LaunchedEffect(state.isPanelOpen) {
        if (state.isPanelOpen) drawerState.open() else drawerState.close()
    }
    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.targetValue }
            .drop(1)
            .collect {
                if (it == DrawerValue.Closed) {
                    focusManager.clearFocus()
                    onClose()
                }
            }
    }

    CompositionLocalProvider(LocalLayoutDirection provides mirroredDirection) {
        ModalNavigationDrawer(
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    TuneDrawerSheet(state = state, actions = actions)
                }
            },
            modifier = modifier,
            drawerState = drawerState,
            gesturesEnabled = state.isPanelOpen,
            content = {
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection,
                    content = content
                )
            }
        )
    }
}

@Composable
private fun TuneDrawerSheet(
    state: TuneStore.State,
    actions: TuneActions,
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
    ) {
        TuneHeader(
            modelName = state.modelName,
            hintsVisible = state.hintsVisible,
            resetEnabled = state.content is TuneStore.State.Content.Chat ||
                state.content is TuneStore.State.Content.Image,
            actions = actions
        )
        LazyColumn(
            modifier = Modifier.weight(1f).imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val content = state.content) {
                TuneStore.State.Content.NoModel -> item(key = "NoModel") {
                    TunePlaceholder(text = stringResource(Res.string.tune_no_model))
                }

                TuneStore.State.Content.Unsupported -> item(key = "Unsupported") {
                    TunePlaceholder(text = stringResource(Res.string.tune_unsupported))
                }

                is TuneStore.State.Content.Chat -> items(
                    items = content.items,
                    key = { it.spec.parameter.name }
                ) { item ->
                    ChatTuneBlock(
                        item = item,
                        hintsVisible = state.hintsVisible,
                        maxTokensLimit = content.maxTokensLimit,
                        actions = actions
                    )
                }

                is TuneStore.State.Content.Image -> items(
                    items = content.items,
                    key = { it.parameter.name }
                ) { item ->
                    ImageTuneBlock(
                        item = item,
                        hintsVisible = state.hintsVisible,
                        actions = actions
                    )
                }
            }
        }
    }
}

@Composable
private fun TuneHeader(
    modelName: String?,
    hintsVisible: Boolean,
    resetEnabled: Boolean,
    actions: TuneActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.tune_model),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = actions.onReset,
                enabled = resetEnabled,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Restart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(Res.string.tune_reset_all), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = modelName ?: stringResource(Res.string.select_model),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.tune_hints),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = hintsVisible,
                    onCheckedChange = actions.onHintsVisibilityChange,
                    colors = SwitchDefaults.colors(
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    thumbContent = {
                        if (hintsVisible) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }
    }
}

@Composable
private fun TunePlaceholder(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = LocalContentColor.current.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    )
}
