package com.mutissx.ruleplay.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.RulePlayMark
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Branded launch moment that continues the system splash: the mark settles, the wordmark slides out of
 * it, field lines sweep across, then the overlay lifts away. Skipped instantly with reduced motion.
 */
@Composable
fun IntroOverlay(onFinished: () -> Unit) {
    val motion = RulePlayTheme.motion
    val type = RulePlayTheme.type
    val words = remember { Animatable(0f) }
    val lines = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }
    val ease = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    LaunchedEffect(Unit) {
        if (motion.reduced) {
            onFinished()
            return@LaunchedEffect
        }
        launch { lines.animateTo(1f, tween(900, easing = ease)) }
        words.animateTo(1f, tween(520, delayMillis = 120, easing = ease))
        delay(380)
        exit.animateTo(1f, tween(420, easing = ease))
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - exit.value
                scaleX = 1f + exit.value * 0.06f
                scaleY = 1f + exit.value * 0.06f
            }
            .background(Palette.Midnight),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Halfway line and centre circle drawing themselves in.
            val p = lines.value
            val c = Offset(size.width / 2, size.height / 2)
            drawLine(Palette.Lime.copy(alpha = 0.18f), Offset(0f, c.y), Offset(size.width * p, c.y), 2.dp.toPx())
            drawArc(
                Palette.Lime.copy(alpha = 0.14f), -90f, 360f * p, false,
                topLeft = Offset(c.x - size.width * 0.36f, c.y - size.width * 0.36f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.width * 0.72f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RulePlayMark(size = 72.dp)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.clipToBounds()) {
                Text(
                    "RULE", style = type.displayLarge, color = Palette.OffWhite,
                    modifier = Modifier.graphicsLayer { translationY = (1f - words.value) * 60.dp.toPx(); alpha = words.value },
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    "PLAY", style = type.displayLarge, color = Palette.Lime,
                    modifier = Modifier.graphicsLayer { translationY = -(1f - words.value) * 60.dp.toPx(); alpha = words.value },
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.brand_tagline).uppercase(),
                style = type.overline, color = Palette.Mist,
                modifier = Modifier.graphicsLayer { alpha = words.value },
            )
        }
    }
}
