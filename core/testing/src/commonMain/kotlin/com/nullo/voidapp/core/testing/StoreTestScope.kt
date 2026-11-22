package com.nullo.voidapp.core.testing

import de.infix.testBalloon.framework.core.Test
import de.infix.testBalloon.framework.core.withMainDispatcher
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

class StoreTestScope(
    val backgroundScope: CoroutineScope,
    private val scheduler: TestCoroutineScheduler,
) {

    fun idle() {
        scheduler.runCurrent()
    }

    fun advanceBy(duration: Duration) {
        scheduler.advanceTimeBy(duration)
        scheduler.runCurrent()
    }
}

suspend fun Test.ExecutionScope.withStoreTestScope(block: suspend StoreTestScope.() -> Unit) {
    withMainDispatcher(StandardTestDispatcher(testScope.testScheduler)) {
        val storeTestScope = StoreTestScope(
            backgroundScope = testScope.backgroundScope,
            scheduler = testScope.testScheduler
        )
        storeTestScope.block()
    }
}
