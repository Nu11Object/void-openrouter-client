package com.nullo.voidapp.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.feature.auth.presentation.component.AuthComponent
import kotlinx.serialization.Serializable

internal class DefaultRootComponent(
    private val componentContext: ComponentContext,
    private val authComponentFactory: AuthComponent.Factory,
) : RootComponent, ComponentContext by componentContext {

    val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Auth,
        handleBackButton = true,
        childFactory = ::child
    )

    fun child(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            Config.Auth -> {
                val component = authComponentFactory.create(
                    onAuthFinished = {
                        // todo: navigate to main screen
                        println("Auth finished")
                    },
                    componentContext = componentContext
                )
                RootComponent.Child.Auth(component)
            }
        }
    }

    @Serializable
    sealed interface Config {

        @Serializable
        data object Auth : Config
    }
}
