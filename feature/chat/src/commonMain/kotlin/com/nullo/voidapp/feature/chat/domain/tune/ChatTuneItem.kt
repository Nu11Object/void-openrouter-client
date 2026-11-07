package com.nullo.voidapp.feature.chat.domain.tune

import androidx.compose.runtime.Immutable
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.TuningParameter

/** [raw] null - untouched (UI shows the default), "" - cleared by the user. */
@Immutable
data class ChatTuneItem(
    val spec: TuneParamSpec,
    val raw: String?,
) {
    val input: TuneInput = spec.parse(raw.orEmpty())

    val displayText: String
        get() = raw ?: spec.defaultValue?.let(spec::format).orEmpty()
}

internal fun ChatModel.toTuneItems(saved: Map<TuningParameter, Double>): List<ChatTuneItem> =
    tuneSpecs().map { spec ->
        ChatTuneItem(spec, raw = saved[spec.parameter]?.let(spec::format))
    }

internal fun List<ChatTuneItem>.toSessionMap(): Map<TuningParameter, Double> =
    mapNotNull { item ->
        (item.input as? TuneInput.Valid)?.let { item.spec.parameter to it.value }
    }.toMap()
