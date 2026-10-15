package com.mutissx.ruleplay.feature.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.art.ArtStyle
import com.mutissx.ruleplay.core.designsystem.art.SportIllustration
import com.mutissx.ruleplay.core.designsystem.component.ButtonEmphasis
import com.mutissx.ruleplay.core.designsystem.component.CardSize
import com.mutissx.ruleplay.core.designsystem.component.ErrorState
import com.mutissx.ruleplay.core.designsystem.component.FeaturedSportCard
import com.mutissx.ruleplay.core.designsystem.component.HeroPlaceholder
import com.mutissx.ruleplay.core.designsystem.component.RoundIconButton
import com.mutissx.ruleplay.core.designsystem.component.RulePlayButton
import com.mutissx.ruleplay.core.designsystem.component.RulePlayWordmark
import com.mutissx.ruleplay.core.designsystem.component.RuleSearchBar
import com.mutissx.ruleplay.core.designsystem.component.SectionHeader
import com.mutissx.ruleplay.core.designsystem.component.SportHeroCard
import com.mutissx.ruleplay.core.designsystem.component.SportThumb
import com.mutissx.ruleplay.core.designsystem.component.StatusBarOverArt
import com.mutissx.ruleplay.core.designsystem.component.StatusBarScrim
import com.mutissx.ruleplay.core.designsystem.component.FitWordsText
import com.mutissx.ruleplay.core.designsystem.component.pressable
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.navigation.AppNavigator
import org.koin.androidx.compose.koinViewModel

/** Space reserved at the bottom of top-level screens for the floating navigation bar. */
val BottomBarSpace: Dp = 128.dp

private object Origin {
    const val HERO = "hero"
    const val POPULAR = "popular"
    const val GEMS = "gems"
    const val SURPRISE = "surprise"
}

@Composable
fun DiscoverRoute(navigator: AppNavigator, viewModel: DiscoverViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiscoverScreen(
        state = state,
        onOpenSport = navigator::sport,
        onOpenCategory = navigator::category,
        onSearch = navigator::search,
        onOpenLibrary = navigator::library,
        onExploreAll = navigator::explore,
        onSurprise = viewModel::onSurpriseMe,
        onToggleFavorite = viewModel::onToggleFavorite,
    )
}

@Composable
fun DiscoverScreen(
    state: DiscoverUiState,
    onOpenSport: (String, String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenLibrary: () -> Unit,
    onExploreAll: () -> Unit,
    onSurprise: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    when (state) {
        DiscoverUiState.Loading -> Column(Modifier.fillMaxSize().background(RulePlayTheme.colors.background)) { HeroPlaceholder() }
        is DiscoverUiState.Error -> ErrorState(state.message, onRetry = null, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
        is DiscoverUiState.Content -> Box(Modifier.fillMaxSize()) {
            DiscoverContent(
                state, listState, onOpenSport, onOpenCategory, onSearch, onOpenLibrary, onExploreAll, onSurprise, onToggleFavorite,
            )
            val pastHero by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            StatusBarScrim(visible = pastHero)
        }
    }
}

@Composable
private fun DiscoverContent(
    state: DiscoverUiState.Content,
    listState: LazyListState,
    onOpenSport: (String, String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenLibrary: () -> Unit,
    onExploreAll: () -> Unit,
    onSurprise: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val density = LocalDensity.current
    val hints = stringArrayResource(R.array.search_hints).toList()
    val heroHeightPx = with(density) { 560.dp.toPx() }
    val heroUnderStatusBar by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    StatusBarOverArt(heroUnderStatusBar)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = BottomBarSpace),
    ) {
        item(key = "hero") {
            val today = state.sportOfTheDay
            SportHeroCard(
                sport = today,
                category = state.categoriesById[today.categoryId],
                overline = stringResource(R.string.discover_sport_of_the_day),
                origin = Origin.HERO,
                onOpen = { onOpenSport(today.id, Origin.HERO) },
                saved = today.id in state.favoriteSportIds,
                onToggleSave = { onToggleFavorite(today.id) },
                parallax = {
                    if (listState.firstVisibleItemIndex > 0) 1f
                    else (listState.firstVisibleItemScrollOffset / heroHeightPx).coerceIn(0f, 1f)
                },
                topContent = {
                    Row(
                        Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .fillMaxWidth()
                            .padding(start = Spacing.gutter, end = Spacing.m, top = Spacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RulePlayWordmark(Modifier.weight(1f), onArt = true)
                        RoundIconButton(Icons.Rounded.Search, stringResource(R.string.search_open), onSearch, onArt = true)
                    }
                },
            )
        }

        item(key = "headline") {
            Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xxl)) {
                Text(
                    stringResource(R.string.discover_headline_1).uppercase(),
                    style = type.displayLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
                Text(stringResource(R.string.discover_headline_2).uppercase(), style = type.displayLarge, color = colors.accentText)
                Spacer(Modifier.height(Spacing.m))
                Text(
                    pluralStringResource(R.plurals.discover_catalog_summary, state.totalSports, state.totalSports, state.categories.size),
                    style = type.body, color = colors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.xl))
                RuleSearchBar(onClick = onSearch, hints = hints)
            }
        }

        item(key = "popular") {
            Spacer(Modifier.height(Spacing.xxxl))
            SectionHeader(
                title = stringResource(R.string.discover_popular),
                overline = stringResource(R.string.discover_popular_overline),
                actionLabel = stringResource(R.string.action_see_all),
                onAction = onExploreAll,
            )
            Spacer(Modifier.height(Spacing.l))
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                items(state.popular, key = { it.id }) { sport ->
                    FeaturedSportCard(
                        sport = sport,
                        category = state.categoriesById[sport.categoryId],
                        origin = Origin.POPULAR,
                        onClick = { onOpenSport(sport.id, Origin.POPULAR) },
                        modifier = Modifier.width(216.dp),
                    )
                }
            }
        }

        item(key = "surprise") {
            Spacer(Modifier.height(Spacing.xxxl))
            SurprisePanel(
                pick = state.surprise,
                category = state.surprise?.let { state.categoriesById[it.categoryId] },
                onSurprise = onSurprise,
                onOpen = { onOpenSport(it, Origin.SURPRISE) },
            )
        }

        item(key = "categories-header") {
            Spacer(Modifier.height(Spacing.xxxl))
            SectionHeader(
                title = stringResource(R.string.discover_categories),
                overline = stringResource(R.string.discover_categories_overline),
            )
            Spacer(Modifier.height(Spacing.l))
        }
        val rows = mosaicRows(state.categories)
        items(rows.size, key = { "cat-row-$it" }) { index ->
            val row = rows[index]
            Row(
                Modifier.padding(horizontal = Spacing.gutter).padding(bottom = Spacing.m),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                row.tiles.forEachIndexed { i, tile ->
                    CategoryMosaicTile(
                        tile = tile,
                        height = row.height,
                        onClick = { onOpenCategory(tile.category.id) },
                        modifier = Modifier.weight(row.weights[i]),
                    )
                }
            }
        }

        if (state.hiddenGems.isNotEmpty()) {
            item(key = "gems") {
                Spacer(Modifier.height(Spacing.xxl))
                SectionHeader(
                    title = stringResource(R.string.discover_hidden_gems),
                    overline = stringResource(R.string.discover_hidden_gems_overline),
                )
                Spacer(Modifier.height(Spacing.l))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                ) {
                    items(state.hiddenGems, key = { it.id }) { sport ->
                        FeaturedSportCard(
                            sport = sport,
                            category = state.categoriesById[sport.categoryId],
                            origin = Origin.GEMS,
                            onClick = { onOpenSport(sport.id, Origin.GEMS) },
                            size = CardSize.WIDE,
                            supporting = sport.hook,
                            modifier = Modifier.width(312.dp),
                        )
                    }
                }
            }
        }

        if (state.becauseOf != null && state.recommendations.isNotEmpty()) {
            item(key = "because") {
                Spacer(Modifier.height(Spacing.xxxl))
                SectionHeader(
                    title = stringResource(R.string.discover_because_you_read, state.becauseOf.name),
                    overline = stringResource(R.string.discover_because_overline),
                )
                Spacer(Modifier.height(Spacing.l))
                ThumbRow(state.recommendations, state.categoriesById, onOpenSport, useHook = true)
            }
        }

        if (state.recent.isNotEmpty()) {
            item(key = "recent") {
                Spacer(Modifier.height(Spacing.xxxl))
                SectionHeader(title = stringResource(R.string.discover_recent), overline = stringResource(R.string.discover_recent_overline))
                Spacer(Modifier.height(Spacing.l))
                ThumbRow(state.recent, state.categoriesById, onOpenSport, useHook = false)
            }
        }

        item(key = "library") {
            Spacer(Modifier.height(Spacing.xxxl))
            LibraryShortcut(state.savedCount, onOpenLibrary)
        }

        item(key = "footer") {
            Spacer(Modifier.height(Spacing.xxl))
            Row(Modifier.padding(horizontal = Spacing.gutter), verticalAlignment = Alignment.Top) {
                Icon(Icons.Rounded.Info, null, tint = colors.textTertiary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(Spacing.s))
                Text(
                    stringResource(R.string.discover_footer_note, state.contentVersion),
                    style = type.caption, color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun ThumbRow(
    sports: List<SportSummary>,
    categories: Map<String, SportCategory>,
    onOpenSport: (String, String) -> Unit,
    useHook: Boolean,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
    ) {
        items(sports, key = { it.id }) { sport ->
            SportThumb(
                sport = sport,
                category = categories[sport.categoryId],
                onClick = { onOpenSport(sport.id, "thumb") },
                subtitle = if (useHook) categories[sport.categoryId]?.name else null,
            )
        }
    }
}

private data class MosaicRow(val tiles: List<CategoryTile>, val weights: List<Float>, val height: Dp)

/** Groups category tiles into alternating asymmetric rows: wide+narrow, three equal, narrow+wide. */
private fun mosaicRows(tiles: List<CategoryTile>): List<MosaicRow> {
    val patterns = listOf(
        listOf(1.5f, 1f) to 168.dp,
        listOf(1f, 1f) to 148.dp,
        listOf(1f, 1.5f) to 168.dp,
    )
    val rows = mutableListOf<MosaicRow>()
    var index = 0
    var pattern = 0
    while (index < tiles.size) {
        val (weights, height) = patterns[pattern % patterns.size]
        val take = minOf(weights.size, tiles.size - index)
        rows += MosaicRow(tiles.subList(index, index + take), weights.take(take), height)
        index += take
        pattern++
    }
    return rows
}

@Composable
private fun CategoryMosaicTile(tile: CategoryTile, height: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val type = RulePlayTheme.type
    val accent = Color(tile.category.accent)
    val label = pluralStringResource(R.plurals.sports_count, tile.count, tile.count)
    Box(
        modifier
            .height(height)
            .clip(Shapes.l)
            .background(Brush.linearGradient(listOf(lerp(accent, Color.White, 0.08f), lerp(accent, Palette.Ink, 0.35f))))
            .pressable(onClick = onClick, onClickLabel = tile.category.name)
            .semantics(mergeDescendants = true) { contentDescription = "${tile.category.name}, $label" },
    ) {
        // The showcase sport's hero object peeks in from the bottom-right corner, cropped by the tile.
        tile.showcase?.let { sport ->
            SportIllustration(
                art = sport.art,
                style = ArtStyle.GLYPH,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = height * 0.28f, y = height * 0.3f)
                    .size(height * 1.05f),
                glyphScale = 1.1f,
            )
        }
        FitWordsText(
            text = tile.category.name.uppercase(),
            style = type.displaySmall,
            color = Palette.Ink,
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(start = Spacing.l, end = Spacing.m, top = Spacing.l),
        )
        Text(
            label, style = type.caption, color = Palette.Ink.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.l)
                .clip(Shapes.pill)
                .background(Color.White.copy(alpha = 0.35f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SurprisePanel(
    pick: SportSummary?,
    category: SportCategory?,
    onSurprise: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val motion = RulePlayTheme.motion
    Column(
        Modifier
            .padding(horizontal = Spacing.gutter)
            .fillMaxWidth()
            .clip(Shapes.xl)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.xl)
            .padding(Spacing.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.accent), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Casino, null, tint = colors.onAccent)
            }
            Spacer(Modifier.width(Spacing.m))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.discover_surprise_overline).uppercase(), style = type.overline, color = colors.accentText)
                Text(stringResource(R.string.discover_surprise_title), style = type.title, color = colors.textPrimary)
            }
        }
        Spacer(Modifier.height(Spacing.m))
        Text(stringResource(R.string.discover_surprise_body), style = type.body, color = colors.textSecondary)
        AnimatedContent(
            targetState = pick,
            transitionSpec = {
                (fadeIn(motion.emphasized()) + scaleIn(motion.emphasized(), initialScale = 0.92f)) togetherWith fadeOut(motion.standard(150))
            },
            contentKey = { it?.id },
            label = "surprise",
        ) { sport ->
            if (sport != null) {
                Column {
                    Spacer(Modifier.height(Spacing.l))
                    FeaturedSportCard(
                        sport = sport,
                        category = category,
                        origin = "surprise",
                        onClick = { onOpen(sport.id) },
                        size = CardSize.WIDE,
                        supporting = sport.hook,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.l))
        RulePlayButton(
            text = stringResource(if (pick == null) R.string.action_surprise_me else R.string.action_surprise_again),
            onClick = onSurprise,
            icon = Icons.Rounded.Casino,
            emphasis = if (pick == null) ButtonEmphasis.PRIMARY else ButtonEmphasis.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LibraryShortcut(savedCount: Int, onOpen: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(
        Modifier
            .padding(horizontal = Spacing.gutter)
            .fillMaxWidth()
            .clip(Shapes.l)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.l)
            .pressable(onClick = onOpen)
            .padding(Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(colors.accentSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.AutoStories, null, tint = colors.accentText)
        }
        Spacer(Modifier.width(Spacing.l))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.discover_library_title), style = type.title, color = colors.textPrimary)
            Text(
                if (savedCount == 0) stringResource(R.string.discover_library_empty)
                else pluralStringResource(R.plurals.saved_items, savedCount, savedCount),
                style = type.caption, color = colors.textSecondary,
            )
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.textSecondary)
    }
}
