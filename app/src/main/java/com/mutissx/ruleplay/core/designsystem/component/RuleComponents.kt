package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.Rule

enum class HighlightKind { KEY, EXAMPLE, NOTE }

/** Call-out block with an accent rail: key rules, worked examples and editorial notes. */
@Composable
fun RuleHighlight(
    text: String,
    modifier: Modifier = Modifier,
    kind: HighlightKind = HighlightKind.EXAMPLE,
    title: String? = null,
    textScale: Float = 1f,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val (accent, icon, defaultTitle) = when (kind) {
        HighlightKind.KEY -> Triple(colors.accentText, Icons.Rounded.Bolt, stringResource(R.string.label_key_rule))
        HighlightKind.EXAMPLE -> Triple(colors.warning, Icons.Rounded.Lightbulb, stringResource(R.string.label_example))
        HighlightKind.NOTE -> Triple(colors.textSecondary, Icons.Rounded.Info, stringResource(R.string.label_note))
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(Shapes.s)
            .background(accent.copy(alpha = 0.08f))
            .semantics(mergeDescendants = true) {},
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(accent))
        Column(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                val kicker = title ?: defaultTitle
                Text(kicker.uppercase(), style = type.overline, color = accent, modifier = Modifier.spokenAs(kicker))
            }
            Spacer(Modifier.height(6.dp))
            Text(text, style = type.body.scaled(textScale), color = colors.textPrimary)
        }
    }
}

fun TextStyle.scaled(scale: Float): TextStyle =
    if (scale == 1f) this else copy(fontSize = fontSize * scale, lineHeight = if (lineHeight == TextUnit.Unspecified) lineHeight else lineHeight * scale)

/**
 * Expandable rule card: the collapsed state shows the plain-language summary; expanding reveals the
 * full explanation, a worked example and related rules.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuleSectionCard(
    rule: Rule,
    expanded: Boolean,
    onToggle: () -> Unit,
    saved: Boolean,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int? = null,
    relatedTitles: List<Pair<String, String>> = emptyList(),
    onRelatedClick: (String) -> Unit = {},
    highlight: AnnotatedString? = null,
    textScale: Float = 1f,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val motion = RulePlayTheme.motion
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, motion.standard(), label = "chevron")
    val stateText = stringResource(if (expanded) R.string.state_expanded else R.string.state_collapsed)
    val border = if (rule.important) colors.accentText.copy(alpha = 0.45f) else colors.outline
    Column(
        modifier
            .fillMaxWidth()
            .clip(Shapes.m)
            .background(colors.surface)
            .border(1.dp, border, Shapes.m)
            .animateContentSize(motion.standard()),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .pressable(onClick = onToggle, pressedScale = 0.99f)
                .semantics(mergeDescendants = true) { stateDescription = stateText }
                .padding(start = Spacing.l, top = Spacing.l, bottom = Spacing.l, end = Spacing.s),
            verticalAlignment = Alignment.Top,
        ) {
            if (index != null) {
                Text(
                    "%02d".format(index),
                    style = type.numeric,
                    color = if (rule.important) colors.accentText else colors.textTertiary,
                    modifier = Modifier.width(44.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (rule.important) {
                        Tag(stringResource(R.string.label_key_rule), icon = Icons.Rounded.Bolt, color = colors.onAccent, container = colors.accent)
                    }
                    rule.reference?.let { Tag(it) }
                }
                if (rule.important || rule.reference != null) Spacer(Modifier.height(Spacing.s))
                Text(rule.title, style = type.title.scaled(textScale), color = colors.textPrimary)
                Spacer(Modifier.height(Spacing.xs))
                Text(highlight ?: AnnotatedString(rule.summary), style = type.body.scaled(textScale), color = colors.textSecondary)
            }
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.ExpandMore, contentDescription = null, tint = colors.textSecondary,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(motion.standard()) + fadeIn(motion.standard()),
            exit = shrinkVertically(motion.standard()) + fadeOut(motion.standard(150)),
        ) {
            Column(Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.l)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outline))
                Spacer(Modifier.height(Spacing.l))
                Text(rule.body, style = type.bodyLarge.scaled(textScale), color = colors.textPrimary)
                rule.example?.let {
                    Spacer(Modifier.height(Spacing.l))
                    RuleHighlight(it, kind = HighlightKind.EXAMPLE, textScale = textScale)
                }
                if (relatedTitles.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.l))
                    val related = stringResource(R.string.label_related_rules)
                    Text(related.uppercase(), style = type.overline, color = colors.textTertiary, modifier = Modifier.spokenAs(related))
                    Spacer(Modifier.height(Spacing.s))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        relatedTitles.forEach { (id, title) ->
                            Box(
                                Modifier
                                    .clip(Shapes.pill)
                                    .border(1.dp, colors.outlineStrong, Shapes.pill)
                                    .pressable(onClick = { onRelatedClick(id) }, onClickLabel = title)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(title, style = type.caption, color = colors.textPrimary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.m))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditorialBadge(Modifier.weight(1f))
                    SaveToggle(
                        saved = saved,
                        onToggle = onToggleSave,
                        contentDescription = stringResource(if (saved) R.string.action_unsave_rule else R.string.action_save_rule),
                        style = SaveStyle.BOOKMARK,
                        size = 48.dp,
                    )
                }
            }
        }
    }
}

/** Marks text as an editorial summary rather than official rulebook wording. */
@Composable
fun EditorialBadge(modifier: Modifier = Modifier) {
    val colors = RulePlayTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Info, null, tint = colors.textTertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.label_editorial_summary), style = RulePlayTheme.type.caption, color = colors.textTertiary)
    }
}

/** Shows whether a rulebook link has been checked, and when. */
@Composable
fun SourceStatusBadge(checkedOn: String?, modifier: Modifier = Modifier) {
    val colors = RulePlayTheme.colors
    val verified = checkedOn != null
    val text = if (verified) stringResource(R.string.label_link_checked, formatIsoDate(checkedOn)) else stringResource(R.string.label_link_unverified)
    Tag(
        text = text,
        icon = if (verified) Icons.Rounded.Verified else Icons.Rounded.Link,
        color = if (verified) colors.accentText else colors.warning,
        container = (if (verified) colors.accentText else colors.warning).copy(alpha = 0.12f),
        modifier = modifier,
    )
}

/** Formats an ISO date (yyyy-MM-dd) in the user's locale. */
fun formatIsoDate(iso: String): String = runCatching {
    val date = java.time.LocalDate.parse(iso)
    date.format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM))
}.getOrDefault(iso)

/** A numbered step in a sequence (match flow). */
@Composable
fun FlowStepItem(number: Int, title: String, description: String, isLast: Boolean, accent: Color, modifier: Modifier = Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min).semantics(mergeDescendants = true) {}) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)).border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(number.toString(), style = type.label, color = accent)
            }
            if (!isLast) {
                Box(Modifier.width(2.dp).weight(1f).background(accent.copy(alpha = 0.25f)))
            }
        }
        Spacer(Modifier.width(Spacing.l))
        Column(Modifier.padding(bottom = if (isLast) 0.dp else Spacing.xl)) {
            Spacer(Modifier.height(5.dp))
            Text(title, style = type.titleSmall, color = colors.textPrimary)
            Spacer(Modifier.height(Spacing.xs))
            Text(description, style = type.body, color = colors.textSecondary)
        }
    }
}


/** Illustrated empty state with an optional action. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Lightbulb,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(
        modifier.fillMaxWidth().padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(132.dp).clip(CircleShape).background(colors.accentSoft.copy(alpha = 0.35f)))
            Box(Modifier.size(96.dp).clip(CircleShape).background(colors.accentSoft))
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(22.dp)).background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = colors.onAccent, modifier = Modifier.size(30.dp))
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        Text(title, style = type.headline, color = colors.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(Spacing.s))
        Text(message, style = type.body, color = colors.textSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xl))
            RulePlayButton(actionLabel, onAction, emphasis = ButtonEmphasis.SECONDARY)
        }
    }
}

/** Shimmer-free, calm loading placeholder. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val colors = RulePlayTheme.colors
    val label = stringResource(R.string.state_loading)
    Box(modifier.fillMaxWidth().padding(Spacing.xxxl).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator(color = colors.accentText, trackColor = colors.outline, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.error_title),
        message = message,
        icon = Icons.Rounded.Info,
        actionLabel = onRetry?.let { stringResource(R.string.action_retry) },
        onAction = onRetry,
        modifier = modifier,
    )
}
