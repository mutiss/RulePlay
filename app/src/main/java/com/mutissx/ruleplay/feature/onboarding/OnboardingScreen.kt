package com.mutissx.ruleplay.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.art.ArtStyle
import com.mutissx.ruleplay.core.designsystem.art.SportIllustration
import com.mutissx.ruleplay.core.designsystem.component.ButtonEmphasis
import com.mutissx.ruleplay.core.designsystem.component.RulePlayButton
import com.mutissx.ruleplay.core.designsystem.component.RulePlayWordmark
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.ArtGlyph
import com.mutissx.ruleplay.domain.model.ArtMotif
import com.mutissx.ruleplay.domain.model.ArtSpec
import com.mutissx.ruleplay.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import kotlin.math.absoluteValue

private data class Page(val title: Int, val body: Int, val art: ArtSpec)

// Three contrasting sports set the tone: a global giant, a water sport and a hidden gem.
private val pages = listOf(
    Page(R.string.onboarding_1_title, R.string.onboarding_1_body, ArtSpec(ArtMotif.COURT, ArtGlyph.BASKETBALL, 0xFF1A0A04, 0xFFB4501A, 0xFFFFB86B, 7)),
    Page(R.string.onboarding_2_title, R.string.onboarding_2_body, ArtSpec(ArtMotif.SHEET, ArtGlyph.CURLING_STONE, 0xFF06101C, 0xFF475569, 0xFFFCA5A5, 113)),
    Page(R.string.onboarding_3_title, R.string.onboarding_3_body, ArtSpec(ArtMotif.MAT, ArtGlyph.RAIDER, 0xFF1A0A02, 0xFFC2410C, 0xFFFDE047, 131)),
)

@Composable
fun OnboardingRoute(onFinished: () -> Unit) {
    val settings: SettingsRepository = koinInject()
    val scope = rememberCoroutineScope()
    OnboardingScreen(onFinished = {
        scope.launch {
            settings.setOnboardingCompleted(true)
            // Navigation must run on the main thread, whatever dispatcher resumed us after the write.
            withContext(Dispatchers.Main.immediate) { onFinished() }
        }
    })
}

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val motion = RulePlayTheme.motion
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            val page = pages[index]
            // Read inside layer blocks only, so swiping updates layers without recomposing the page.
            val offset = { pagerState.currentPage - index + pagerState.currentPageOffsetFraction }
            // Clip each page so its parallax artwork can never slide over the neighbouring page.
            Box(Modifier.fillMaxSize().clipToBounds()) {
                SportIllustration(
                    art = page.art,
                    style = ArtStyle.COVER,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Gentle parallax as pages slide.
                            if (!motion.reduced) {
                                // Zoom in as the page moves, slightly more than it shifts, so no empty edge is ever revealed.
                                val pageOffset = offset()
                                val zoom = 1f + 0.45f * pageOffset.absoluteValue.coerceAtMost(1f)
                                scaleX = zoom
                                scaleY = zoom
                                translationX = pageOffset * size.width * 0.2f
                            }
                        },
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0f to Palette.Ink.copy(alpha = 0.4f), 0.35f to Color.Transparent, 0.62f to Palette.Ink.copy(alpha = 0.75f), 1f to Palette.Ink),
                    ),
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter)
                        .padding(bottom = 180.dp)
                        .graphicsLayer { alpha = 1f - offset().absoluteValue.coerceIn(0f, 1f) },
                ) {
                    Text(
                        stringResource(page.title).uppercase(),
                        style = type.displayHero,
                        color = if (index == pages.lastIndex) Palette.Lime else colors.onArt,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(Spacing.l))
                    Text(stringResource(page.body), style = type.bodyLarge, color = colors.onArtSecondary)
                }
            }
        }

        Row(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .padding(start = Spacing.gutter, end = Spacing.s, top = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RulePlayWordmark(Modifier.weight(1f), onArt = true)
            if (!isLast) {
                RulePlayButton(stringResource(R.string.action_skip), onFinished, emphasis = ButtonEmphasis.ON_ART)
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Spacing.gutter, vertical = Spacing.xl),
        ) {
            val pageLabel = stringResource(R.string.onboarding_page, pagerState.currentPage + 1, pages.size)
            Row(
                Modifier.semantics(mergeDescendants = true) { contentDescription = pageLabel },
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                pages.indices.forEach { i ->
                    val selected = i == pagerState.currentPage
                    val width by animateDpAsState(if (selected) 28.dp else 8.dp, motion.standard(), label = "dot")
                    val color by animateColorAsState(if (selected) Palette.Lime else Color.White.copy(alpha = 0.3f), motion.standard(), label = "dotColor")
                    Box(Modifier.height(8.dp).width(width).clip(androidx.compose.foundation.shape.CircleShape).background(color))
                }
            }
            Spacer(Modifier.height(Spacing.xl))
            RulePlayButton(
                text = stringResource(if (isLast) R.string.action_start_exploring else R.string.action_next),
                onClick = {
                    if (isLast) onFinished()
                    else scope.launch {
                        if (motion.reduced) pagerState.scrollToPage(pagerState.currentPage + 1)
                        else pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
