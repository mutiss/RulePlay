package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing

/**
 * Makes screen readers speak the natural-case [text] behind an upper-cased label: some voices spell short
 * all-caps words letter by letter ("NOTE" → "N, O, T, E").
 */
fun Modifier.spokenAs(text: String): Modifier = semantics { contentDescription = text }

/** Max lines for a title that is single-line by design: at large font sizes it wraps to two instead of truncating. */
@Composable
@ReadOnlyComposable
fun titleMaxLines(): Int = if (LocalDensity.current.fontScale > 1.3f) 2 else 1

/** The RulePlay mark: a rule bar beside a play triangle, in a lime tile. */
@Composable
fun RulePlayMark(modifier: Modifier = Modifier, size: Dp = 40.dp, tile: Color = Palette.Lime, glyph: Color = Palette.Ink) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        drawRoundRect(tile, cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.28f))
        drawRoundRect(glyph, topLeft = Offset(s * 0.24f, s * 0.24f), size = Size(s * 0.12f, s * 0.52f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.04f))
        val tri = Path().apply {
            moveTo(s * 0.45f, s * 0.24f)
            lineTo(s * 0.8f, s * 0.5f)
            lineTo(s * 0.45f, s * 0.76f)
            close()
        }
        drawPath(tri, glyph)
    }
}

/** Wordmark: RULE in the text colour, PLAY in the accent, set in the display face. */
@Composable
fun RulePlayWordmark(modifier: Modifier = Modifier, large: Boolean = false, showMark: Boolean = true, onArt: Boolean = false) {
    val type = RulePlayTheme.type
    val colors = RulePlayTheme.colors
    val ruleColor = if (onArt) colors.onArt else colors.textPrimary
    val playColor = if (onArt) Palette.Lime else colors.accentText
    Row(modifier.clearAndSetSemantics { contentDescription = "RulePlay" }, verticalAlignment = Alignment.CenterVertically) {
        if (showMark) {
            RulePlayMark(size = if (large) 52.dp else 30.dp)
            Spacer(Modifier.width(if (large) 14.dp else 10.dp))
        }
        val style = if (large) type.displayLarge else type.displaySmall
        Text("RULE", style = style, color = ruleColor)
        Text("PLAY", style = style, color = playColor)
    }
}

enum class ButtonEmphasis { PRIMARY, SECONDARY, GHOST, ON_ART }

/** Pill button. Primary is lime with ink text — reserved for the main action on a screen. */
@Composable
fun RulePlayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasis: ButtonEmphasis = ButtonEmphasis.PRIMARY,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = RulePlayTheme.colors
    val (container, content, border) = when (emphasis) {
        ButtonEmphasis.PRIMARY -> Triple(colors.accent, colors.onAccent, Color.Transparent)
        ButtonEmphasis.SECONDARY -> Triple(colors.surfaceRaised, colors.textPrimary, colors.outline)
        ButtonEmphasis.GHOST -> Triple(Color.Transparent, colors.textPrimary, colors.outlineStrong)
        ButtonEmphasis.ON_ART -> Triple(Color.White.copy(alpha = 0.14f), colors.onArt, Color.White.copy(alpha = 0.24f))
    }
    Row(
        modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(Shapes.pill)
            .background(container.copy(alpha = if (enabled) container.alpha else container.alpha * 0.4f))
            .border(1.dp, border, Shapes.pill)
            .pressable(onClick = onClick, enabled = enabled)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.s))
        }
        Text(text, style = RulePlayTheme.type.label, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (trailingIcon != null) {
            Spacer(Modifier.width(Spacing.s))
            Icon(trailingIcon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        }
    }
}

/** Round icon button with a 48dp touch target. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onArt: Boolean = false,
    tint: Color? = null,
) {
    val colors = RulePlayTheme.colors
    val container = if (onArt) Palette.Ink.copy(alpha = 0.42f) else colors.surfaceRaised
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, if (onArt) Color.White.copy(alpha = 0.16f) else colors.outline, CircleShape)
            .pressable(onClick = onClick, onClickLabel = contentDescription, pressedScale = 0.9f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint ?: if (onArt) colors.onArt else colors.textPrimary, modifier = Modifier.size(22.dp))
    }
}

/**
 * Save toggle with a small celebratory micro-interaction: the icon pops and a ring expands when an item
 * is saved.
 */
@Composable
fun SaveToggle(
    saved: Boolean,
    onToggle: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onArt: Boolean = false,
    style: SaveStyle = SaveStyle.HEART,
    size: Dp = 48.dp,
) {
    val colors = RulePlayTheme.colors
    val motion = RulePlayTheme.motion
    val pop = remember { Animatable(1f) }
    val ring = remember { Animatable(0f) }
    // Only celebrate transitions that happen while visible, not the initial state.
    val previous = remember { mutableStateOf(saved) }
    LaunchedEffect(saved) {
        val becameSaved = saved && !previous.value
        previous.value = saved
        if (becameSaved && !motion.reduced) {
            ring.snapTo(0f)
            pop.snapTo(0.6f)
            launch { pop.animateTo(1f, motion.bouncy()) }
            launch { ring.animateTo(1f, motion.standard(450)) }
        }
    }
    val tint by animateColorAsState(
        if (saved) colors.accentText.takeUnless { onArt } ?: Palette.Lime else if (onArt) colors.onArt else colors.textSecondary,
        motion.standard(), label = "saveTint",
    )
    val stateText = stringResource(if (saved) R.string.state_saved else R.string.state_not_saved)
    val container = if (onArt) Palette.Ink.copy(alpha = 0.42f) else colors.surfaceRaised
    Box(
        modifier
            .size(size)
            .semantics { stateDescription = stateText }
            .clip(CircleShape)
            .background(container)
            .border(1.dp, if (onArt) Color.White.copy(alpha = 0.16f) else colors.outline, CircleShape)
            .pressable(onClick = onToggle, role = Role.Checkbox, onClickLabel = contentDescription, pressedScale = 0.88f),
        contentAlignment = Alignment.Center,
    ) {
        val ringValue = ring.value
        if (ringValue in 0.01f..0.99f) {
            Canvas(Modifier.size(size)) {
                drawCircle(Palette.Lime.copy(alpha = 1f - ringValue), radius = this.size.minDimension / 2 * (0.5f + ringValue * 0.5f), style = Stroke(width = 3.dp.toPx() * (1f - ringValue)))
            }
        }
        val icon = when (style) {
            SaveStyle.HEART -> if (saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
            SaveStyle.BOOKMARK -> if (saved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder
        }
        Icon(
            icon, contentDescription = contentDescription, tint = tint,
            modifier = Modifier.size(size * 0.46f).graphicsLayer { scaleX = pop.value; scaleY = pop.value },
        )
    }
}

enum class SaveStyle { HEART, BOOKMARK }

/** Section title with an optional overline and trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(
        modifier.fillMaxWidth().padding(horizontal = Spacing.gutter),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) { heading() }) {
            if (overline != null) {
                Text(overline.uppercase(), style = type.overline, color = colors.accentText, modifier = Modifier.spokenAs(overline))
                Spacer(Modifier.height(Spacing.xs))
            }
            Text(title, style = type.headline, color = colors.textPrimary)
        }
        if (actionLabel != null && onAction != null) {
            Row(
                Modifier
                    .clip(Shapes.pill)
                    .pressable(onClick = onAction)
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(actionLabel, style = type.label, color = colors.textSecondary)
                Spacer(Modifier.width(Spacing.xs))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Small rounded label (e.g. category, "Law 11", "Key rule"). */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = RulePlayTheme.colors.textSecondary,
    container: Color = RulePlayTheme.colors.surfaceRaised,
    icon: ImageVector? = null,
    dot: Color? = null,
) {
    Row(
        modifier
            .clip(Shapes.pill)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(6.dp))
        }
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = RulePlayTheme.type.caption, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Thin progress track used by the reader. */
@Composable
fun ProgressTrack(progress: Float, modifier: Modifier = Modifier, color: Color = RulePlayTheme.colors.accent) {
    val motion = RulePlayTheme.motion
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), motion.standard(), label = "progress")
    Box(modifier.height(3.dp).clip(RoundedCornerShape(2.dp)).background(RulePlayTheme.colors.outline)) {
        Box(Modifier.fillMaxWidth(animated).height(3.dp).clip(RoundedCornerShape(2.dp)).background(color))
    }
}
