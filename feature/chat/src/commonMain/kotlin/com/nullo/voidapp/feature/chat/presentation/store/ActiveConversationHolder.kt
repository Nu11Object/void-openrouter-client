package com.nullo.voidapp.feature.chat.presentation.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** ID of the opened conversation shared between stores (null = new, not created yet). */
internal class ActiveConversationHolder {

    private val _id = MutableStateFlow<Long?>(null)
    val id: StateFlow<Long?> = _id.asStateFlow()

    fun set(id: Long?) {
        _id.value = id
    }
}
