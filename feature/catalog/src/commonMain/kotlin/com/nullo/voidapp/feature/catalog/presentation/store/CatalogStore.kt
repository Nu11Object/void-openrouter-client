package com.nullo.voidapp.feature.catalog.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.utils.resources.UiText

interface CatalogStore : Store<CatalogStore.Intent, CatalogStore.State, CatalogStore.Label> {

    @Immutable
    data class State(
        val searchQuery: String = "",
        val chatModels: List<ChatModel> = emptyList(),
        val imageModels: List<ImageModel> = emptyList(),
        val selectedTab: CatalogTab = CatalogTab.ChatModels,
        val showVerboseInfo: Boolean = true,
        val isLoading: Boolean = false,
        val error: UiText? = null,
        val message: UiText? = null,
    ) {

        sealed interface CatalogTab {

            data object ChatModels : CatalogTab
            data object ImageModels : CatalogTab
        }
    }

    sealed interface Intent {

        data object NavigateBack : Intent
        data object RefreshCatalog : Intent
        data class ChangeSearchQuery(val query: String) : Intent
        data class ChangeCatalogTab(val tab: State.CatalogTab) : Intent
        data object ToggleVerboseInfo : Intent

        data class UseModel(val modelRef: ActiveModelRef) : Intent
        data class SetPinState(
            val modelId: String,
            val modelType: ModelType,
            val pinned: Boolean
        ) : Intent

        data object DismissError : Intent
        data object DismissMessage : Intent
    }

    sealed interface Label {

        data object NavigatedBack : Label
    }
}
