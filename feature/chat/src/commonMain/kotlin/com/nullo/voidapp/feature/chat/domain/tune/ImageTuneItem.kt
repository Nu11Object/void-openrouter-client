package com.nullo.voidapp.feature.chat.domain.tune

import androidx.compose.runtime.Immutable
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import kotlin.math.min
import kotlin.math.round

data class ImageNumberSpec(
    val min: Double,
    val max: Double,
    val isInteger: Boolean,
    val hasSlider: Boolean,
) {
    fun parse(raw: String): TuneInput = parseNumber(raw, min, max, isInteger)

    fun snap(value: Double): Double = round(value).coerceIn(min, max)

    fun format(value: Double): String = formatNumber(value, decimals = if (isInteger) 0 else 2)
}

@Immutable
sealed interface ImageTuneItem {

    val parameter: ImageParameter

    data class Choice(
        override val parameter: ImageParameter,
        val options: List<String>,
        val selected: String?,
    ) : ImageTuneItem

    /** [raw] null - untouched, "" - cleared by the user. */
    data class Numeric(
        override val parameter: ImageParameter,
        val spec: ImageNumberSpec,
        val raw: String?,
    ) : ImageTuneItem {
        val input: TuneInput = spec.parse(raw.orEmpty())
    }
}

private val IMAGE_TUNE_ORDER = listOf(
    ImageParameter.RESOLUTION,
    ImageParameter.ASPECT_RATIO,
    ImageParameter.SIZE,
    ImageParameter.QUALITY,
    ImageParameter.OUTPUT_FORMAT,
    ImageParameter.BACKGROUND,
    ImageParameter.OUTPUT_COMPRESSION,
    ImageParameter.N,
    ImageParameter.SEED
)

private val SLIDER_PARAMETERS = setOf(ImageParameter.N, ImageParameter.OUTPUT_COMPRESSION)
private val INTEGER_PARAMETERS = SLIDER_PARAMETERS + ImageParameter.SEED

internal fun ImageModel.toTuneItems(saved: Map<ImageParameter, ImageParameterValue>): List<ImageTuneItem> =
    IMAGE_TUNE_ORDER.mapNotNull { parameter ->
        val value = saved[parameter]
        when (val capability = capabilityOf(parameter)) {
            null -> null

            is ImageParameterCapability.Options -> ImageTuneItem.Choice(
                parameter = parameter,
                options = capability.values.toList(),
                selected = (value as? ImageParameterValue.Text)?.value
                    ?.takeIf { it in capability.values }
            )

            is ImageParameterCapability.Range -> {
                val spec = ImageNumberSpec(
                    min = capability.min,
                    max = if (parameter == ImageParameter.SEED) min(capability.max, MAX_SEED) else capability.max,
                    isInteger = parameter in INTEGER_PARAMETERS,
                    hasSlider = parameter in SLIDER_PARAMETERS
                )
                ImageTuneItem.Numeric(
                    parameter = parameter,
                    spec = spec,
                    raw = (value as? ImageParameterValue.Number)?.value?.let(spec::format)
                )
            }

            ImageParameterCapability.Flag -> null
        }
    }

internal fun List<ImageTuneItem>.toSessionMap(): Map<ImageParameter, ImageParameterValue> = buildMap {
    for (item in this@toSessionMap) {
        when (item) {
            is ImageTuneItem.Choice -> item.selected?.let {
                put(item.parameter, ImageParameterValue.Text(it))
            }

            is ImageTuneItem.Numeric -> (item.input as? TuneInput.Valid)?.let {
                put(item.parameter, ImageParameterValue.Number(it.value))
            }
        }
    }
}

internal fun ImageTuneItem.cleared(): ImageTuneItem = when (this) {
    is ImageTuneItem.Choice -> copy(selected = null)
    is ImageTuneItem.Numeric -> copy(raw = null)
}
