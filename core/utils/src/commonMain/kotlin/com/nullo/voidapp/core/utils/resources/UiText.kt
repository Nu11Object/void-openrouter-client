package com.nullo.voidapp.core.utils.resources

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/** A sealed hierarchy representing user-facing text. */
sealed interface UiText {

    /** Represents raw text. */
    data class Static(val text: String) : UiText

    /** Represents a localized string resource with optional formatting arguments. */
    data class Resource(
        val stringResource: StringResource,
        val args: List<Any> = emptyList()
    ) : UiText

    data object Empty : UiText

    /** Resolves the text within a Composable UI context. */
    @Composable
    fun asString(): String = resolve { res ->
        stringResource(res.stringResource, *res.args.toTypedArray())
    }

    /** Asynchronously resolves the text in non-UI contexts. */
    suspend fun asStringSuspend(): String = resolve { res ->
        getString(res.stringResource, *res.args.toTypedArray())
    }

    companion object {

        /** Shorthand factory function to create a [UiText.Resource] using varargs. */
        operator fun invoke(
            stringResource: StringResource,
            vararg args: Any
        ): UiText = Resource(stringResource, args.toList())
    }
}

private inline fun UiText.resolve(resolveResource: (UiText.Resource) -> String): String =
    when (this) {
        is UiText.Static -> text
        is UiText.Empty -> ""
        is UiText.Resource -> resolveResource(this)
    }
