package com.nullo.voidapp.core.utils

import com.arkivanov.mvikotlin.core.rx.Disposable
import com.arkivanov.mvikotlin.core.rx.Observer
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.utils.mvikotlin.asValue
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private class FakeStore<State : Any>(initialState: State) : Store<Any, State, Any> {

    override var state: State = initialState
        private set

    override var isDisposed: Boolean = false
        private set

    private val stateObservers = mutableListOf<Observer<State>>()
    private val subscriptions = mutableListOf<Disposable>()

    override fun states(observer: Observer<State>): Disposable {
        stateObservers += observer
        observer.onNext(state)

        return Disposable {
            stateObservers -= observer
        }.also { subscriptions += it }
    }

    override fun labels(observer: Observer<Any>): Disposable = Disposable()

    override fun accept(intent: Any) = Unit

    override fun init() = Unit

    override fun dispose() {
        isDisposed = true
        stateObservers.clear()
    }

    fun emit(value: State) {
        state = value
        stateObservers.toList().forEach { it.onNext(value) }
    }

    fun activeSubscriptionCount(): Int = subscriptions.count { !it.isDisposed }
}

val ValueExtTest by testSuite {
    test("value exposes the current store state") {
        FakeStore(1).asValue().value shouldBe 1
    }

    test("value reflects state changes made before subscription") {
        val store = FakeStore(1)
        val value = store.asValue()

        store.emit(2)

        value.value shouldBe 2
    }

    test("subscription receives the current state immediately") {
        val received = mutableListOf<Int>()

        FakeStore(1).asValue().subscribe { received += it }

        received shouldBe listOf(1)
    }

    test("subscription receives subsequent state emissions") {
        val store = FakeStore(1)
        val received = mutableListOf<Int>()

        store.asValue().subscribe { received += it }
        store.emit(2)
        store.emit(3)

        received shouldBe listOf(1, 2, 3)
    }

    test("multiple subscribers receive state emissions") {
        val store = FakeStore(1)
        val first = mutableListOf<Int>()
        val second = mutableListOf<Int>()
        val value = store.asValue()

        value.subscribe { first += it }
        value.subscribe { second += it }
        store.emit(2)

        first shouldBe listOf(1, 2)
        second shouldBe listOf(1, 2)
    }

    test("cancellation disposes the store subscription") {
        val store = FakeStore(1)
        val received = mutableListOf<Int>()
        val cancellation = store.asValue().subscribe { received += it }

        cancellation.cancel()

        store.activeSubscriptionCount() shouldBe 0
        store.emit(2)
        received shouldBe listOf(1)
    }

    test("cancelling one subscription keeps the other one active") {
        val store = FakeStore(1)
        val first = mutableListOf<Int>()
        val second = mutableListOf<Int>()
        val value = store.asValue()

        val firstCancellation = value.subscribe { first += it }
        value.subscribe { second += it }
        firstCancellation.cancel()
        store.emit(2)

        store.activeSubscriptionCount() shouldBe 1
        first shouldBe listOf(1)
        second shouldBe listOf(1, 2)
    }
}
