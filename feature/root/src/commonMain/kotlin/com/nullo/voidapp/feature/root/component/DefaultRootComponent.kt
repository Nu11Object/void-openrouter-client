package com.nullo.voidapp.feature.root.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.core.designsystem.theme.AppTheme
import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import com.nullo.voidapp.core.utils.decompose.componentScope
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import com.nullo.voidapp.feature.settings.presentation.component.SettingsComponent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

internal class DefaultRootComponent(
    private val componentContext: ComponentContext,
    private val authComponentFactory: AuthComponent.Factory,
    private val settingsComponentFactory: SettingsComponent.Factory,
    private val settingsRepository: SettingsRepository,
    private val apiKeyStorage: ApiKeyStorage,
) : RootComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    override val appTheme: StateFlow<AppTheme?> = settingsRepository.appTheme
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Initial,
        handleBackButton = true,
        childFactory = ::child
    )

    init {
        if (stack.value.active.configuration == Config.Initial) {
            scope.launch {
                val hasKey = apiKeyStorage.hasApiKey().first()
                val target = if (hasKey) Config.Settings else Config.Auth
                navigation.replaceCurrent(target)
            }
        }
    }

    fun child(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            Config.Auth -> {
                val component = authComponentFactory.create(
                    onAuthFinished = {
                        // todo: navigate to main screen
                        navigation.replaceCurrent(Config.Settings)
                    },
                    componentContext = componentContext
                )
                RootComponent.Child.Auth(component)
            }

            Config.Settings -> {
                val component = settingsComponentFactory.create(
                    onNavigateBack = navigation::pop,
                    onSignedOut = { navigation.replaceAll(Config.Auth) },
                    componentContext = componentContext,
                )
                RootComponent.Child.Settings(component)
            }

            Config.Initial -> RootComponent.Child.Initial
        }
    }

    @Serializable
    sealed interface Config {

        @Serializable
        data object Initial : Config

        @Serializable
        data object Auth : Config

        @Serializable
        data object Settings : Config
    }
}
