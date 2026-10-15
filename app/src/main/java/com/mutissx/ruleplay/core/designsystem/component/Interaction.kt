package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme

/**
 * Clickable with refined press feedback: a subtle scale-down plus a ripple. Respects reduced motion.
 */
@Composable
fun Modifier.pressable(
    onClick: () -> Unit,
    onClickLabel: String? = null,
    role: Role = Role.Button,
    pressedScale: Float = 0.97f,
    enabled: Boolean = true,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val motion = RulePlayTheme.motion
    val scale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) pressedScale else 1f,
        animationSpec = motion.standard(150),
        label = "pressScale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = source,
            indication = ripple(),
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
}

/** Shared-transition scopes, provided by the navigation host so leaf components can opt in. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Keys used to link a sport's artwork between a card and its detail hero. */
object SharedKeys {
    fun art(origin: String, sportId: String) = "art/$origin/$sportId"
    fun title(origin: String, sportId: String) = "title/$origin/$sportId"
    const val SEARCH_BAR = "search-bar"
}

/**
 * Applies a shared-bounds transition when the app is hosted in a shared-transition layout and
 * reduced motion is off. Otherwise a no-op, so components stay usable in previews and tests.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsOrNone(key: String, scaleContent: Boolean = false): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val visibility = LocalNavAnimatedVisibilityScope.current ?: return this
    if (RulePlayTheme.motion.reduced) return this
    return with(shared) {
        this@sharedBoundsOrNone.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibility,
            // Artwork is laid out once at its final size and scaled during the morph, so its canvases are
            // recorded once instead of being re-measured and re-drawn on every frame of the transition.
            resizeMode = if (scaleContent) {
                SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Crop, Alignment.Center)
            } else {
                SharedTransitionScope.ResizeMode.RemeasureToBounds
            },
        )
    }
}
