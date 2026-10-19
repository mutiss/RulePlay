package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.ui.platform.LocalContext
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.art.ArtStyle
import com.mutissx.ruleplay.core.designsystem.art.SportIllustration
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import kotlinx.coroutines.delay

fun SportCategory?.accentColor(): Color = this?.let { Color(it.accent) } ?: Palette.Lime

enum class CardSize { TALL, WIDE, COMPACT }

/**
 * Editorial sport card with original artwork. Size variants let screens compose varied, magazine-like
 * layouts rather than uniform lists.
 */
@Composable
fun FeaturedSportCard(
    sport: SportSummary,
    category: SportCategory?,
    origin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.TALL,
    supporting: String = sport.tagline,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val accent = category.accentColor()
    val openLabel = stringResource(R.string.action_open_sport, sport.name)
    val ratio = when (size) {
        CardSize.TALL -> 0.74f
        CardSize.WIDE -> 1.6f
        CardSize.COMPACT -> 1f
    }
    Box(
        modifier
            .aspectRatio(ratio)
            .clip(Shapes.l)
            .background(colors.scrim)
            .pressable(onClick = onClick, onClickLabel = openLabel)
            .semantics(mergeDescendants = true) {
                contentDescription = "${sport.name}. ${category?.name.orEmpty()}. $supporting"
            },
    ) {
        SportIllustration(
            art = sport.art,
            style = ArtStyle.CARD,
            modifier = Modifier.fillMaxSize().sharedBoundsOrNone(SharedKeys.art(origin, sport.id), scaleContent = true),
            glyphScale = if (size == CardSize.WIDE) 0.95f else 1f,
        )
        ArtScrim()
        if (sport.hiddenGem && size != CardSize.COMPACT) {
            Tag(
                text = stringResource(R.string.label_hidden_gem),
                icon = Icons.Rounded.AutoAwesome,
                color = Palette.Ink,
                container = Palette.Lime,
                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.m),
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(if (size == CardSize.COMPACT) Spacing.m else Spacing.l),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(6.dp))
                Text(
                    (category?.name ?: "").uppercase(),
                    style = type.overline, color = colors.onArtSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(
                sport.name.uppercase(),
                style = if (size == CardSize.COMPACT) type.displaySmall else type.displayMedium,
                color = colors.onArt,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (size != CardSize.COMPACT) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    supporting,
                    style = type.caption,
                    color = colors.onArtSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Bottom-weighted darkening so text over artwork stays legible. */
@Composable
fun BoxScope.ArtScrim(strength: Float = 1f) {
    Box(
        Modifier
            .matchParentSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.38f to Color.Transparent,
                    1f to Palette.Ink.copy(alpha = 0.92f * strength),
                ),
            ),
    )
}

/** Small artwork tile used in rows of recently viewed and related sports. */
@Composable
fun SportThumb(
    sport: SportSummary,
    category: SportCategory?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(
        modifier
            .width(132.dp)
            .pressable(onClick = onClick, onClickLabel = stringResource(R.string.action_open_sport, sport.name))
            .semantics(mergeDescendants = true) {},
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(Shapes.m)) {
            SportIllustration(sport.art, Modifier.fillMaxSize(), style = ArtStyle.CARD, glyphScale = 1.15f)
        }
        Spacer(Modifier.height(Spacing.s))
        Text(sport.name, style = type.titleSmall, color = colors.textPrimary, maxLines = titleMaxLines(), overflow = TextOverflow.Ellipsis)
        Text(
            subtitle ?: category?.name.orEmpty(),
            style = type.caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Horizontal list row: artwork tile + text. Used in Explore, Library and search results. */
@Composable
fun SportRow(
    sport: SportSummary,
    category: SportCategory?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    supporting: String = sport.tagline,
    title: AnnotatedString = AnnotatedString(sport.name),
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(
        modifier
            .fillMaxWidth()
            .clip(Shapes.m)
            .pressable(onClick = onClick, onClickLabel = stringResource(R.string.action_open_sport, sport.name), pressedScale = 0.985f)
            .padding(vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(76.dp).clip(Shapes.s)) {
            SportIllustration(sport.art, Modifier.fillMaxSize(), style = ArtStyle.CARD, glyphScale = 1.2f)
        }
        Spacer(Modifier.width(Spacing.l))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(category.accentColor()))
                Spacer(Modifier.width(6.dp))
                Text(category?.name.orEmpty(), style = type.caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            Text(title, style = type.title, color = colors.textPrimary, maxLines = titleMaxLines(), overflow = TextOverflow.Ellipsis)
            Text(supporting, style = type.caption, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.s))
            trailing()
        }
    }
}

/** Selectable category pill; fills with the category's accent when selected. */
@Composable
fun SportCategoryChip(
    label: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val colors = RulePlayTheme.colors
    val motion = RulePlayTheme.motion
    val container by animateColorAsState(if (selected) accent else colors.surfaceRaised, motion.standard(), label = "chipBg")
    val content by animateColorAsState(if (selected) Palette.Ink else colors.textPrimary, motion.standard(), label = "chipFg")
    val dotScale by animateFloatAsState(if (selected) 0f else 1f, motion.standard(), label = "chipDot")
    Row(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(Shapes.pill)
            .background(container)
            .border(1.dp, if (selected) Color.Transparent else colors.outline, Shapes.pill)
            .semantics { this.selected = selected }
            .pressable(onClick = onClick, role = Role.Tab, pressedScale = 0.94f)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dotScale > 0.01f) {
            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp * dotScale))
        }
        Text(label, style = RulePlayTheme.type.label, color = content, maxLines = 1)
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Text(count.toString(), style = RulePlayTheme.type.caption, color = content.copy(alpha = 0.6f))
        }
    }
}

/**
 * Entry point to search. It is not an input itself: tapping it opens the dedicated search screen, and
 * the shared-bounds transition morphs it into the real field. The hint cycles once through example queries and
 * then settles, so the motion doesn't go on forever (WCAG 2.2.2); it stays still with reduced motion or a screen reader.
 */
@Composable
fun RuleSearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hints: List<String> = emptyList(),
    onArt: Boolean = false,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val motion = RulePlayTheme.motion
    var hintIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val screenReaderOn = remember(context) {
        context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled == true
    }
    if (hints.size > 1 && !motion.reduced && !screenReaderOn) {
        LaunchedEffect(hints) {
            repeat(hints.size) {
                delay(2800)
                hintIndex = (hintIndex + 1) % hints.size
            }
        }
    }
    val label = stringResource(R.string.search_open)
    Row(
        modifier
            .sharedBoundsOrNone(SharedKeys.SEARCH_BAR)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(Shapes.pill)
            .background(if (onArt) Color.White.copy(alpha = 0.12f) else colors.surfaceRaised)
            .border(1.dp, if (onArt) Color.White.copy(alpha = 0.2f) else colors.outline, Shapes.pill)
            .pressable(onClick = onClick, onClickLabel = label, pressedScale = 0.98f)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = if (onArt) colors.onArt else colors.accentText, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(Spacing.m))
        Text(stringResource(R.string.search_prefix), style = type.body, color = if (onArt) colors.onArtSecondary else colors.textSecondary)
        Spacer(Modifier.width(5.dp))
        AnimatedContent(
            targetState = hints.getOrNull(hintIndex) ?: stringResource(R.string.search_hint_default),
            transitionSpec = {
                (slideInVertically(motion.standard()) { it / 2 } + fadeIn(motion.standard())) togetherWith
                    (slideOutVertically(motion.standard()) { -it / 2 } + fadeOut(motion.standard()))
            },
            label = "hint",
        ) { hint ->
            Text(hint, style = type.bodyStrong, color = if (onArt) colors.onArt else colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

data class MetaItem(val icon: ImageVector, val label: String, val value: String)

/** Key facts about a sport in a responsive grid of tiles (wraps at large font sizes). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SportMetadataRow(items: List<MetaItem>, modifier: Modifier = Modifier, accent: Color = RulePlayTheme.colors.accentText) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
        maxItemsInEachRow = 2,
    ) {
        items.forEach { item ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(Shapes.m)
                    .background(colors.surface)
                    .border(1.dp, colors.outline, Shapes.m)
                    .padding(Spacing.l)
                    .semantics(mergeDescendants = true) {},
            ) {
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(Spacing.m))
                Text(item.label.uppercase(), style = type.overline, color = colors.textTertiary, modifier = Modifier.spokenAs(item.label))
                Spacer(Modifier.height(Spacing.xs))
                Text(item.value, style = type.titleSmall, color = colors.textPrimary)
            }
        }
    }
}
