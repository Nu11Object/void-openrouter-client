package com.nullo.voidapp.feature.auth.data

import kotlinx.coroutines.channels.Channel

internal class OAuthCallbackDispatcher {

    private val channel = Channel<Result<String>>(capacity = Channel.CONFLATED)

    fun onCallbackUrl(url: String) {
        channel.trySend(Result.success(url))
    }

    fun reset() {
        channel.tryReceive()
    }

    suspend fun awaitCallback(): String = channel.receive().getOrThrow()
}