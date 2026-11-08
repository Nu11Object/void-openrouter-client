package com.nullo.voidapp.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.component.ApiKeyField
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Key
import com.nullo.voidapp.core.designsystem.icon.default.Warning
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.settings.generated.resources.Res
import voidapp.feature.settings.generated.resources.api_key_changed
import voidapp.feature.settings.generated.resources.button_desc_sign_out
import voidapp.feature.settings.generated.resources.change_api_key
import voidapp.feature.settings.generated.resources.desc_submit_api_key
import voidapp.feature.settings.generated.resources.description_auth
import voidapp.feature.settings.generated.resources.sign_out
import voidapp.feature.settings.generated.resources.title_auth
import voidapp.feature.settings.generated.resources.title_danger

@Composable
internal fun AuthSection(
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
            title = stringResource(Res.string.title_auth)
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
internal fun DangerSection(
    onRequestSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            icon = Icons.Default.Warning,
            title = stringResource(Res.string.title_danger)
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
            shape = MaterialTheme.shapes.medium
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
internal fun SectionHeader(
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
