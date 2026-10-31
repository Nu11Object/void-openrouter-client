package com.nullo.voidapp.feature.catalog.domain

import androidx.compose.runtime.Immutable
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageModel

@Immutable
data class ImageModelFilters(
    val pinnedOnly: Boolean = false,
    val requiredInputModalities: Set<Modality> = emptySet(),
) {
    val isDefault: Boolean
        get() = this == Default

    val activeCount: Int
        get() = (if (pinnedOnly) 1 else 0) + requiredInputModalities.size

    companion object {
        val Default = ImageModelFilters()
    }
}

fun ImageModel.matches(filters: ImageModelFilters): Boolean =
    (!filters.pinnedOnly || pinned) &&
        inputModalities.containsAll(filters.requiredInputModalities)

fun List<ImageModel>.applyFilters(filters: ImageModelFilters): List<ImageModel> =
    if (filters.isDefault) this else filter { it.matches(filters) }
