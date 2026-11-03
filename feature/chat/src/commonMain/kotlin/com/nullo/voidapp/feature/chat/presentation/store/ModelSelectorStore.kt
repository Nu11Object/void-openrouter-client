package com.nullo.voidapp.feature.chat.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel

interface ModelSelectorStore : Store<ModelSelectorStore.Intent, ModelSelectorStore.State, Nothing> {

    @Immutable
    data class State(
        val isModelSelectorOpen: Boolean = false,
        val modelSelectorTab: SelectorTab = SelectorTab.ChatModels,
        val modelSearchQuery: String = "",
        val pinnedChatModels: List<ChatModel> = emptyList(),
        val pinnedImageModels: List<ImageModel> = emptyList(),
    ) {

        @Immutable
        sealed interface SelectorTab {
            data object ChatModels : SelectorTab
            data object ImageModels : SelectorTab
        }
    }

    sealed interface Intent {

        data object OpenModelSelector : Intent
        data object CloseModelSelector : Intent

        data class SelectModel(val activeModelRef: ActiveModelRef) : Intent
        data class UnpinModel(
            val modelId: String,
            val modelType: ModelType,
        ) : Intent

        data class ChangeModelSelectorTab(val tab: State.SelectorTab) : Intent
        data class ChangeModelSearchQuery(val query: String) : Intent
    }
}
