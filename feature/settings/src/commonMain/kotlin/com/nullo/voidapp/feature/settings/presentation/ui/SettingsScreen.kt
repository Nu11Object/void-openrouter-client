package com.nullo.voidapp.feature.settings.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nullo.voidapp.core.designsystem.component.ApiKeyField
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.ArrowBack
import com.nullo.voidapp.core.designsystem.icon.default.DarkTheme
import com.nullo.voidapp.core.designsystem.icon.default.Key
import com.nullo.voidapp.core.designsystem.icon.default.LightTheme
import com.nullo.voidapp.core.designsystem.icon.default.Palette
import com.nullo.voidapp.core.designsystem.icon.default.SystemTheme
import com.nullo.voidapp.core.designsystem.icon.default.Warning
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.settings.presentation.component.SettingsComponent
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Dialog
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Loading
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.cancel
import voidapp.core.utils.generated.resources.desc_go_back
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.feature.settings.generated.resources.Res
import voidapp.feature.settings.generated.resources.api_key_changed
import voidapp.feature.settings.generated.resources.button_desc_sign_out
import voidapp.feature.settings.generated.resources.change_api_key
import voidapp.feature.settings.generated.resources.dark
import voidapp.feature.settings.generated.resources.desc_dark
import voidapp.feature.settings.generated.resources.desc_light
import voidapp.feature.settings.generated.resources.desc_submit_api_key
import voidapp.feature.settings.generated.resources.desc_system
import voidapp.feature.settings.generated.resources.description_auth
import voidapp.feature.settings.generated.resources.description_sign_out
import voidapp.feature.settings.generated.resources.light
import voidapp.feature.settings.generated.resources.question_sign_out
import voidapp.feature.settings.generated.resources.sign_out
import voidapp.feature.settings.generated.resources.system
import voidapp.feature.settings.generated.resources.title_auth
import voidapp.feature.settings.generated.resources.title_danger
import voidapp.feature.settings.generated.resources.title_settings
import voidapp.feature.settings.generated.resources.title_theme
import voidapp.core.utils.generated.resources.Res as CoreRes

@Composable
fun SettingsScreen(
    settingsComponent: SettingsComponent,
    modifier: Modifier = Modifier
) {
    val state by settingsComponent.model.subscribeAsState()

    SettingsScreenContent(
        modifier = modifier,
        state = state,
        onBackClick = settingsComponent::onBackClicked,
        onApiKeyChange = settingsComponent::onApiKeyChanged,
        onSubmitApiKey = settingsComponent::onSubmitApiKeyClicked,
        onChangeTheme = settingsComponent::onThemeClicked,
        onRequestSignOut = settingsComponent::onRequestSignOutClicked,
        onConfirmSignOut = settingsComponent::onConfirmSignOutClicked,
        onDismissDialog = settingsComponent::onDialogDismissed
    )
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsStore.State,
    onBackClick: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSubmitApiKey: () -> Unit,
    onChangeTheme: (AppTheme) -> Unit,
    onRequestSignOut: () -> Unit,
    onConfirmSignOut: () -> Unit,
    onDismissDialog: () -> Unit,
) {
    val deviceConfiguration = rememberDeviceConfiguration()
    val isPortrait = remember(deviceConfiguration) {
        deviceConfiguration == DeviceConfiguration.MOBILE_PORTRAIT ||
                deviceConfiguration == DeviceConfiguration.TABLET_PORTRAIT
    }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val itemModifier = remember(surfaceColor) {
        Modifier
            .background(
                color = surfaceColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    }

    Scaffold(
        modifier = modifier,
        topBar = { SettingsTopAppBar(onBackClick = onBackClick) }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            columns = StaggeredGridCells.Fixed(if (isPortrait) 1 else 2),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            item(key = "ThemeSection") {
                ThemeSection(
                    currentTheme = state.appTheme,
                    onChangeTheme = onChangeTheme,
                    modifier = itemModifier
                )
            }

            item(key = "AuthSection") {
                AuthSection(
                    apiKey = state.apiKey,
                    isLoading = state.loading == Loading.ApiKey,
                    isApiKeySaved = state.isApiKeySaved,
                    onApiKeyChange = onApiKeyChange,
                    onSubmitApiKey = onSubmitApiKey,
                    modifier = itemModifier
                )
            }

            item(key = "DangerSection") {
                DangerSection(
                    onRequestSignOut = onRequestSignOut,
                    modifier = itemModifier
                )
            }
        }
    }

    state.dialog?.let { dialog ->
        when (dialog) {
            Dialog.SignOutConfirmation -> {
                VoidAlertDialog(
                    title = stringResource(Res.string.question_sign_out),
                    message = stringResource(Res.string.description_sign_out),
                    onDismissRequest = onDismissDialog,
                    confirmButton = VoidDialogButton(
                        text = stringResource(Res.string.sign_out),
                        textColor = MaterialTheme.colorScheme.error,
                        onClick = onConfirmSignOut
                    ),
                    dismissButton = VoidDialogButton(
                        text = stringResource(CoreRes.string.cancel),
                        onClick = onDismissDialog,
                    )
                )
            }

            is Dialog.Error -> {
                VoidAlertDialog(
                    title = stringResource(CoreRes.string.oops),
                    message = dialog.message.asString(),
                    onDismissRequest = onDismissDialog,
                    confirmButton = VoidDialogButton(
                        text = stringResource(CoreRes.string.ok),
                        onClick = onDismissDialog
                    )
                )
            }
        }
    }

    AnimatedVisibility(
        visible = state.loading == Loading.FullScreen,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                indicatorColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AuthSection(
    apiKey: String,
    isLoading: Boolean,
    isApiKeySaved: Boolean,
    onApiKeyChange: (String) -> Unit,
    onSubmitApiKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            icon = Icons.Default.Key,
            title = stringResource(Res.string.title_auth),
        )
        Text(
            text = stringResource(Res.string.description_auth),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        ApiKeyField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            placeholder = stringResource(Res.string.change_api_key),
            onSubmit = remember(onSubmitApiKey) {
                {
                    focusManager.clearFocus()
                    onSubmitApiKey()
                }
            },
            submitEnabled = !isLoading,
            submitContentDescription = stringResource(Res.string.desc_submit_api_key),
            isSaved = isApiKeySaved,
            savedText = stringResource(Res.string.api_key_changed)
        )
    }
}

@Composable
private fun ThemeSection(
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
            ),
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            icon = Icons.Default.Palette,
            title = stringResource(Res.string.title_theme),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(size = 24.dp))
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DangerSection(
    onRequestSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            icon = Icons.Default.Warning,
            title = stringResource(Res.string.title_danger),
        )
        Button(
            onClick = onRequestSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(size = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(Res.string.sign_out))
                Text(
                    text = stringResource(Res.string.button_desc_sign_out),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SettingsTopAppBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(Res.string.title_settings)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.ArrowBack,
                    contentDescription = stringResource(CoreRes.string.desc_go_back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
    )
}

private data class ThemeOptionData(
    val theme: AppTheme,
    val icon: ImageVector,
    val name: StringResource,
    val contentDescription: StringResource,
)

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    VoidTheme {
        SettingsScreenContent(
            state = SettingsStore.State(
                apiKey = "",
                isApiKeySaved = false,
                loading = null
            ),
            onBackClick = {},
            onApiKeyChange = {},
            onSubmitApiKey = {},
            onChangeTheme = {},
            onConfirmSignOut = {},
            onRequestSignOut = {},
            onDismissDialog = {},
        )
    }
}
