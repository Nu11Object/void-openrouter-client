package com.nullo.voidapp.core.utils.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Creates a [CoroutineScope] bound to the [ComponentContext] lifecycle.
 * The scope is automatically canceled when the component is destroyed.
 */
fun ComponentContext.componentScope() = CoroutineScope(
    SupervisorJob() + Dispatchers.Main.immediate
).also {
    lifecycle.doOnDestroy { it.cancel() }
}
