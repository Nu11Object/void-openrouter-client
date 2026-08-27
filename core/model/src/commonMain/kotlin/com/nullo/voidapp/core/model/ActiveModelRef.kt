package com.nullo.voidapp.core.model

sealed interface ActiveModelRef {
    val id: String

    data class Chat(override val id: String) : ActiveModelRef
    data class Image(override val id: String) : ActiveModelRef
}
