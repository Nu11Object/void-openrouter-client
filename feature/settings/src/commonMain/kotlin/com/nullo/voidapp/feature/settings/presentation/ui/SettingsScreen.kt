package com.nullo.voidapp.feature.settings.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nullo.voidapp.core.designsystem.component.BackTopAppBar
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.settings.presentation.component.SettingsComponent
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Dialog
import com.nullo.voidapp.feature.settings.presentation.store.SettingsStore.State.Loading
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.cancel
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.feature.settings.generated.resources.Res
import voidapp.feature.settings.generated.resources.description_sign_out
import voidapp.feature.settings.generated.resources.question_sign_out
import voidapp.feature.settings.generated.resources.sign_out
import voidapp.feature.settings.generated.resources.title_settings

@Composable
fun SettingsScreen(
    settingsComponent: SettingsComponent,
    modifier: Modifier = Modifier,
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
    state: SettingsStore.State,
    onBackClick: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSubmitApiKey: () -> Unit,
    onChangeTheme: (AppTheme) -> Unit,
    onRequestSignOut: () -> Unit,
    onConfirmSignOut: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
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
        topBar = {
            BackTopAppBar(
                title = stringResource(Res.string.title_settings),
                onBackClick = onBackClick
            )
        }
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
        SettingsDialog(
            dialog = dialog,
            onConfirmSignOut = onConfirmSignOut,
            onDismiss = onDismissDialog
        )
    }

    LoadingOverlay(visible = state.loading == Loading.FullScreen)
}

@Composable
private fun SettingsDialog(
    dialog: Dialog,
    onConfirmSignOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        Dialog.SignOutConfirmation -> VoidAlertDialog(
            title = stringResource(Res.string.question_sign_out),
            message = stringResource(Res.string.description_sign_out),
            onDismissRequest = onDismiss,
            confirmButton = VoidDialogButton(
                text = stringResource(Res.string.sign_out),
                textColor = MaterialTheme.colorScheme.error,
                onClick = onConfirmSignOut
            ),
            dismissButton = VoidDialogButton(
                text = stringResource(CoreRes.string.cancel),
                onClick = onDismiss
            )
        )

        is Dialog.Error -> VoidAlertDialog(
            title = stringResource(CoreRes.string.oops),
            message = dialog.message.asString(),
            onDismissRequest = onDismiss,
            confirmButton = VoidDialogButton(
                text = stringResource(CoreRes.string.ok),
                onClick = onDismiss
            )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

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
            onDismissDialog = {}
        )
    }
}
