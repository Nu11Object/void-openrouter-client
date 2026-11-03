package com.nullo.voidapp.feature.chat.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.utils.resources.UiText

interface SideMenuStore : Store<SideMenuStore.Intent, SideMenuStore.State, Nothing> {

    @Immutable
    data class State(
        val conversations: List<Conversation> = emptyList(),
        val conversationSearchQuery: String = "",
        val activeConversationId: Long? = null,

        val renamingConversationId: Long? = null,
        val renamingConversationInput: String = "",

        val errorDialogText: UiText? = null,
    )

    sealed interface Intent {

        data class ChangeConversationSearchQuery(val query: String) : Intent
        data class OpenConversation(val conversationId: Long) : Intent
        data object StartNewConversation : Intent
        data class DeleteConversation(val conversationId: Long) : Intent

        data class OpenRenameConversationDialog(val conversationId: Long) : Intent
        data object CloseRenameConversationDialog : Intent
        data class ChangeConversationNameInput(val input: String) : Intent
        data object ConfirmConversationRename : Intent

        data object DismissErrorDialog : Intent
    }
}
