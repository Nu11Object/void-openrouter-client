package com.nullo.voidapp.core.utils.compose

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.isSpecified

/** Edge of the element on which the fade effect is applied. */
enum class FadeDirection {
    Top,
    Bottom,
    Start,
    End,
}

/**
 * Smoothstep easing: `3t² - 2t³`. Maps `0..1` to `0..1` with zero slope at both ends.
 */
val SmoothStepEasing: (Float) -> Float = { t -> t * t * (3f - 2f * t) }

private val LinearFade: (Float) -> Float = { it }

/**
 * Builds gradient color stops ordered from the edge inward: a hold zone fixed at [edge],
 * followed by a transition to [inner] shaped by [easing].
 *
 * @param hold Fraction of the gradient, from the edge, kept at [edge].
 * @param easing Transition curve, must map `0..1` to `0..1`. Output is clamped.
 * @param steps Number of segments approximating [easing].
 */
private fun buildFadeStops(
    inner: Color,
    edge: Color,
    hold: Float,
    easing: (Float) -> Float,
    steps: Int = 16,
): Array<Pair<Float, Color>> = buildList {
    add(0f to edge)
    if (hold > 0f) add(hold to edge)
    for (i in 1..steps) {
        val t = i / steps.toFloat()
        val fraction = easing(t).coerceIn(0f, 1f)
        add((hold + (1f - hold) * t) to lerp(edge, inner, fraction))
    }
}.toTypedArray()

/**
 * Describes a single edge fade for [edgeFades]. See [edgeFade] for parameter details.
 */
@Immutable
class EdgeFadeSpec(
    val direction: FadeDirection,
    val sizeFraction: Float = 0.3f,
    val fadeSize: Dp = Dp.Unspecified,
    val holdFraction: Float = 0f,
    val easing: (Float) -> Float = LinearFade,
    val startAlpha: Float = 1f,
    val endAlpha: Float = 0f,
)

/**
 * Applies several edge fades within a single offscreen layer.
 * Prefer this over chaining [edgeFade] calls, which add one layer each.
 */
fun Modifier.edgeFades(vararg fades: EdgeFadeSpec): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val masks = fades.map { createFadeMask(it) }
        onDrawWithContent {
            drawContent()
            masks.forEach { mask ->
                if (mask.size.width > 0f && mask.size.height > 0f) {
                    drawRect(
                        brush = mask.brush,
                        topLeft = mask.topLeft,
                        size = mask.size,
                        blendMode = BlendMode.DstIn
                    )
                }
            }
        }
    }

/**
 * Fades the content out to [endAlpha] near the chosen edge using a gradient mask.
 *
 * Renders the content into an offscreen layer; each call adds one layer.
 * To combine several fades in one layer, use [edgeFades].
 *
 * @param direction Edge on which the content fades out.
 * @param sizeFraction Size of the fade zone as a fraction (`0f..1f`) of the element's size
 * along the fade axis, measured from the edge inward. Ignored if [fadeSize] is specified.
 * @param fadeSize Absolute size of the fade zone. Overrides [sizeFraction] when specified
 * and is clamped to the element's size.
 * @param holdFraction Fraction (`0f..0.99f`) of the fade zone, starting at the edge,
 * that stays fully at [endAlpha] before the transition begins.
 * @param easing Shape of the transition after the hold zone. Must map `0..1` to `0..1`.
 * Defaults to linear.
 * @param startAlpha Alpha at the inner boundary of the fade zone.
 * @param endAlpha Alpha at the edge of the element.
 */
fun Modifier.edgeFade(
    direction: FadeDirection = FadeDirection.Bottom,
    sizeFraction: Float = 0.3f,
    fadeSize: Dp = Dp.Unspecified,
    holdFraction: Float = 0f,
    easing: (Float) -> Float = LinearFade,
    startAlpha: Float = 1f,
    endAlpha: Float = 0f,
): Modifier = edgeFades(
    EdgeFadeSpec(
        direction = direction,
        sizeFraction = sizeFraction,
        fadeSize = fadeSize,
        holdFraction = holdFraction,
        easing = easing,
        startAlpha = startAlpha,
        endAlpha = endAlpha
    )
)

private class FadeMask(
    val brush: Brush,
    val topLeft: Offset,
    val size: Size,
)

private fun CacheDrawScope.createFadeMask(spec: EdgeFadeSpec): FadeMask {
    val hold = spec.holdFraction.coerceIn(0f, 0.99f)
    val inner = Color.Black.copy(alpha = spec.startAlpha)
    val edge = Color.Black.copy(alpha = spec.endAlpha)

    val isVertical = spec.direction == FadeDirection.Top || spec.direction == FadeDirection.Bottom
    val axisSize = if (isVertical) size.height else size.width
    val fadeLength = if (spec.fadeSize.isSpecified) {
        spec.fadeSize.toPx().coerceIn(0f, axisSize)
    } else {
        axisSize * spec.sizeFraction.coerceIn(0f, 1f)
    }

    val edgeFirst = buildFadeStops(inner, edge, hold, spec.easing)
    val edgeLast = edgeFirst
        .map { (position, color) -> (1f - position) to color }
        .reversed()
        .toTypedArray()

    return when (spec.direction) {
        FadeDirection.Bottom -> {
            val startY = size.height - fadeLength
            FadeMask(
                brush = Brush.verticalGradient(
                    colorStops = edgeLast,
                    startY = startY,
                    endY = size.height
                ),
                topLeft = Offset(0f, startY),
                size = Size(size.width, fadeLength)
            )
        }

        FadeDirection.Top -> FadeMask(
            brush = Brush.verticalGradient(
                colorStops = edgeFirst,
                startY = 0f,
                endY = fadeLength
            ),
            topLeft = Offset.Zero,
            size = Size(size.width, fadeLength)
        )

        FadeDirection.Start, FadeDirection.End -> {
            val isLtr = layoutDirection == LayoutDirection.Ltr
            val atRight = (spec.direction == FadeDirection.End) == isLtr
            if (atRight) {
                val startX = size.width - fadeLength
                FadeMask(
                    brush = Brush.horizontalGradient(
                        colorStops = edgeLast,
                        startX = startX,
                        endX = size.width
                    ),
                    topLeft = Offset(startX, 0f),
                    size = Size(fadeLength, size.height)
                )
            } else {
                FadeMask(
                    brush = Brush.horizontalGradient(
                        colorStops = edgeFirst,
                        startX = 0f,
                        endX = fadeLength
                    ),
                    topLeft = Offset.Zero,
                    size = Size(fadeLength, size.height)
                )
            }
        }
    }
}

/**
 * Shortcut for [edgeFade] with [FadeDirection.Bottom] and a linear transition.
 *
 * @param heightFraction Height of the fade zone as a fraction (`0f..1f`) of the element's
 * height, measured from the bottom edge upward.
 * @param startAlpha Alpha at the inner boundary of the fade zone.
 * @param endAlpha Alpha at the bottom edge of the element.
 */
fun Modifier.bottomFade(
    heightFraction: Float = 0.3f,
    startAlpha: Float = 1f,
    endAlpha: Float = 0f,
): Modifier = edgeFade(
    direction = FadeDirection.Bottom,
    sizeFraction = heightFraction,
    startAlpha = startAlpha,
    endAlpha = endAlpha
)
