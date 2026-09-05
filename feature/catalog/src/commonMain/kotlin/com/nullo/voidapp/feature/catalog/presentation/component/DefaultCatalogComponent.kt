package com.nullo.voidapp.feature.catalog.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.utils.decompose.componentScope
import com.nullo.voidapp.core.utils.mvikotlin.asValue
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.Intent
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStoreFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DefaultCatalogComponent(
    private val onNavigateBack: () -> Unit,
    private val catalogStoreFactory: CatalogStoreFactory,
    private val componentContext: ComponentContext,
) : CatalogComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val store = instanceKeeper.getStore { catalogStoreFactory.create() }

    init {
        observeLabels()
    }

    override val model: Value<CatalogStore.State> = store.asValue()

    override fun onBackClicked() {
        store.accept(Intent.NavigateBack)
    }

    override fun onSearchQueryChanged(query: String) {
        store.accept(Intent.ChangeSearchQuery(query))
    }

    override fun onSearchQueryCleared() {
        store.accept(Intent.ClearSearchQuery)
    }

    override fun onRefreshCatalogClicked() {
        store.accept(Intent.RefreshCatalog)
    }

    override fun onVerboseInfoToggled(enabled: Boolean) {
        store.accept(Intent.ToggleVerboseInfo(enabled))
    }

    override fun onUseModelClicked(modelRef: ActiveModelRef) {
        store.accept(Intent.UseModel(modelRef))
    }

    override fun onPinModelClicked(
        modelId: String,
        modelType: ModelType,
        pin: Boolean
    ) {
        store.accept(Intent.SetPinState(modelId, modelType, pin))
    }

    override fun onTabSelected(tab: CatalogStore.State.CatalogTab) {
        store.accept(Intent.ChangeCatalogTab(tab))
    }

    override fun onErrorDismissed() {
        store.accept(Intent.DismissError)
    }

    override fun onMessageDismissed() {
        store.accept(Intent.DismissMessage)
    }

    private fun observeLabels() {
        store.labels
            .onEach { label ->
                when (label) {
                    CatalogStore.Label.NavigatedBack -> onNavigateBack()
                }
            }
            .launchIn(scope)
    }
}
