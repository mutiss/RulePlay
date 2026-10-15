package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing

/**
 * Top bar that sits over content. [solidness] (0..1) drives the background and the title, so headers
 * can start transparent over artwork and become solid as the user scrolls.
 */
@Composable
fun RulePlayTopBar(
    title: String,
    modifier: Modifier = Modifier,
    solidness: () -> Float = { 1f },
    onBack: (() -> Unit)? = null,
    overArt: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer { }
            .background(Color.Transparent),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = solidness() }
                .background(colors.background),
        )
        Row(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                RoundIconButton(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    onArt = overArt,
                )
            } else {
                Spacer(Modifier.width(Spacing.m))
            }
            Text(
                title,
                style = type.title,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.m)
                    .graphicsLayer { alpha = solidness() },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically, content = actions)
        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(1.dp)
                .graphicsLayer { alpha = solidness() }
                .background(colors.outline),
        )
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

/**
 * Floating pill navigation. The selected destination expands to show its label on a lime capsule;
 * the others stay icon-only, keeping the bar compact and one-thumb friendly.
 */
@Composable
fun RulePlayBottomBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RulePlayTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .shadow(24.dp, Shapes.pill, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(Shapes.pill)
                .background(if (colors.isDark) colors.surfaceRaised.copy(alpha = 0.97f) else colors.textPrimary.copy(alpha = 0.97f))
                .border(1.dp, if (colors.isDark) colors.outline else Color.Transparent, Shapes.pill)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                BottomBarItem(item, selected = index == selectedIndex, onClick = { onSelect(index) })
            }
        }
    }
}

@Composable
private fun BottomBarItem(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    val colors = RulePlayTheme.colors
    val motion = RulePlayTheme.motion
    val container by animateColorAsState(if (selected) colors.accent else Color.Transparent, motion.standard(), label = "navBg")
    val idleTint = if (colors.isDark) colors.textSecondary else colors.background.copy(alpha = 0.7f)
    val tint by animateColorAsState(if (selected) colors.onAccent else idleTint, motion.standard(), label = "navTint")
    val horizontal by animateDpAsState(if (selected) 18.dp else 14.dp, motion.standard(), label = "navPad")
    val iconScale by animateFloatAsState(if (selected) 1f else 0.94f, motion.bouncy(), label = "navScale")
    Row(
        Modifier
            .clip(Shapes.pill)
            .background(container)
            .semantics(mergeDescendants = true) {
                this.selected = selected
                contentDescription = item.label
            }
            .pressable(onClick = onClick, role = Role.Tab, pressedScale = 0.92f)
            .height(52.dp)
            .padding(horizontal = horizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (selected) item.selectedIcon else item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp).graphicsLayer { scaleX = iconScale; scaleY = iconScale },
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(motion.standard()) + fadeIn(motion.standard()),
            exit = shrinkHorizontally(motion.standard()) + fadeOut(motion.standard(120)),
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(item.label, style = RulePlayTheme.type.label, color = tint, maxLines = 1)
            }
        }
    }
}

/** Large editorial screen title used by top-level screens. */
@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier, overline: String? = null, subtitle: String? = null) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(modifier.fillMaxWidth().padding(horizontal = Spacing.gutter).semantics(mergeDescendants = true) {}) {
        if (overline != null) {
            Text(overline.uppercase(), style = type.overline, color = colors.accentText)
            Spacer(Modifier.height(Spacing.s))
        }
        Text(title.uppercase(), style = type.displayLarge, color = colors.textPrimary)
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.s))
            Text(subtitle, style = type.body, color = colors.textSecondary)
        }
    }
}

/** Segmented control with an animated thumb (used for sort orders and library tabs). */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RulePlayTheme.colors
    val motion = RulePlayTheme.motion
    Row(
        modifier
            .clip(Shapes.pill)
            .background(colors.surfaceSunken)
            .border(1.dp, colors.outline, Shapes.pill)
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(if (selected) colors.surfaceRaised else Color.Transparent, motion.standard(), label = "segBg")
            val fg by animateColorAsState(if (selected) colors.textPrimary else colors.textSecondary, motion.standard(), label = "segFg")
            Box(
                Modifier
                    .weight(1f)
                    .clip(Shapes.pill)
                    .background(bg)
                    .border(1.dp, if (selected) colors.outline else Color.Transparent, Shapes.pill)
                    .semantics { this.selected = selected }
                    .pressable(onClick = { onSelect(index) }, role = Role.Tab, pressedScale = 0.96f)
                    .height(44.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = RulePlayTheme.type.label, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Rounded surface container used for grouped content (settings groups, fact panels). */
@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = RulePlayTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
    ) { content() }
}
