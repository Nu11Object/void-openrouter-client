package com.nullo.voidapp.feature.chat.presentation.ui.tune

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.automirrored.Restart
import com.nullo.voidapp.core.designsystem.icon.default.AspectRatio
import com.nullo.voidapp.core.designsystem.icon.default.Casino
import com.nullo.voidapp.core.designsystem.icon.default.Compress
import com.nullo.voidapp.core.designsystem.icon.default.Diamond
import com.nullo.voidapp.core.designsystem.icon.default.Explore
import com.nullo.voidapp.core.designsystem.icon.default.Filter
import com.nullo.voidapp.core.designsystem.icon.default.FormatListBulleted
import com.nullo.voidapp.core.designsystem.icon.default.HighQuality
import com.nullo.voidapp.core.designsystem.icon.default.Image
import com.nullo.voidapp.core.designsystem.icon.default.PhotoLibrary
import com.nullo.voidapp.core.designsystem.icon.default.PhotoSizeSelectLarge
import com.nullo.voidapp.core.designsystem.icon.default.Repeat
import com.nullo.voidapp.core.designsystem.icon.default.Text
import com.nullo.voidapp.core.designsystem.icon.default.Thermostat
import com.nullo.voidapp.core.designsystem.icon.default.Wallpaper
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.feature.chat.domain.tune.TuneInput
import com.nullo.voidapp.feature.chat.domain.tune.TuneSupport
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.template_tune_error_above_max
import voidapp.feature.chat.generated.resources.template_tune_error_below_min
import voidapp.feature.chat.generated.resources.tune_aspect_ratio_desc
import voidapp.feature.chat.generated.resources.tune_aspect_ratio_title
import voidapp.feature.chat.generated.resources.tune_background_desc
import voidapp.feature.chat.generated.resources.tune_background_title
import voidapp.feature.chat.generated.resources.tune_error_not_a_number
import voidapp.feature.chat.generated.resources.tune_error_not_an_integer
import voidapp.feature.chat.generated.resources.tune_frequency_penalty_desc
import voidapp.feature.chat.generated.resources.tune_frequency_penalty_title
import voidapp.feature.chat.generated.resources.tune_max_tokens_desc
import voidapp.feature.chat.generated.resources.tune_max_tokens_title
import voidapp.feature.chat.generated.resources.tune_n_desc
import voidapp.feature.chat.generated.resources.tune_n_title
import voidapp.feature.chat.generated.resources.tune_output_compression_desc
import voidapp.feature.chat.generated.resources.tune_output_compression_title
import voidapp.feature.chat.generated.resources.tune_output_format_desc
import voidapp.feature.chat.generated.resources.tune_output_format_title
import voidapp.feature.chat.generated.resources.tune_presence_penalty_desc
import voidapp.feature.chat.generated.resources.tune_presence_penalty_title
import voidapp.feature.chat.generated.resources.tune_quality_desc
import voidapp.feature.chat.generated.resources.tune_quality_title
import voidapp.feature.chat.generated.resources.tune_repetition_penalty_desc
import voidapp.feature.chat.generated.resources.tune_repetition_penalty_title
import voidapp.feature.chat.generated.resources.tune_resolution_desc
import voidapp.feature.chat.generated.resources.tune_resolution_title
import voidapp.feature.chat.generated.resources.tune_seed_desc
import voidapp.feature.chat.generated.resources.tune_seed_title
import voidapp.feature.chat.generated.resources.tune_size_desc
import voidapp.feature.chat.generated.resources.tune_size_title
import voidapp.feature.chat.generated.resources.tune_support_penalty_encourages_repeats
import voidapp.feature.chat.generated.resources.tune_support_penalty_mild
import voidapp.feature.chat.generated.resources.tune_support_penalty_neutral
import voidapp.feature.chat.generated.resources.tune_support_penalty_strong
import voidapp.feature.chat.generated.resources.tune_support_temperature_balanced
import voidapp.feature.chat.generated.resources.tune_support_temperature_precise
import voidapp.feature.chat.generated.resources.tune_support_temperature_wild
import voidapp.feature.chat.generated.resources.tune_support_top_k_diverse
import voidapp.feature.chat.generated.resources.tune_support_top_k_focused
import voidapp.feature.chat.generated.resources.tune_support_top_k_off
import voidapp.feature.chat.generated.resources.tune_support_top_p_balanced
import voidapp.feature.chat.generated.resources.tune_support_top_p_full
import voidapp.feature.chat.generated.resources.tune_support_top_p_narrow
import voidapp.feature.chat.generated.resources.tune_temperature_desc
import voidapp.feature.chat.generated.resources.tune_temperature_title
import voidapp.feature.chat.generated.resources.tune_top_k_desc
import voidapp.feature.chat.generated.resources.tune_top_k_title
import voidapp.feature.chat.generated.resources.tune_top_p_desc
import voidapp.feature.chat.generated.resources.tune_top_p_title

@Immutable
internal class TuneParamTexts(
    val icon: ImageVector,
    val title: StringResource,
    val description: StringResource,
)

internal fun TuningParameter.tuneTexts(): TuneParamTexts = when (this) {
    TuningParameter.TEMPERATURE -> TuneParamTexts(
        Icons.Default.Thermostat,
        Res.string.tune_temperature_title,
        Res.string.tune_temperature_desc
    )

    TuningParameter.TOP_P -> TuneParamTexts(
        Icons.Default.Filter,
        Res.string.tune_top_p_title,
        Res.string.tune_top_p_desc
    )

    TuningParameter.TOP_K -> TuneParamTexts(
        Icons.Default.FormatListBulleted,
        Res.string.tune_top_k_title,
        Res.string.tune_top_k_desc
    )

    TuningParameter.FREQUENCY_PENALTY -> TuneParamTexts(
        Icons.AutoMirrored.Restart,
        Res.string.tune_frequency_penalty_title,
        Res.string.tune_frequency_penalty_desc
    )

    TuningParameter.PRESENCE_PENALTY -> TuneParamTexts(
        Icons.Default.Explore,
        Res.string.tune_presence_penalty_title,
        Res.string.tune_presence_penalty_desc
    )

    TuningParameter.REPETITION_PENALTY -> TuneParamTexts(
        Icons.Default.Repeat,
        Res.string.tune_repetition_penalty_title,
        Res.string.tune_repetition_penalty_desc
    )

    TuningParameter.MAX_TOKENS -> TuneParamTexts(
        Icons.Default.Text,
        Res.string.tune_max_tokens_title,
        Res.string.tune_max_tokens_desc
    )

    TuningParameter.SEED -> TuneParamTexts(
        Icons.Default.Casino,
        Res.string.tune_seed_title,
        Res.string.tune_seed_desc
    )
}

internal fun ImageParameter.tuneTexts(): TuneParamTexts? = when (this) {
    ImageParameter.RESOLUTION -> TuneParamTexts(
        Icons.Default.HighQuality,
        Res.string.tune_resolution_title,
        Res.string.tune_resolution_desc
    )

    ImageParameter.ASPECT_RATIO -> TuneParamTexts(
        Icons.Default.AspectRatio,
        Res.string.tune_aspect_ratio_title,
        Res.string.tune_aspect_ratio_desc
    )

    ImageParameter.SIZE -> TuneParamTexts(
        Icons.Default.PhotoSizeSelectLarge,
        Res.string.tune_size_title,
        Res.string.tune_size_desc
    )

    ImageParameter.QUALITY -> TuneParamTexts(
        Icons.Default.Diamond,
        Res.string.tune_quality_title,
        Res.string.tune_quality_desc
    )

    ImageParameter.OUTPUT_FORMAT -> TuneParamTexts(
        Icons.Default.Image,
        Res.string.tune_output_format_title,
        Res.string.tune_output_format_desc
    )

    ImageParameter.BACKGROUND -> TuneParamTexts(
        Icons.Default.Wallpaper,
        Res.string.tune_background_title,
        Res.string.tune_background_desc
    )

    ImageParameter.OUTPUT_COMPRESSION -> TuneParamTexts(
        Icons.Default.Compress,
        Res.string.tune_output_compression_title,
        Res.string.tune_output_compression_desc
    )

    ImageParameter.N -> TuneParamTexts(
        Icons.Default.PhotoLibrary,
        Res.string.tune_n_title,
        Res.string.tune_n_desc
    )

    ImageParameter.SEED -> TuneParamTexts(
        Icons.Default.Casino,
        Res.string.tune_seed_title,
        Res.string.tune_seed_desc
    )

    ImageParameter.INPUT_REFERENCES -> null
}

internal fun TuneSupport.textRes(): StringResource = when (this) {
    TuneSupport.TEMPERATURE_PRECISE -> Res.string.tune_support_temperature_precise
    TuneSupport.TEMPERATURE_BALANCED -> Res.string.tune_support_temperature_balanced
    TuneSupport.TEMPERATURE_WILD -> Res.string.tune_support_temperature_wild
    TuneSupport.TOP_P_NARROW -> Res.string.tune_support_top_p_narrow
    TuneSupport.TOP_P_BALANCED -> Res.string.tune_support_top_p_balanced
    TuneSupport.TOP_P_FULL -> Res.string.tune_support_top_p_full
    TuneSupport.TOP_K_OFF -> Res.string.tune_support_top_k_off
    TuneSupport.TOP_K_FOCUSED -> Res.string.tune_support_top_k_focused
    TuneSupport.TOP_K_DIVERSE -> Res.string.tune_support_top_k_diverse
    TuneSupport.PENALTY_ENCOURAGES_REPEATS -> Res.string.tune_support_penalty_encourages_repeats
    TuneSupport.PENALTY_NEUTRAL -> Res.string.tune_support_penalty_neutral
    TuneSupport.PENALTY_MILD -> Res.string.tune_support_penalty_mild
    TuneSupport.PENALTY_STRONG -> Res.string.tune_support_penalty_strong
}

@Composable
internal fun TuneInput.Invalid.message(min: String, max: String): String = when (reason) {
    TuneInput.Reason.NOT_A_NUMBER -> stringResource(Res.string.tune_error_not_a_number)
    TuneInput.Reason.NOT_AN_INTEGER -> stringResource(Res.string.tune_error_not_an_integer)
    TuneInput.Reason.BELOW_MIN -> stringResource(Res.string.template_tune_error_below_min, min)
    TuneInput.Reason.ABOVE_MAX -> stringResource(Res.string.template_tune_error_above_max, max)
}
