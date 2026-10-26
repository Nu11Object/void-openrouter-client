package com.nullo.voidapp.feature.catalog.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore

interface CatalogComponent {

    val model: Value<CatalogStore.State>

    fun onBackClicked()

    fun onSearchQueryChanged(query: String)

    fun onSearchQueryCleared()

    fun onRefreshCatalogClicked()

    fun onVerboseInfoToggled(enabled: Boolean)

    fun onUseModelClicked(modelRef: ActiveModelRef)

    fun onPinModelClicked(modelId: String, modelType: ModelType, pin: Boolean)

    fun onTabSelected(tab: CatalogStore.State.CatalogTab)

    fun onErrorDismissed()

    fun onMessageDismissed()

    fun interface Factory {

        fun create(
            onNavigateBack: () -> Unit,
            componentContext: ComponentContext,
        ): CatalogComponent
    }
}
