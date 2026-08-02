package com.nullo.voidapp.feature.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nullo.voidapp.core.designsystem.component.ApiKeyField
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Logo
import com.nullo.voidapp.core.designsystem.icon.default.ShieldLock
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.cancel_sign_in
import voidapp.feature.auth.generated.resources.desc_welcome_to_the_void
import voidapp.feature.auth.generated.resources.key_auth_description
import voidapp.feature.auth.generated.resources.oauth_description
import voidapp.feature.auth.generated.resources.or
import voidapp.feature.auth.generated.resources.paste_api_key
import voidapp.feature.auth.generated.resources.sign_in_via_open_router
import voidapp.feature.auth.generated.resources.submit_api_key
import voidapp.feature.auth.generated.resources.to_the
import voidapp.feature.auth.generated.resources.void
import voidapp.feature.auth.generated.resources.welcome
import voidapp.core.utils.generated.resources.Res as CoreRes

private val maxButtonAndTextFieldWidth = 350.dp

@Composable
fun AuthScreen(
    authComponent: AuthComponent,
    modifier: Modifier = Modifier,
) {
    val state by authComponent.model.subscribeAsState()

    AuthScreenContent(
        modifier = modifier,
        state = state,
        onOAuthClick = authComponent::onOAuthClicked,
        onCancelOAuthClick = authComponent::onCancelOAuthClicked,
        onApiKeyChange = authComponent::onApiKeyInputChanged,
        onApiKeySubmit = authComponent::onSubmitApiKeyClicked,
        onAuthFinish = authComponent::onAuthFinished,
        onDismissError = authComponent::onDismissErrorClicked
    )
}

@Composable
private fun AuthScreenContent(
    modifier: Modifier = Modifier,
    state: AuthStore.State,
    onOAuthClick: () -> Unit,
    onCancelOAuthClick: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onApiKeySubmit: () -> Unit,
    onAuthFinish: () -> Unit,
    onDismissError: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val deviceConfiguration = rememberDeviceConfiguration()

    val density = LocalDensity.current
    val ticketExitAnimation = rememberTicketExitAnimation()

    val primaryColor = MaterialTheme.colorScheme.primary
    val glowBrush = remember(primaryColor) {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                primaryColor,
            )
        )
    }

    LaunchedEffect(state.isAuthCompleted) {
        if (state.isAuthCompleted) {
            keyboardController?.hide()
            ticketExitAnimation.animateExit()
            onAuthFinish()
        }
    }

    val bottomTicketContent: @Composable (Modifier) -> Unit = { modifier ->
        AuthMethodSection(
            modifier = modifier,
            isLoading = state.isLoading,
            isAuthCompleted = state.isAuthCompleted,
            isOAuthInProgress = state.isOAuthInProgress,
            onOAuthClick = onOAuthClick,
            onCancelOAuthClick = onCancelOAuthClick,
            apiKey = state.apiKey,
            onApiKeyChanged = onApiKeyChange,
            onApiKeySubmit = onApiKeySubmit,
        )
    }

    Scaffold(modifier = modifier) { innerPadding ->
        val rootModifier = Modifier
            .glowOnExit(
                progress = { ticketExitAnimation.linearProgress },
                brush = glowBrush
            )
            .padding(innerPadding)
            .padding(32.dp)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT,
            DeviceConfiguration.TABLET_PORTRAIT -> {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(rootModifier),
                    verticalArrangement = Arrangement.Center,
                ) {
                    WelcomeSection(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = with(density) {
                                    ticketExitAnimation.topOffset.toPx()
                                }
                                rotationZ = ticketExitAnimation.topRotationDegrees
                            }
                            .fillMaxWidth()
                    )
                    bottomTicketContent(
                        Modifier
                            .graphicsLayer {
                                translationY = with(density) {
                                    ticketExitAnimation.bottomOffset.toPx()
                                }
                            }
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                    )
                }
            }

            DeviceConfiguration.MOBILE_LANDSCAPE,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Row(
                    modifier = rootModifier.fillMaxSize()
                ) {
                    WelcomeSection(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = with(density) {
                                    ticketExitAnimation.topOffset.toPx()
                                }
                            }
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    bottomTicketContent(
                        Modifier
                            .graphicsLayer {
                                translationX = with(density) {
                                    -ticketExitAnimation.topOffset.toPx()
                                }
                            }
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }

    state.error?.let { errorMessage ->
        VoidAlertDialog(
            title = stringResource(CoreRes.string.oops),
            onDismissRequest = onDismissError,
            confirmButton = VoidDialogButton(
                text = stringResource(CoreRes.string.ok),
                onClick = onDismissError
            ),
            message = errorMessage.asString(),
        )
    }
}

@Composable
private fun WelcomeSection(modifier: Modifier = Modifier) {
    val contentDescription = stringResource(Res.string.desc_welcome_to_the_void)
    Column(
        modifier = modifier
            .ticketShadow()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(32.dp),
            )
            .padding(vertical = 24.dp)
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.welcome),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.to_the),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Logo,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = null,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.void),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun AuthMethodSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isAuthCompleted: Boolean,
    isOAuthInProgress: Boolean,
    onOAuthClick: () -> Unit,
    onCancelOAuthClick: () -> Unit,
    apiKey: String,
    onApiKeyChanged: (String) -> Unit,
    onApiKeySubmit: () -> Unit,
) {
    Column(
        modifier = modifier
            .ticketShadow()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(32.dp),
            )
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SignInViaOpenRouterOption(
            modifier = Modifier.padding(horizontal = 32.dp),
            isLoading = isLoading,
            isAuthCompleted = isAuthCompleted,
            isOAuthInProgress = isOAuthInProgress,
            onOAuthClick = onOAuthClick,
            onOAuthCancelClick = onCancelOAuthClick,
        )
        OrHorizontalDivider()
        SignWithApiKeyOption(
            modifier = Modifier.padding(horizontal = 32.dp),
            apiKey = apiKey,
            isLoading = isLoading,
            onApiKeyChanged = onApiKeyChanged,
            onApiKeySubmit = onApiKeySubmit,
        )
    }

}

@Composable
private fun SignInViaOpenRouterOption(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isAuthCompleted: Boolean,
    isOAuthInProgress: Boolean,
    onOAuthClick: () -> Unit,
    onOAuthCancelClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            modifier = Modifier
                .widthIn(max = maxButtonAndTextFieldWidth)
                .pointerHoverIcon(PointerIcon.Hand),
            onClick = if (isOAuthInProgress) onOAuthCancelClick else onOAuthClick,
            enabled = (!isLoading || isOAuthInProgress) && !isAuthCompleted,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            if (isOAuthInProgress) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ShieldLock,
                    contentDescription = null
                )
            }
            Text(
                text = stringResource(
                    if (isOAuthInProgress) {
                        Res.string.cancel_sign_in
                    } else {
                        Res.string.sign_in_via_open_router
                    }
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(Res.string.oauth_description),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun OrHorizontalDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
            thickness = 2.dp,
        )
        Text(
            text = stringResource(Res.string.or),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
            thickness = 2.dp,
        )
    }
}

@Composable
private fun SignWithApiKeyOption(
    modifier: Modifier = Modifier,
    apiKey: String,
    isLoading: Boolean,
    onApiKeyChanged: (String) -> Unit,
    onApiKeySubmit: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ApiKeyField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            placeholder = stringResource(Res.string.paste_api_key),
            onSubmit = onApiKeySubmit,
            submitEnabled = !isLoading && apiKey.isNotBlank(),
            submitContentDescription = stringResource(Res.string.submit_api_key),
            modifier = Modifier.widthIn(max = maxButtonAndTextFieldWidth),
        )
        Text(
            text = stringResource(Res.string.key_auth_description),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun Modifier.ticketShadow(): Modifier = shadow(
    elevation = 32.dp,
    shape = RoundedCornerShape(32.dp),
    clip = false,
)

private fun Modifier.glowOnExit(
    progress: () -> Float,
    brush: Brush,
): Modifier = drawWithCache {
    onDrawBehind {
        val p = progress()
        if (p <= 0f) return@onDrawBehind
        withTransform(
            transformBlock = { translate(top = (1f - p) * size.height) },
            drawBlock = { drawRect(brush = brush) }
        )
    }
}

@PreviewLightDark
@Composable
private fun AuthScreenPreview() {
    VoidTheme {
        AuthScreenContent(
            state = AuthStore.State(
                apiKey = "",
                isLoading = false,
                error = null,
            ),
            onOAuthClick = {},
            onCancelOAuthClick = {},
            onApiKeyChange = {},
            onApiKeySubmit = {},
            onAuthFinish = {},
            onDismissError = {}
        )
    }
}
