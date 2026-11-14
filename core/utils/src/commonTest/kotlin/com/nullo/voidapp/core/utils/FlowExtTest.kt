package com.nullo.voidapp.core.utils

import com.nullo.voidapp.core.utils.kotlin.debouncedQuery
import com.nullo.voidapp.core.utils.kotlin.mapItems
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

val FlowExtTest by testSuite {
    testSuite("mapItems") {
        test("maps every item of every list") {
            flowOf(listOf(1, 2, 3), listOf(4, 5))
                .mapItems { it * 2 }
                .toList() shouldBe listOf(listOf(2, 4, 6), listOf(8, 10))
        }

        test("preserves empty lists") {
            flowOf(listOf(1), emptyList(), listOf(2))
                .mapItems { it.toString() }
                .toList() shouldBe listOf(listOf("1"), emptyList(), listOf("2"))
        }
    }

    testSuite("debouncedQuery") {
        test("emits empty queries immediately") {
            flowOf("", "", "").debouncedQuery(1.seconds).toList() shouldBe listOf("", "", "")
        }

        test("emits only the last query of a quick burst") {
            flowOf("a", "ab", "abc").debouncedQuery(100.milliseconds).toList() shouldBe listOf("abc")
        }

        test("emits queries spaced by more than the duration") {
            val result = flow {
                emit("a")
                delay(200.milliseconds)
                emit("b")
            }.debouncedQuery(100.milliseconds).toList()

            result shouldBe listOf("a", "b")
        }

        test("drops a query replaced within the duration") {
            val result = flow {
                emit("a")
                delay(50.milliseconds)
                emit("b")
            }.debouncedQuery(100.milliseconds).toList()

            result shouldBe listOf("b")
        }

        test("emits an empty query immediately and drops the pending query") {
            val result = flow {
                emit("a")
                emit("")
                emit("b")
            }.debouncedQuery(100.milliseconds).toList()

            result shouldBe listOf("", "b")
        }

        test("does not emit anything for an empty flow") {
            flowOf<String>().debouncedQuery(100.milliseconds).toList() shouldBe emptyList()
        }
    }
}
