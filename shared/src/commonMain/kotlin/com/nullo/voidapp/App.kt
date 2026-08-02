package com.nullo.voidapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nullo.voidapp.feature.root.component.RootComponent
import com.nullo.voidapp.feature.root.component.RootContent

@Composable
fun App(
    rootComponent: RootComponent,
    modifier: Modifier = Modifier,
    windowTitleBar: (@Composable () -> Unit)? = null
) {
    RootContent(
        rootComponent = rootComponent,
        modifier = modifier,
        windowTitleBar = windowTitleBar
    )
}
