package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.art.ArtStyle
import com.mutissx.ruleplay.core.designsystem.art.SportIllustration
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary

/**
 * Immersive, edge-to-edge sport hero. Always a dark "stage" (in both themes) so artwork and type keep
 * their drama; the bottom corners are rounded to sit on the page like a magazine cover.
 */
@Composable
fun SportHeroCard(
    sport: SportSummary,
    category: SportCategory?,
    overline: String,
    origin: String,
    onOpen: () -> Unit,
    saved: Boolean,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    parallax: () -> Float = { 0f },
    topContent: @Composable () -> Unit = {},
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val accent = category.accentColor()
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 560.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Palette.Ink),
    ) {
        SportIllustration(
            art = sport.art,
            style = ArtStyle.COVER,
            parallax = parallax,
            modifier = Modifier.matchParentSize().sharedBoundsOrNone(SharedKeys.art(origin, sport.id), scaleContent = true),
            glyphScale = 0.95f,
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Palette.Ink.copy(alpha = 0.55f),
                        0.22f to Color.Transparent,
                        0.5f to Color.Transparent,
                        1f to Palette.Ink.copy(alpha = 0.95f),
                    ),
                ),
        )
        Column(Modifier.fillMaxWidth()) {
            topContent()
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .padding(bottom = Spacing.xxl),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Palette.Lime))
                Spacer(Modifier.width(Spacing.s))
                Text(overline.uppercase(), style = type.overline, color = Palette.Lime, modifier = Modifier.spokenAs(overline))
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                sport.name.uppercase(),
                style = type.displayHero,
                color = colors.onArt,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(Spacing.s))
            Text(sport.tagline, style = type.bodyLarge, color = colors.onArtSecondary)
            Spacer(Modifier.height(Spacing.l))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Tag(category?.name.orEmpty(), dot = accent, color = colors.onArt, container = Color.White.copy(alpha = 0.12f))
                Tag(sport.playersLabel, color = colors.onArt, container = Color.White.copy(alpha = 0.12f))
            }
            Spacer(Modifier.height(Spacing.xl))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RulePlayButton(
                    text = stringResource(R.string.action_know_the_rules),
                    onClick = onOpen,
                    trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.m))
                SaveToggle(
                    saved = saved,
                    onToggle = onToggleSave,
                    contentDescription = stringResource(if (saved) R.string.action_unsave_sport else R.string.action_save_sport, sport.name),
                    onArt = true,
                    size = 52.dp,
                )
            }
        }
    }
}

/** Placeholder to keep hero height stable while content loads. */
@Composable
fun HeroPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(560.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Palette.Navy800),
    ) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Palette.Navy700, Palette.Ink))))
    }
}
