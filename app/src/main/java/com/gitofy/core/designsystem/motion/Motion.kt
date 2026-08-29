package com.gitofy.core.designsystem.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.gitofy.core.designsystem.tokens.Dimensions
import com.gitofy.core.designsystem.tokens.MotionTokens

/** Single motion language for the whole app: smooth slide + fade, with a
 * restrained spring on components so the UI feels physical without feeling
 * sluggish. */
@Composable
fun Modifier.gitofyPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = Dimensions.cardPressedScale
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = MotionTokens.PressSpring,
        label = "gitofyPressScale"
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

private const val PAGE_SLIDE_FRACTION = 0.16f

private fun smoothPageEnter(offset: (Int) -> Int): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(360, easing = MotionTokens.EmphasizedEasing),
        initialOffsetX = offset
    ) + fadeIn(tween(320, easing = MotionTokens.EmphasizedEasing))

private fun smoothPageExit(offset: (Int) -> Int): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(300, easing = MotionTokens.EmphasizedEasing),
        targetOffsetX = offset
    ) + fadeOut(tween(260, easing = MotionTokens.EmphasizedEasing))

val AnimatedContentTransitionScope<*>.gitofyForwardEnter: EnterTransition
    get() = smoothPageEnter { (it * PAGE_SLIDE_FRACTION).toInt() }

val AnimatedContentTransitionScope<*>.gitofyForwardExit: ExitTransition
    get() = smoothPageExit { -(it * PAGE_SLIDE_FRACTION).toInt() }

val AnimatedContentTransitionScope<*>.gitofyBackEnter: EnterTransition
    get() = smoothPageEnter { -(it * PAGE_SLIDE_FRACTION).toInt() }

val AnimatedContentTransitionScope<*>.gitofyBackExit: ExitTransition
    get() = smoothPageExit { (it * PAGE_SLIDE_FRACTION).toInt() }

/** Component/text entrance: fade + vertical slide + a small spring overshoot. */
val gitofySlideFadeEnter: EnterTransition
    get() = slideInVertically(
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 520f
        ),
        initialOffsetY = { it / 10 }
    ) + fadeIn(tween(300, easing = MotionTokens.EmphasizedEasing))

val gitofySlideFadeExit: ExitTransition
    get() = slideOutVertically(
        animationSpec = tween(260, easing = MotionTokens.EmphasizedEasing),
        targetOffsetY = { -it / 14 }
    ) + fadeOut(tween(220, easing = MotionTokens.EmphasizedEasing))

val gitofyCardEnter: EnterTransition get() = gitofySlideFadeEnter
val gitofyCardExit: ExitTransition get() = gitofySlideFadeExit
val gitofyListInsertEnter: EnterTransition get() = gitofySlideFadeEnter
val gitofyListRemoveExit: ExitTransition get() = gitofySlideFadeExit
val gitofyContentEnter: EnterTransition get() = gitofySlideFadeEnter
val gitofyLoadingExit: ExitTransition get() = gitofySlideFadeExit
val gitofyTabEnter: EnterTransition get() = gitofySlideFadeEnter
val gitofyTabExit: ExitTransition get() = gitofySlideFadeExit
val gitofySuccessEnter: EnterTransition get() = gitofySlideFadeEnter
val gitofyErrorEnter: EnterTransition get() = gitofySlideFadeEnter
