package com.nullo.voidapp.feature.settings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.DarkTheme
import com.nullo.voidapp.core.designsystem.icon.default.LightTheme
import com.nullo.voidapp.core.designsystem.icon.default.Palette
import com.nullo.voidapp.core.designsystem.icon.default.SystemTheme
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.settings.generated.resources.Res
import voidapp.feature.settings.generated.resources.dark
import voidapp.feature.settings.generated.resources.desc_dark
import voidapp.feature.settings.generated.resources.desc_light
import voidapp.feature.settings.generated.resources.desc_system
import voidapp.feature.settings.generated.resources.light
import voidapp.feature.settings.generated.resources.system
import voidapp.feature.settings.generated.resources.title_theme

private data class ThemeOptionData(
    val theme: AppTheme,
    val icon: ImageVector,
    val name: StringResource,
    val contentDescription: StringResource,
)

@Composable
internal fun ThemeSection(
    currentTheme: AppTheme,
    onChangeTheme: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeOptions = remember {
        listOf(
            ThemeOptionData(
                AppTheme.DARK,
                Icons.Default.DarkTheme,
                Res.string.dark,
                Res.string.desc_dark
            ),
            ThemeOptionData(
                AppTheme.LIGHT,
                Icons.Default.LightTheme,
                Res.string.light,
                Res.string.desc_light
            ),
            ThemeOptionData(
                AppTheme.SYSTEM,
                Icons.Default.SystemTheme,
                Res.string.system,
                Res.string.desc_system
            )
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            icon = Icons.Default.Palette,
            title = stringResource(Res.string.title_theme)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            themeOptions.fastForEach { option ->
                ThemeOption(
                    icon = option.icon,
                    contentDescription = option.contentDescription,
                    theme = option.theme,
                    themeName = option.name,
                    isSelected = currentTheme == option.theme,
                    onSelect = onChangeTheme,
                    modifier = Modifier.weight(1f).aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    contentDescription: StringResource,
    theme: AppTheme,
    themeName: StringResource,
    isSelected: Boolean,
    onSelect: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val onClick = remember(theme, onSelect) { { onSelect(theme) } }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color = containerColor)
                .selectable(
                    selected = isSelected,
                    role = Role.RadioButton,
                    onClick = onClick
                )
                .pointerHoverIcon(PointerIcon.Hand),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(contentDescription),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(themeName),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
