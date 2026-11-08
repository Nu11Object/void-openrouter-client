package com.nullo.voidapp.feature.chat.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.MenuOpen
import com.nullo.voidapp.core.designsystem.icon.default.ArrowDropDown
import com.nullo.voidapp.core.designsystem.icon.default.Tune
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.desc_open_side_menu
import voidapp.feature.chat.generated.resources.template_desc_active_model
import voidapp.feature.chat.generated.resources.tune_model

@Composable
internal fun ChatTopAppBar(
    isSideMenuAlwaysExpanded: Boolean,
    activeModelName: String,
    onNavigationIconClick: () -> Unit,
    onOpenModelSelectorClick: () -> Unit,
    onOpenTuningPanelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        navigationIcon = {
            if (!isSideMenuAlwaysExpanded) {
                AppBarNavigationButton(onClick = onNavigationIconClick)
            }
        },
        title = { ActiveModelAppBarTitle(activeModelName, onClick = onOpenModelSelectorClick) },
        actions = { TuneModelButton(onClick = onOpenTuningPanelClick) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(end = 6.dp)
    )
}

@Composable
private fun AppBarNavigationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.MenuOpen,
            contentDescription = stringResource(Res.string.desc_open_side_menu)
        )
    }
}

@Composable
private fun TuneModelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.width(ButtonDefaults.ExtraSmallIconSpacing))
        Text(text = stringResource(Res.string.tune_model))
    }
}

@Composable
private fun ActiveModelAppBarTitle(
    activeModelName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.template_desc_active_model, activeModelName)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                role = Role.Button,
                indication = null,
                interactionSource = null
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .clearAndSetSemantics { this.contentDescription = contentDescription }
    ) {
        AnimatedContent(
            targetState = activeModelName,
            transitionSpec = {
                slideInHorizontally() + fadeIn() togetherWith
                    slideOutHorizontally { it / 2 } + fadeOut()
            },
            label = "AppBarModelName",
            modifier = Modifier.weight(1f, false)
        ) { modelName ->
            Text(
                text = modelName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
