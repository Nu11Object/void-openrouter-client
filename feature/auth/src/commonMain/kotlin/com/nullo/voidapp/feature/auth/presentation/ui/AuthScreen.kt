package com.nullo.voidapp.feature.auth.presentation.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.component.VoidTextField
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.Logo
import com.nullo.voidapp.core.designsystem.icon.automirrored.ArrowForward
import com.nullo.voidapp.core.designsystem.icon.default.ShieldLock
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore
import com.nullo.voidapp.feature.auth.presentation.store.AuthStore.State.Completion
import org.koin.compose.getKoin

private val maxButtonAndTextFieldWidth = 350.dp

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authComponent: AuthComponent,
) {
    val model by authComponent.model.subscribeAsState()
    val storage = getKoin().get<ApiKeyStorage>() // todo: remove after testing

    LaunchedEffect(Unit) {
        storage.clear()
    }

    AuthScreenContent(
        modifier = modifier,
        state = model,
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

    LaunchedEffect(state.completion) {
        when (state.completion) {
            null -> Unit
            Completion.Instant -> onAuthFinish()
            Completion.Animated -> {
                keyboardController?.hide()
                ticketExitAnimation.animateExit()
                onAuthFinish()
            }
        }
    }

    val bottomTicketContent: @Composable (Modifier) -> Unit = { modifier ->
        AuthMethodSection(
            modifier = modifier,
            isLoading = state.isLoading,
            isAuthCompleted = state.completion != null,
            isOAuthInProgress = state.isOAuthInProgress,
            onOAuthClick = onOAuthClick,
            onCancelOAuthClick = onCancelOAuthClick,
            apiKey = state.apiKey,
            onApiKeyChanged = onApiKeyChange,
            onApiKeySubmit = onApiKeySubmit,
        )
    }

    Scaffold(modifier = modifier) { innerPadding ->
        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT,
            DeviceConfiguration.TABLET_PORTRAIT -> {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .glowOnExit(
                            progress = { ticketExitAnimation.linearProgress },
                            brush = glowBrush
                        )
                        .padding(innerPadding)
                        .padding(32.dp),
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
                    modifier = Modifier
                        .padding(innerPadding)
                        .glowOnExit(
                            progress = { ticketExitAnimation.linearProgress },
                            brush = glowBrush
                        )
                        .padding(32.dp)
                        .fillMaxSize()
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
            title = "Oops",
            onDismissRequest = onDismissError,
            confirmButton = VoidDialogButton(
                text = "Ok",
                onClick = onDismissError
            ),
            message = errorMessage,
        )
    }
}

@Composable
private fun WelcomeSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceDim,
                shape = RoundedCornerShape(32.dp),
            )
            .padding(vertical = 24.dp)
            .clearAndSetSemantics {
                contentDescription = "Welcome to the Void"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Welcome", style = MaterialTheme.typography.displaySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "to the", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Image(imageVector = Icons.Logo, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(text = "Void", style = MaterialTheme.typography.titleMedium)
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
                text = if (isOAuthInProgress) "Cancel Sign In" else "Sign in via OpenRouter",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "The app will receive a temporary API key",
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
            text = "OR",
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
            modifier = Modifier.widthIn(max = maxButtonAndTextFieldWidth),
            value = apiKey,
            onValueChange = onApiKeyChanged,
            onSubmit = onApiKeySubmit,
            submitEnabled = !isLoading && apiKey.isNotBlank()
        )
        Text(
            text = "The key will be encrypted and saved locally",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ApiKeyField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
) {
    VoidTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        placeholder = {
            Text(
                text = "Paste API key",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            IconButton(
                enabled = submitEnabled,
                onClick = onSubmit,
                colors = IconButtonDefaults.filledIconButtonColors(),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.ArrowForward,
                    contentDescription = "Submit API key",
                )
            }
        }
    )
}

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
