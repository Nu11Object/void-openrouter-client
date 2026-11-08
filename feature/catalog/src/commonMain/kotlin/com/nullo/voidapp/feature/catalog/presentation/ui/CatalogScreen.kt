package com.nullo.voidapp.feature.catalog.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.designsystem.component.BackTopAppBar
import com.nullo.voidapp.core.designsystem.component.VoidAlertDialog
import com.nullo.voidapp.core.designsystem.component.VoidDialogButton
import com.nullo.voidapp.core.designsystem.component.VoidSearchBar
import com.nullo.voidapp.core.designsystem.component.VoidSnackbarHost
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Filter
import com.nullo.voidapp.core.designsystem.theme.VoidTheme
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.rememberDeviceConfiguration
import com.nullo.voidapp.feature.catalog.presentation.component.CatalogComponent
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore.State.CatalogTab
import com.nullo.voidapp.feature.catalog.presentation.ui.filters.FilterActions
import com.nullo.voidapp.feature.catalog.presentation.ui.filters.FiltersSection
import com.nullo.voidapp.feature.catalog.presentation.ui.models.ModelActions
import com.nullo.voidapp.feature.catalog.presentation.ui.models.ModelsListSection
import org.jetbrains.compose.resources.stringResource
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.ok
import voidapp.core.utils.generated.resources.oops
import voidapp.core.utils.generated.resources.template_dismiss_with_timer
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.desc_open_filters
import voidapp.feature.catalog.generated.resources.model_catalog
import voidapp.feature.catalog.generated.resources.search_models

private val screenHorizontalPadding = 16.dp

@Composable
fun CatalogScreen(
    catalogComponent: CatalogComponent,
    modifier: Modifier = Modifier,
) {
    val model by catalogComponent.model.subscribeAsState()

    val filterActions = remember(catalogComponent) {
        FilterActions(
            onChatFiltersChange = catalogComponent::onChatFiltersChanged,
            onImageFiltersChange = catalogComponent::onImageFiltersChanged,
            onReset = catalogComponent::onFiltersReset
        )
    }
    val modelActions = remember(catalogComponent) {
        ModelActions(
            onUseModel = catalogComponent::onUseModelClicked,
            onPinModel = catalogComponent::onPinModelClicked,
            onImagePriceRequest = catalogComponent::onImagePriceRequested
        )
    }

    CatalogScreenContent(
        state = model,
        filterActions = filterActions,
        modelActions = modelActions,
        onBackClick = catalogComponent::onBackClicked,
        onSearchQueryChange = catalogComponent::onSearchQueryChanged,
        onClearSearchQuery = catalogComponent::onSearchQueryCleared,
        onRefreshCatalogClick = catalogComponent::onRefreshCatalogClicked,
        onToggleVerboseInfo = catalogComponent::onVerboseInfoToggled,
        onTabSelected = catalogComponent::onTabSelected,
        onDismissMessage = catalogComponent::onMessageDismissed,
        onDismissError = catalogComponent::onErrorDismissed,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogScreenContent(
    state: CatalogStore.State,
    filterActions: FilterActions,
    modelActions: ModelActions,
    onBackClick: () -> Unit,
    onSearchQueryChange: (query: String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onRefreshCatalogClick: () -> Unit,
    onToggleVerboseInfo: (Boolean) -> Unit,
    onTabSelected: (CatalogTab) -> Unit,
    onDismissMessage: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deviceConfiguration = rememberDeviceConfiguration()
    val isPortrait = remember(deviceConfiguration) {
        deviceConfiguration == DeviceConfiguration.MOBILE_PORTRAIT ||
            deviceConfiguration == DeviceConfiguration.TABLET_PORTRAIT
    }

    var showFilters by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isPortrait) {
        if (!isPortrait) showFilters = false
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            BackTopAppBar(
                title = stringResource(Res.string.model_catalog),
                onBackClick = onBackClick,
                actions = {
                    if (isPortrait) {
                        IconButton(
                            onClick = { showFilters = true },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (state.activeFiltersCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) { Text(text = state.activeFiltersCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Filter,
                                    contentDescription = stringResource(Res.string.desc_open_filters)
                                )
                            }
                        }
                    } else {
                        CatalogTabSwitcher(
                            selectedTab = state.selectedTab,
                            onTabSelected = onTabSelected,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            VoidSnackbarHost(
                message = state.message,
                action = {
                    Button(
                        onClick = onDismissMessage,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Text(
                            text = stringResource(
                                CoreRes.string.template_dismiss_with_timer,
                                state.messageDismissalTimer
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isPortrait) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CatalogHeader(
                        state = state,
                        onSearchQueryChange = onSearchQueryChange,
                        onClearSearchQuery = onClearSearchQuery,
                        onToggleVerboseInfo = onToggleVerboseInfo,
                        onRefreshCatalogClick = onRefreshCatalogClick
                    )

                    ModelsListSection(
                        state = state,
                        actions = modelActions
                    )
                }

                CatalogFloatingToolbar(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(3f)
                        .verticalScroll(rememberScrollState())
                ) {
                    CatalogHeader(
                        state = state,
                        onSearchQueryChange = onSearchQueryChange,
                        onClearSearchQuery = onClearSearchQuery,
                        onToggleVerboseInfo = onToggleVerboseInfo,
                        onRefreshCatalogClick = onRefreshCatalogClick
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 2.dp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )

                    FiltersSection(
                        selectedTab = state.selectedTab,
                        chatModelFilters = state.chatFilters,
                        imageModelFilters = state.imageFilters,
                        activeFiltersCount = state.activeFiltersCount,
                        actions = filterActions,
                        modifier = Modifier
                            .padding(horizontal = screenHorizontalPadding)
                            .padding(bottom = 16.dp)
                    )
                }

                ModelsListSection(
                    state = state,
                    actions = modelActions,
                    bottomFadeFraction = 0.1f,
                    modifier = Modifier.weight(5f)
                )
            }
        }
    }

    if (isPortrait && showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FiltersSection(
                selectedTab = state.selectedTab,
                chatModelFilters = state.chatFilters,
                imageModelFilters = state.imageFilters,
                activeFiltersCount = state.activeFiltersCount,
                actions = filterActions,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = screenHorizontalPadding)
                    .padding(bottom = 16.dp)
            )
        }
    }

    state.error?.let { error ->
        VoidAlertDialog(
            title = stringResource(CoreRes.string.oops),
            message = error.asString(),
            onDismissRequest = onDismissError,
            confirmButton = VoidDialogButton(
                text = stringResource(CoreRes.string.ok),
                onClick = onDismissError
            )
        )
    }
}

@Composable
private fun CatalogHeader(
    state: CatalogStore.State,
    onSearchQueryChange: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onToggleVerboseInfo: (Boolean) -> Unit,
    onRefreshCatalogClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VoidSearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            onClearQuery = onClearSearchQuery,
            placeholder = stringResource(Res.string.search_models),
            modifier = Modifier.padding(horizontal = screenHorizontalPadding)
        )
        ControlsSection(
            showVerboseInfo = state.showVerboseInfo,
            onToggleVerboseInfo = onToggleVerboseInfo,
            onRefreshCatalogClick = onRefreshCatalogClick,
            isLoading = state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenHorizontalPadding)
        )
    }
}

@PreviewLightDark
@Composable
private fun CatalogScreenPreview() {
    VoidTheme {
        CatalogScreenContent(
            state = CatalogStore.State(
                chatModels = buildList {
                    repeat(5) {
                        add(
                            ChatModel(
                                id = "id$it",
                                name = "Model Name",
                                description = "Super cool model! Try it right now or pin idk...",
                                createdAtTimestamp = 10000000,
                                contextLength = 256000,
                                maxCompletionTokens = 1000000,
                                inputModalities = setOf(Modality.TEXT, Modality.IMAGE),
                                outputModalities = setOf(Modality.TEXT),
                                supportedFeatures = setOf(ModelFeature.THINKING),
                                supportedTuningParameters = setOf(),
                                defaultTuningParameters = DefaultTuningParameters(),
                                pricing = ModelPricing(
                                    promptPerMillion = 3.toBigDecimal(),
                                    completionPerMillion = (15.5).toBigDecimal()
                                ),
                                pinned = it % 2 == 0
                            )
                        )
                    }
                }
            ),
            filterActions = FilterActions(),
            modelActions = ModelActions(),
            onBackClick = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onRefreshCatalogClick = {},
            onToggleVerboseInfo = {},
            onTabSelected = {},
            onDismissMessage = {},
            onDismissError = {}
        )
    }
}
