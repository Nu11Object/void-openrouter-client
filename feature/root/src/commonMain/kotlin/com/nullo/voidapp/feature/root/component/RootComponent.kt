package com.nullo.voidapp.feature.root.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import com.nullo.voidapp.feature.catalog.presentation.component.CatalogComponent
import com.nullo.voidapp.feature.settings.presentation.component.SettingsComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    val appTheme: StateFlow<AppTheme?>

    sealed interface Child {

        data object Initial : Child

        data class Auth(val component: AuthComponent) : Child

        data class Settings(val component: SettingsComponent) : Child

        data class Catalog(val component: CatalogComponent) : Child
    }
}
