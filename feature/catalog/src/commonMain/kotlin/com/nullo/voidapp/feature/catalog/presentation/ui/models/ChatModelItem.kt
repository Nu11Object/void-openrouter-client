package com.nullo.voidapp.feature.catalog.presentation.ui.models

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Lightbulb
import com.nullo.voidapp.core.designsystem.icon.default.Robot
import com.nullo.voidapp.core.designsystem.icon.default.Stylus
import com.nullo.voidapp.core.designsystem.icon.default.Vision
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.feature.catalog.presentation.ui.toCompactString
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.catalog.generated.resources.Res
import voidapp.feature.catalog.generated.resources.per_1m_tokens
import voidapp.feature.catalog.generated.resources.reasoning
import voidapp.feature.catalog.generated.resources.template_context_length
import voidapp.feature.catalog.generated.resources.vision

@Composable
internal fun ChatModelItem(
    chatModel: ChatModel,
    onUseModelClick: (ActiveModelRef) -> Unit,
    onPinModelClick: (modelId: String, ModelType, pin: Boolean) -> Unit,
    verbose: Boolean,
    modifier: Modifier = Modifier,
) = ModelCard(modifier = modifier) {
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Text(text = chatModel.name, style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedContent(
            targetState = verbose,
            label = "ContextInfo"
        ) { isVerbose ->
            if (isVerbose) {
                Text(
                    text = stringResource(Res.string.per_1m_tokens),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            } else {
                chatModel.contextLength?.let { context ->
                    Text(
                        text = stringResource(
                            Res.string.template_context_length,
                            context.toCompactString()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.Stylus,
            modifier = Modifier.size(16.dp),
            contentDescription = null
        )
        Text(
            text = chatModel.pricing.promptPerMillion.toPriceString(),
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor
        )
        Icon(
            imageVector = Icons.Default.Robot,
            modifier = Modifier.size(16.dp),
            contentDescription = null
        )
        Text(
            text = chatModel.pricing.completionPerMillion.toPriceString(),
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor
        )
    }
    Spacer(Modifier.height(4.dp))
    AnimatedVisibility(
        visible = verbose,
        label = "ChatModelVerboseSection"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chatModel.description.takeIf { it.isNotEmpty() }?.let { desc ->
                Text(
                    modifier = Modifier.padding(vertical = 6.dp),
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chatModel.contextLength?.let { context ->
                    OutlineTag(
                        text = stringResource(
                            Res.string.template_context_length,
                            context.toCompactString()
                        )
                    )
                }
                if (chatModel.supportsReasoning) {
                    OutlineTag(
                        icon = Icons.Default.Lightbulb,
                        text = stringResource(Res.string.reasoning)
                    )
                }
                if (chatModel.supportsVision) {
                    OutlineTag(
                        icon = Icons.Default.Vision,
                        text = stringResource(Res.string.vision)
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    ModelInteractionButtons(
        onUseModelClick = { onUseModelClick(ActiveModelRef.Chat(chatModel.id)) },
        onPinModelClick = { onPinModelClick(chatModel.id, ModelType.CHAT, !chatModel.pinned) },
        isModelPinned = chatModel.pinned,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun BigDecimal.toPriceString(): String {
    val plain = this.toPlainString()
    if (!plain.contains('.')) return "$$plain"

    val trimmed = plain.trimEnd('0').trimEnd('.')
    return "$" + trimmed.ifEmpty { "0" }
}
