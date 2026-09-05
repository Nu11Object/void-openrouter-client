package com.nullo.voidapp.core.utils.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Draws a fading (bottom-to-top) gradient mask over the content, creating an effect
 * where content visually fades out to transparent near the bottom edge of the element.
 *
 * Typical usage:
 * ```
 * LazyColumn(
 *     modifier = Modifier.bottomFade(heightFraction = 0.2f)
 * )
 * ```
 *
 * @param heightFraction Fraction of the element's height (from 0f to 1f) that the
 * gradient occupies, measured from the bottom edge upward.
 * @param startAlpha Alpha value at the point where the gradient begins.
 * @param endAlpha Alpha value at the very edge of the element.
 */
fun Modifier.bottomFade(
    heightFraction: Float = 0.3f,
    startAlpha: Float = 1f,
    endAlpha: Float = 0f,
): Modifier {
    val fraction = heightFraction.coerceIn(0f, 1f)
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val fadeHeight = size.height * fraction
            val fadeStartY = size.height - fadeHeight

            val brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.Black.copy(alpha = startAlpha),
                    1f to Color.Black.copy(alpha = endAlpha),
                ),
                startY = fadeStartY,
                endY = size.height,
            )

            onDrawWithContent {
                drawContent()
                drawRect(
                    brush = brush,
                    topLeft = Offset(0f, fadeStartY),
                    size = Size(size.width, fadeHeight),
                    blendMode = BlendMode.DstIn,
                )
            }
        }
}
