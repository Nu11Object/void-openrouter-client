package com.nullo.voidapp.feature.chat.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullo.voidapp.core.designsystem.icon.Icons
import com.nullo.voidapp.core.designsystem.icon.default.Logo
import com.nullo.voidapp.core.utils.compose.SmoothStepEasing
import org.jetbrains.compose.resources.stringResource
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.empty_chat_placeholder

private const val CHAT_PLACEHOLDER_FADE_STEPS = 16

@Composable
internal fun EmptyChatPlaceholder(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 700)),
        exit = fadeOut(),
        modifier = modifier
            .imePadding()
            .semantics { hideFromAccessibility() },
        label = "EmptyChatPlaceholder"
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Logo,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.empty_chat_placeholder),
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
internal fun Modifier.chatPlaceholderGlow(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    heightFraction: Float = 0.95f,
    horizontalFadeFraction: Float = 0.25f,
    bottomOffset: Dp = 0.dp,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    ),
): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        progress.animateTo(if (visible) 1f else 0f, animationSpec)
    }

    val brush = remember(color) {
        Brush.verticalGradient(
            colorStops = Array(CHAT_PLACEHOLDER_FADE_STEPS + 1) { i ->
                val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
                t to color.copy(alpha = SmoothStepEasing(t))
            }
        )
    }

    val horizontalMask = remember(horizontalFadeFraction) {
        val f = horizontalFadeFraction.coerceIn(0f, 0.5f)
        val stops = buildList {
            for (i in 0..CHAT_PLACEHOLDER_FADE_STEPS) {
                val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
                add(t * f to Color.Black.copy(alpha = SmoothStepEasing(t)))
            }
            for (i in CHAT_PLACEHOLDER_FADE_STEPS downTo 0) {
                val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
                add((1f - t * f) to Color.Black.copy(alpha = SmoothStepEasing(t)))
            }
        }
        Brush.horizontalGradient(colorStops = stops.toTypedArray())
    }

    val bottomMask = remember {
        val stops = Array(CHAT_PLACEHOLDER_FADE_STEPS + 1) { i ->
            val t = i / CHAT_PLACEHOLDER_FADE_STEPS.toFloat()
            (0.85f + 0.15f * t) to Color.Black.copy(alpha = SmoothStepEasing(1f - t))
        }
        Brush.verticalGradient(colorStops = arrayOf(0f to Color.Black, *stops))
    }

    return drawWithCache {
        val areaSize = Size(
            width = size.width,
            height = (size.height - bottomOffset.toPx()).coerceAtLeast(0f)
        )
        val glowHeight = areaSize.height * heightFraction

        onDrawBehind {
            val p = progress.value
            if (p <= 0f || areaSize.height <= 0f) return@onDrawBehind

            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(Offset.Zero, areaSize), Paint())

                translate(top = areaSize.height - glowHeight * p) {
                    drawRect(
                        brush = brush,
                        size = Size(areaSize.width, glowHeight),
                        alpha = p
                    )
                }
                drawRect(
                    brush = horizontalMask,
                    size = areaSize,
                    blendMode = BlendMode.DstIn
                )
                drawRect(
                    brush = bottomMask,
                    size = areaSize,
                    blendMode = BlendMode.DstIn
                )

                canvas.restore()
            }
        }
    }
}
