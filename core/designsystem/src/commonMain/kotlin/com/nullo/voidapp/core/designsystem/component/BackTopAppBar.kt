package com.nullo.voidapp.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.ArrowBack
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.desc_go_back
import voidapp.core.utils.generated.resources.Res as CoreRes

/**
 * A top app bar component with a title and a back navigation button.
 *
 * @param title The title text to be displayed in the app bar.
 * @param onBackClick Called when the back icon is clicked.
 */
@Composable
fun BackTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = { },
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.ArrowBack,
                    contentDescription = stringResource(CoreRes.string.desc_go_back)
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
    )
}
