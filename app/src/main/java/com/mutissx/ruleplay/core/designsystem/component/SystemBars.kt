package com.mutissx.ruleplay.core.designsystem.component

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme

/**
 * Forces light status-bar icons while [overArt] is true (e.g. while a dark hero sits under the status bar
 * in the light theme), restoring the theme default otherwise.
 */
@Composable
fun StatusBarOverArt(overArt: Boolean) {
    val view = LocalView.current
    val themeLightIcons = !RulePlayTheme.colors.isDark
    if (view.isInEditMode) return
    DisposableEffect(overArt, themeLightIcons) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = if (overArt) false else themeLightIcons
        onDispose { controller?.isAppearanceLightStatusBars = themeLightIcons }
    }
}

/**
 * Text that shrinks (down to [minSize]) until its longest word fits on one line, so display type never
 * breaks a word in the middle — important for long category names and translations.
 */
@Composable
fun FitWordsText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minSize: TextUnit = 16.sp,
    maxLines: Int = 3,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val maxWidth = constraints.maxWidth
        val size = remember(text, style, maxWidth, density) {
            val words = text.split(' ').filter { it.isNotBlank() }
            var current = style.fontSize.value
            while (current > minSize.value) {
                val trial = style.copy(fontSize = current.sp, lineHeight = (current * 0.98f).sp)
                val widest = words.maxOfOrNull { measurer.measure(it, trial).size.width } ?: 0
                if (widest <= maxWidth) break
                current -= 1f
            }
            current
        }
        Text(
            text,
            style = style.copy(fontSize = size.sp, lineHeight = (size * 0.98f).sp),
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Solid band behind the status bar so scrolled content never collides with status icons. */
@Composable
fun StatusBarScrim(visible: Boolean = true) {
    if (!visible) return
    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(RulePlayTheme.colors.background),
    )
}
