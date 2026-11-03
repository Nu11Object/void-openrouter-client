package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.utils.kotlin.debouncedQuery
import com.nullo.voidapp.core.utils.kotlin.filterWithQuery
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.chat.domain.DeleteConversationUseCase
import com.nullo.voidapp.feature.chat.domain.FindConversationIdsByTextUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveConversationsUseCase
import com.nullo.voidapp.feature.chat.domain.RenameConversationUseCase
import com.nullo.voidapp.feature.chat.presentation.store.SideMenuStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.SideMenuStore.State
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.error_blank_conversation_name
import voidapp.feature.chat.generated.resources.error_cannot_access_conversation
import voidapp.feature.chat.generated.resources.error_cannot_rename_conversation

internal class SideMenuStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeConversationsUseCase: ObserveConversationsUseCase,
    private val findConversationIdsByTextUseCase: FindConversationIdsByTextUseCase,
    private val renameConversationUseCase: RenameConversationUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
) : StoreFactory by storeFactory {

    fun create(activeConversation: ActiveConversationHolder): SideMenuStore {
        // Search query from SideMenuStore.State is used for UI only!
        val conversationSearchQuery = MutableStateFlow("")

        return object :
            SideMenuStore,
            Store<Intent, State, Nothing> by storeFactory.create(
                name = "SideMenuStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(conversationSearchQuery, activeConversation),
                executorFactory = { ExecutorImpl(conversationSearchQuery, activeConversation) },
                reducer = ReducerImpl
            ) {}
    }

    private sealed interface Msg {

        data class ConversationsUpdated(val conversations: List<Conversation>) : Msg
        data class ActiveConversationIdChanged(val conversationId: Long?) : Msg
        data class ConversationSearchQueryChanged(val query: String) : Msg

        data class RenameConversationDialogOpened(val conversationId: Long) : Msg
        data object RenameConversationDialogClosed : Msg
        data class ConversationNameInputChanged(val input: String) : Msg

        data class ErrorOccurred(val text: UiText) : Msg
        data object ErrorDialogDismissed : Msg
    }

    private sealed interface Action {

        data class UpdateConversations(val conversations: List<Conversation>) : Action
        data class UpdateActiveConversationId(val conversationId: Long?) : Action
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private inner class BootstrapperImpl(
        private val conversationSearchQuery: Flow<String>,
        private val activeConversation: ActiveConversationHolder,
    ) : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observeConversations()
            observeActiveConversationId()
        }

        private fun observeConversations() {
            val matchedIds = conversationSearchQuery
                .debouncedQuery(SEARCH_QUERY_DEBOUNCE)
                .flatMapLatest { query ->
                    flow { emit(query to findConversationIdsByTextUseCase(query)) }
                }

            combine(observeConversationsUseCase(), matchedIds) { conversations, (query, ids) ->
                val titleMatches = conversations
                    .filterWithQuery(query = query, filterBy = { it.title.orEmpty() })
                    .mapTo(HashSet()) { it.id }
                conversations.filter { it.id in titleMatches || it.id in ids }
            }
                .onEach { dispatch(Action.UpdateConversations(it)) }
                .launchIn(scope)
        }

        private fun observeActiveConversationId() {
            activeConversation.id
                .onEach { dispatch(Action.UpdateActiveConversationId(it)) }
                .launchIn(scope)
        }
    }

    private inner class ExecutorImpl(
        private val conversationSearchQuery: MutableStateFlow<String>,
        private val activeConversation: ActiveConversationHolder,
    ) : CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.UpdateConversations -> dispatch(
                    Msg.ConversationsUpdated(action.conversations)
                )

                is Action.UpdateActiveConversationId -> dispatch(
                    Msg.ActiveConversationIdChanged(action.conversationId)
                )
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.ChangeConversationSearchQuery -> {
                    dispatch(Msg.ConversationSearchQueryChanged(intent.query))
                    conversationSearchQuery.update { intent.query }
                }

                is Intent.OpenConversation -> activeConversation.set(intent.conversationId)

                Intent.StartNewConversation -> activeConversation.set(null)

                is Intent.DeleteConversation -> scope.launch {
                    deleteConversationUseCase(intent.conversationId)
                }

                is Intent.OpenRenameConversationDialog -> {
                    dispatch(Msg.RenameConversationDialogOpened(intent.conversationId))
                }

                Intent.CloseRenameConversationDialog -> {
                    dispatch(Msg.RenameConversationDialogClosed)
                }

                is Intent.ChangeConversationNameInput -> {
                    dispatch(Msg.ConversationNameInputChanged(intent.input))
                }

                Intent.ConfirmConversationRename -> scope.launch {
                    val state = state()
                    val id = state.renamingConversationId ?: run {
                        dispatchError(UiText(Res.string.error_cannot_access_conversation))
                        return@launch
                    }
                    val newTitle = state.renamingConversationInput.ifBlank {
                        dispatchError(UiText(Res.string.error_blank_conversation_name))
                        return@launch
                    }

                    try {
                        renameConversationUseCase(id, newTitle)
                        dispatch(Msg.RenameConversationDialogClosed)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        dispatchError(UiText(Res.string.error_cannot_rename_conversation))
                    }
                }

                Intent.DismissErrorDialog -> dispatch(Msg.ErrorDialogDismissed)
            }
        }

        private fun dispatchError(text: UiText) {
            dispatch(Msg.ErrorOccurred(text))
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.ConversationsUpdated -> copy(
                conversations = msg.conversations
            )

            is Msg.ActiveConversationIdChanged -> copy(
                activeConversationId = msg.conversationId
            )

            is Msg.ConversationSearchQueryChanged -> copy(
                conversationSearchQuery = msg.query
            )

            is Msg.RenameConversationDialogOpened -> copy(
                renamingConversationId = msg.conversationId
            )

            Msg.RenameConversationDialogClosed -> copy(
                renamingConversationId = null,
                renamingConversationInput = ""
            )

            is Msg.ConversationNameInputChanged -> copy(
                renamingConversationInput = msg.input
            )

            is Msg.ErrorOccurred -> copy(
                errorDialogText = msg.text
            )

            Msg.ErrorDialogDismissed -> copy(
                errorDialogText = null
            )
        }
    }

    companion object {

        private val SEARCH_QUERY_DEBOUNCE = 300.milliseconds
    }
}
