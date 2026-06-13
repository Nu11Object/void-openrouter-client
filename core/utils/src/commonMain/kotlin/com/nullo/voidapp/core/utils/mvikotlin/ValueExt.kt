package com.nullo.voidapp.core.utils.mvikotlin

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.rx.observer
import com.arkivanov.mvikotlin.core.store.Store

/**
 * Converts MVIKotlin [Store] into a Decompose [Value].
 * Automatically manages state subscriptions based on the [Value] lifecycle.
 */
fun <State : Any> Store<*, State, *>.asValue(): Value<State> = object : Value<State>() {

    override val value: State get() = state

    override fun subscribe(observer: (State) -> Unit): Cancellation {
        val disposable = states(observer(onNext = observer))

        return Cancellation {
            disposable.dispose()
        }
    }
}
