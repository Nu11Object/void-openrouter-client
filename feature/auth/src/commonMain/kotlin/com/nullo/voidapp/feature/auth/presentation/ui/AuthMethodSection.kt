package com.nullo.voidapp.feature.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.component.ApiKeyField
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.ShieldLock
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.cancel_sign_in
import voidapp.feature.auth.generated.resources.key_auth_description
import voidapp.feature.auth.generated.resources.oauth_description
import voidapp.feature.auth.generated.resources.or
import voidapp.feature.auth.generated.resources.paste_api_key
import voidapp.feature.auth.generated.resources.sign_in_via_open_router
import voidapp.feature.auth.generated.resources.submit_api_key

private val maxButtonAndTextFieldWidth = 350.dp

@Composable
internal fun AuthMethodSection(
    isLoading: Boolean,
    isAuthCompleted: Boolean,
    isOAuthInProgress: Boolean,
    onOAuthClick: () -> Unit,
    onCancelOAuthClick: () -> Unit,
    apiKey: String,
    onApiKeyChanged: (String) -> Unit,
    onApiKeySubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .ticketShadow()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(32.dp)
            )
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignInViaOpenRouterOption(
            modifier = Modifier.padding(horizontal = 32.dp),
            isLoading = isLoading,
            isAuthCompleted = isAuthCompleted,
            isOAuthInProgress = isOAuthInProgress,
            onOAuthClick = onOAuthClick,
            onOAuthCancelClick = onCancelOAuthClick
        )
        OrHorizontalDivider()
        SignWithApiKeyOption(
            modifier = Modifier.padding(horizontal = 32.dp),
            apiKey = apiKey,
            isLoading = isLoading,
            onApiKeyChanged = onApiKeyChanged,
            onApiKeySubmit = onApiKeySubmit
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SignInViaOpenRouterOption(
    isLoading: Boolean,
    isAuthCompleted: Boolean,
    isOAuthInProgress: Boolean,
    onOAuthClick: () -> Unit,
    onOAuthCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier
                .widthIn(max = maxButtonAndTextFieldWidth)
                .pointerHoverIcon(PointerIcon.Hand),
            onClick = if (isOAuthInProgress) onOAuthCancelClick else onOAuthClick,
            enabled = (!isLoading || isOAuthInProgress) && !isAuthCompleted,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (isOAuthInProgress) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary
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
            thickness = 2.dp
        )
        Text(
            text = stringResource(Res.string.or),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
            thickness = 2.dp
        )
    }
}

@Composable
private fun SignWithApiKeyOption(
    apiKey: String,
    isLoading: Boolean,
    onApiKeyChanged: (String) -> Unit,
    onApiKeySubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiKeyField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            placeholder = stringResource(Res.string.paste_api_key),
            onSubmit = onApiKeySubmit,
            submitEnabled = !isLoading && apiKey.isNotBlank(),
            submitContentDescription = stringResource(Res.string.submit_api_key),
            modifier = Modifier.widthIn(max = maxButtonAndTextFieldWidth)
        )
        Text(
            text = stringResource(Res.string.key_auth_description),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
