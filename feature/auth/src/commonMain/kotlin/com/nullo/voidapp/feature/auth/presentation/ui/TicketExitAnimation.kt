package com.nullo.voidapp.feature.auth.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Duration of "pulling the ticket" - shared between the offset and glow growth. */
private const val PullDurationMs = 1500

/** The total upward offset of the ticket before it tears. */
private val PullLiftOffset = (-32).dp

/** The final offset of the top half after the tear. */
private val TopTearOffset = (-96).dp

/** The final offset of the bottom half - it is only slightly lifted by the tear. */
private val BottomLiftOffset = (-48).dp

/** The target rotation angle (in degrees) for the top half of the ticket after it tears. */
private const val TopRotationDegree = -8f

/**
 * Two phases:
 * 1. Pulling - the top and bottom of the ticket move together, while the glow grows steadily.
 * 2. Tear - the top half sharply flies further up, slightly pulling the bottom half along.
 */
@Stable
internal class TicketExitAnimationState(
    private val topOffsetAnim: Animatable<Dp, AnimationVector1D>,
    private val bottomOffsetAnim: Animatable<Dp, AnimationVector1D>,
    private val linearProgressAnim: Animatable<Float, AnimationVector1D>,
    private val topRotationAnim: Animatable<Float, AnimationVector1D>,
) {
    val topOffset: Dp
        get() = topOffsetAnim.value

    val bottomOffset: Dp
        get() = bottomOffsetAnim.value

    val linearProgress: Float
        get() = linearProgressAnim.value

    val topRotationDegrees: Float
        get() = topRotationAnim.value

    /** Runs through all phases, suspending until the ticket completely settles down. */
    suspend fun animateExit() = coroutineScope {
        launch {
            topOffsetAnim.animateTo(
                targetValue = PullLiftOffset,
                animationSpec = tween(
                    durationMillis = PullDurationMs,
                    easing = LinearEasing,
                )
            )
            launch {
                topOffsetAnim.animateTo(
                    targetValue = TopTearOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                )
            }
            topRotationAnim.animateTo(
                targetValue = TopRotationDegree,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
            )
        }
        launch {
            bottomOffsetAnim.animateTo(
                targetValue = PullLiftOffset,
                animationSpec = tween(
                    durationMillis = PullDurationMs,
                    easing = LinearEasing,
                )
            )
            bottomOffsetAnim.animateTo(
                targetValue = BottomLiftOffset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            )
        }
        launch {
            linearProgressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = PullDurationMs,
                    easing = EaseOut,
                )
            )
        }
    }
}

/**
 * Creates and remembers a [TicketExitAnimationState] to manage the ticket tearing animation.
 */
@Composable
internal fun rememberTicketExitAnimation(): TicketExitAnimationState {
    val topOffsetAnim = remember { Animatable(0.dp, Dp.VectorConverter) }
    val bottomOffsetAnim = remember { Animatable(0.dp, Dp.VectorConverter) }
    val linearProgressAnim = remember { Animatable(0f) }
    val topRotationAnim = remember { Animatable(0f) }
    return remember(topOffsetAnim, bottomOffsetAnim, linearProgressAnim, topRotationAnim) {
        TicketExitAnimationState(
            topOffsetAnim,
            bottomOffsetAnim,
            linearProgressAnim,
            topRotationAnim
        )
    }
}
