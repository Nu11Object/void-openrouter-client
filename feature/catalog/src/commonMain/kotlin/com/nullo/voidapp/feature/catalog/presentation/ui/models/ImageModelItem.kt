package com.nullo.voidapp.feature.catalog.presentation.ui.models

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImagePricing
import com.nullo.voidapp.core.model.imagemodel.PricingLine
import com.nullo.voidapp.core.model.imagemodel.PricingUnit
import com.nullo.voidapp.feature.catalog.presentation.store.CatalogStore
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.checking_price
import voidapp.feature.catalog.generated.resources.price_load_failed
import voidapp.feature.catalog.generated.resources.price_not_available
import voidapp.feature.catalog.generated.resources.price_unit_image
import voidapp.feature.catalog.generated.resources.price_unit_megapixel
import voidapp.feature.catalog.generated.resources.price_unit_other
import voidapp.feature.catalog.generated.resources.price_unit_request
import voidapp.feature.catalog.generated.resources.price_unit_token
import voidapp.feature.catalog.generated.resources.template_price_line

@Composable
internal fun ImageModelItem(
    imageModel: ImageModel,
    verbose: Boolean,
    pricing: CatalogStore.State.ImagePricingState?,
    onCheckPriceClick: () -> Unit,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onPinModelClick: (modelId: String, ModelType, pin: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = ModelCard(modifier = modifier) {
    Text(text = imageModel.name)
    AnimatedVisibility(
        visible = verbose && imageModel.description.isNotEmpty(),
        label = "ImageModelDescription"
    ) {
        Text(
            text = imageModel.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
    AnimatedContent(
        targetState = pricing,
        label = "ImageModelPricing"
    ) { pricingState ->
        ImagePricingSection(pricingState)
    }
    Spacer(Modifier.height(8.dp))
    ModelInteractionButtons(
        onUseModelClick = { onUseModelClick(ActiveModelRef.Image(imageModel.id)) },
        onPinModelClick = { onPinModelClick(imageModel.id, ModelType.IMAGE, !imageModel.pinned) },
        isModelPinned = imageModel.pinned,
        showCheckPriceButton = pricing == null || pricing is CatalogStore.State.ImagePricingState.Failed,
        onCheckPriceClick = onCheckPriceClick,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ImagePricingSection(
    pricing: CatalogStore.State.ImagePricingState?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.animateContentSize()) {
        when (pricing) {
            is CatalogStore.State.ImagePricingState.Loaded -> ImagePricingDetails(pricing.providers)

            is CatalogStore.State.ImagePricingState.Failed -> Text(
                text = stringResource(Res.string.price_load_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )

            is CatalogStore.State.ImagePricingState.Loading -> {
                Text(
                    text = stringResource(Res.string.checking_price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun ImagePricingDetails(
    providers: List<ImagePricing>,
    modifier: Modifier = Modifier,
) {
    if (providers.isEmpty()) {
        Text(
            text = stringResource(Res.string.price_not_available),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        providers.forEach { provider ->
            Column {
                Text(
                    text = provider.providerName,
                    style = MaterialTheme.typography.labelLarge
                )
                provider.lines.forEach { line ->
                    Text(
                        text = line.formatted(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PricingLine.formatted(): String {
    val label = billable.replace('_', ' ').replaceFirstChar { it.uppercase() } +
        variant?.let { " ($it)" }.orEmpty()
    return stringResource(
        Res.string.template_price_line,
        label,
        costUsd.toStringExpanded(),
        stringResource(unit.labelRes())
    )
}

private fun PricingUnit.labelRes() = when (this) {
    PricingUnit.REQUEST -> Res.string.price_unit_request
    PricingUnit.IMAGE -> Res.string.price_unit_image
    PricingUnit.MEGAPIXEL -> Res.string.price_unit_megapixel
    PricingUnit.TOKEN -> Res.string.price_unit_token
    PricingUnit.OTHER -> Res.string.price_unit_other
}
