package com.mutissx.ruleplay.core.designsystem.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/** Spacing scale (4-pt grid). */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp

    /** Horizontal screen gutter. */
    val gutter = 20.dp
}

@Immutable
object Shapes {
    val xs = RoundedCornerShape(8.dp)
    val s = RoundedCornerShape(12.dp)
    val m = RoundedCornerShape(20.dp)
    val l = RoundedCornerShape(28.dp)
    val xl = RoundedCornerShape(36.dp)
    val pill = RoundedCornerShape(percent = 50)
}

/**
 * Motion tokens. When reduced motion is on, every spec collapses to an instant snap so content changes
 * without movement, while state changes still happen.
 */
@Immutable
data class RulePlayMotion(val reduced: Boolean) {
    fun <T> emphasized(durationMs: Int = 420): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(durationMs, easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0f, 0f, 1f))

    fun <T> standard(durationMs: Int = 260): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(durationMs, easing = androidx.compose.animation.core.FastOutSlowInEasing)

    fun <T> bouncy(): FiniteAnimationSpec<T> =
        if (reduced) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)

    fun <T> gentle(): AnimationSpec<T> =
        if (reduced) snap() else spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
}
