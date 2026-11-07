package com.nullo.voidapp.feature.chat.presentation.store

import androidx.compose.runtime.Immutable
import com.arkivanov.mvikotlin.core.store.Store
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.feature.chat.domain.tune.ChatTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.ImageTuneItem

interface TuneStore : Store<TuneStore.Intent, TuneStore.State, Nothing> {

    @Immutable
    data class State(
        val isPanelOpen: Boolean = false,
        val hintsVisible: Boolean = true,
        val modelName: String? = null,
        val content: Content = Content.NoModel,
    ) {

        sealed interface Content {

            data object NoModel : Content
            data object Unsupported : Content
            data class Chat(
                val items: List<ChatTuneItem>,
                val maxTokensLimit: Int?,
            ) : Content

            data class Image(val items: List<ImageTuneItem>) : Content
        }
    }

    sealed interface Intent {

        data object OpenPanel : Intent
        data object ClosePanel : Intent

        data class ChangeHintsVisibility(val visible: Boolean) : Intent
        data object ResetAll : Intent

        data class ChangeChatValue(
            val parameter: TuningParameter,
            val raw: String,
        ) : Intent

        data class ChangeChatSlider(
            val parameter: TuningParameter,
            val value: Double,
        ) : Intent

        data object SetMaxTokensToLimit : Intent
        data object RandomizeChatSeed : Intent

        data class SelectImageOption(
            val parameter: ImageParameter,
            val option: String,
        ) : Intent

        data class ChangeImageValue(
            val parameter: ImageParameter,
            val raw: String,
        ) : Intent

        data class ChangeImageSlider(
            val parameter: ImageParameter,
            val value: Double,
        ) : Intent

        data object RandomizeImageSeed : Intent
    }
}
