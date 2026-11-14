package com.nullo.voidapp.core.utils

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.nullo.voidapp.core.utils.decompose.componentScope
import de.infix.testBalloon.framework.core.testSuite
import de.infix.testBalloon.framework.core.withMainDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher

private suspend fun withTestMainDispatcher(action: suspend () -> Unit) {
    withMainDispatcher(StandardTestDispatcher(), action)
}

val ComponentExtTest by testSuite {
    test("componentScope creates an active scope") {
        withTestMainDispatcher {
            val lifecycle = LifecycleRegistry()
            lifecycle.create()

            val scope = DefaultComponentContext(lifecycle).componentScope()

            scope.coroutineContext[Job]?.isActive shouldBe true
        }
    }

    test("componentScope cancels the scope when the component is destroyed") {
        withTestMainDispatcher {
            val lifecycle = LifecycleRegistry()
            lifecycle.create()
            val scope = DefaultComponentContext(lifecycle).componentScope()

            lifecycle.destroy()

            scope.coroutineContext[Job]?.isCancelled shouldBe true
        }
    }

    test("componentScope of one component does not cancel another component scope") {
        withTestMainDispatcher {
            val firstLifecycle = LifecycleRegistry()
            val secondLifecycle = LifecycleRegistry()
            firstLifecycle.create()
            secondLifecycle.create()
            val firstScope = DefaultComponentContext(firstLifecycle).componentScope()
            val secondScope = DefaultComponentContext(secondLifecycle).componentScope()

            firstLifecycle.destroy()

            firstScope.coroutineContext[Job]?.isCancelled shouldBe true
            secondScope.coroutineContext[Job]?.isActive shouldBe true
        }
    }
}
