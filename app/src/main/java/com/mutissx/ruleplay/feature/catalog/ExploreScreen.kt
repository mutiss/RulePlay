package com.mutissx.ruleplay.feature.catalog

import androidx.compose.ui.semantics.paneTitle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.CardSize
import com.mutissx.ruleplay.core.designsystem.component.EmptyState
import com.mutissx.ruleplay.core.designsystem.component.ErrorState
import com.mutissx.ruleplay.core.designsystem.component.FeaturedSportCard
import com.mutissx.ruleplay.core.designsystem.component.LoadingState
import com.mutissx.ruleplay.core.designsystem.component.RulePlayTopBar
import com.mutissx.ruleplay.core.designsystem.component.SaveStyle
import com.mutissx.ruleplay.core.designsystem.component.SaveToggle
import com.mutissx.ruleplay.core.designsystem.component.ScreenTitle
import com.mutissx.ruleplay.core.designsystem.component.SearchField
import com.mutissx.ruleplay.core.designsystem.component.SegmentedControl
import com.mutissx.ruleplay.core.designsystem.component.SportCategoryChip
import com.mutissx.ruleplay.core.designsystem.component.SportRow
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.SortOrder
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.feature.discover.BottomBarSpace
import com.mutissx.ruleplay.navigation.AppNavigator
import org.koin.androidx.compose.koinViewModel

private const val ORIGIN = "explore"

@Composable
fun ExploreRoute(
    navigator: AppNavigator,
    @Suppress("UNUSED_PARAMETER") categoryId: String?,
    showBack: Boolean,
    viewModel: ExploreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExploreScreen(
        state = state,
        showBack = showBack,
        onBack = navigator::back,
        onOpenSport = { navigator.sport(it, ORIGIN) },
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onSortChange = viewModel::onSortChange,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
        onClearFilters = viewModel::onClearFilters,
        onToggleFavorite = viewModel::onToggleFavorite,
    )
}

/** Rows of the editorial grid: a full-width feature or a pair of tall cards. */
private sealed interface GridRow {
    data class Wide(val sport: SportSummary) : GridRow
    data class Pair(val first: SportSummary, val second: SportSummary?) : GridRow
}

private fun editorialRows(sports: List<SportSummary>): List<GridRow> {
    val rows = mutableListOf<GridRow>()
    var i = 0
    var block = 0
    while (i < sports.size) {
        // Every third row is a wide feature to break the rhythm.
        if (block % 3 == 0) {
            rows += GridRow.Wide(sports[i]); i++
        } else {
            rows += GridRow.Pair(sports[i], sports.getOrNull(i + 1)); i += 2
        }
        block++
    }
    return rows
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(
    state: ExploreUiState,
    showBack: Boolean,
    onBack: () -> Unit,
    onOpenSport: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val listState = rememberLazyListState()
    val sortOptions = listOf(SortOrder.CURATED, SortOrder.POPULAR, SortOrder.ALPHABETICAL)
    val sortLabels = listOf(stringResource(R.string.sort_curated), stringResource(R.string.sort_popular), stringResource(R.string.sort_az))
    val category = state.selectedCategory
    val screenTitle = category?.name ?: stringResource(R.string.explore_title)

    Box(Modifier.fillMaxSize().background(colors.background).semantics { paneTitle = screenTitle }) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(top = if (showBack) 64.dp else 0.dp),
            contentPadding = PaddingValues(bottom = BottomBarSpace),
        ) {
            item(key = "title") {
                if (showBack && category != null) {
                    CategoryBanner(category.name, category.description, Color(category.accent), state.results.size)
                } else {
                    Column(Modifier.padding(top = Spacing.xxl)) {
                        ScreenTitle(
                            title = stringResource(R.string.explore_title),
                            overline = stringResource(R.string.explore_overline),
                            subtitle = pluralStringResource(R.plurals.explore_subtitle, state.totalSports, state.totalSports),
                        )
                    }
                }
            }
            item(key = "search") {
                Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl)) {
                    SearchField(
                        value = state.filter.query,
                        onValueChange = onQueryChange,
                        placeholder = stringResource(R.string.explore_filter_placeholder),
                    )
                }
            }
            stickyHeader(key = "chips") {
                LazyRow(
                    Modifier.fillMaxWidth().background(colors.background).padding(vertical = Spacing.m),
                    contentPadding = PaddingValues(horizontal = Spacing.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    item(key = "all") {
                        SportCategoryChip(
                            label = stringResource(R.string.explore_all),
                            accent = Palette.Lime,
                            selected = state.filter.categoryId == null,
                            onClick = { onCategorySelect(null) },
                        )
                    }
                    items(state.categories, key = { it.category.id }) { option ->
                        SportCategoryChip(
                            label = option.category.name,
                            accent = Color(option.category.accent),
                            selected = state.filter.categoryId == option.category.id,
                            onClick = { onCategorySelect(option.category.id) },
                            count = option.count,
                        )
                    }
                }
            }
            item(key = "controls") {
                Row(
                    Modifier.padding(horizontal = Spacing.gutter).padding(bottom = Spacing.l),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SegmentedControl(
                        options = sortLabels,
                        selectedIndex = sortOptions.indexOf(state.filter.sort),
                        onSelect = { onSortChange(sortOptions[it]) },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.padding(start = Spacing.s))
                    SaveToggle(
                        saved = state.filter.favoritesOnly,
                        onToggle = { onFavoritesOnlyChange(!state.filter.favoritesOnly) },
                        contentDescription = stringResource(R.string.explore_favorites_only),
                        style = SaveStyle.HEART,
                        size = 52.dp,
                    )
                }
            }

            when {
                state.loading -> item(key = "loading") { LoadingState() }
                state.error -> item(key = "error") { ErrorState(stringResource(R.string.error_content), onRetry = null) }
                state.results.isEmpty() -> item(key = "empty") {
                    EmptyState(
                        title = stringResource(if (state.filter.favoritesOnly) R.string.explore_empty_favorites_title else R.string.explore_empty_title),
                        message = stringResource(if (state.filter.favoritesOnly) R.string.explore_empty_favorites_body else R.string.explore_empty_body),
                        icon = Icons.Rounded.SearchOff,
                        actionLabel = stringResource(R.string.action_clear_filters),
                        onAction = onClearFilters,
                    )
                }
                state.filter.sort == SortOrder.ALPHABETICAL -> {
                    val groups = state.results.groupBy { it.name.first().uppercaseChar() }
                    groups.forEach { (letter, sports) ->
                        item(key = "letter-$letter") {
                            Text(
                                letter.toString(),
                                style = type.displaySmall,
                                color = colors.accentText,
                                modifier = Modifier
                                    .padding(horizontal = Spacing.gutter)
                                    .padding(top = Spacing.l, bottom = Spacing.xs)
                                    .semantics { heading() },
                            )
                        }
                        items(sports, key = { "az-${it.id}" }) { sport ->
                            SportRow(
                                sport = sport,
                                category = state.categoriesById[sport.categoryId],
                                onClick = { onOpenSport(sport.id) },
                                modifier = Modifier.padding(horizontal = Spacing.gutter),
                                trailing = {
                                    SaveToggle(
                                        saved = sport.id in state.favoriteIds,
                                        onToggle = { onToggleFavorite(sport.id) },
                                        contentDescription = stringResource(
                                            if (sport.id in state.favoriteIds) R.string.action_unsave_sport else R.string.action_save_sport,
                                            sport.name,
                                        ),
                                        size = 44.dp,
                                    )
                                },
                            )
                        }
                    }
                }
                else -> {
                    val rows = editorialRows(state.results)
                    items(rows.size, key = { index ->
                        when (val row = rows[index]) {
                            is GridRow.Wide -> "wide-${row.sport.id}"
                            is GridRow.Pair -> "pair-${row.first.id}"
                        }
                    }) { index ->
                        val modifier = Modifier.padding(horizontal = Spacing.gutter).padding(bottom = Spacing.m)
                        when (val row = rows[index]) {
                            is GridRow.Wide -> FeaturedSportCard(
                                sport = row.sport,
                                category = state.categoriesById[row.sport.categoryId],
                                origin = ORIGIN,
                                onClick = { onOpenSport(row.sport.id) },
                                size = CardSize.WIDE,
                                supporting = row.sport.hook,
                                modifier = modifier.fillMaxWidth().animateItem(),
                            )
                            is GridRow.Pair -> Row(modifier.animateItem(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                                FeaturedSportCard(
                                    sport = row.first,
                                    category = state.categoriesById[row.first.categoryId],
                                    origin = ORIGIN,
                                    onClick = { onOpenSport(row.first.id) },
                                    modifier = Modifier.weight(1f),
                                )
                                if (row.second != null) {
                                    FeaturedSportCard(
                                        sport = row.second,
                                        category = state.categoriesById[row.second.categoryId],
                                        origin = ORIGIN,
                                        onClick = { onOpenSport(row.second.id) },
                                        modifier = Modifier.weight(1f),
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBack) {
            RulePlayTopBar(
                title = category?.name.orEmpty(),
                onBack = onBack,
                solidness = { 1f },
                overArt = false,
            )
        }
    }
}

@Composable
private fun CategoryBanner(name: String, description: String, accent: Color, count: Int) {
    val type = RulePlayTheme.type
    Column(
        Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.linearGradient(listOf(lerp(accent, Color.White, 0.1f), lerp(accent, Palette.Ink, 0.45f))))
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.xl, bottom = Spacing.xxl),
    ) {
        Text(pluralStringResource(R.plurals.sports_count, count, count).uppercase(), style = type.overline, color = Palette.Ink.copy(alpha = 0.7f))
        Spacer(Modifier.height(Spacing.s))
        Text(name.uppercase(), style = type.displayLarge, color = Palette.Ink, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(Spacing.s))
        Text(description, style = type.bodyLarge, color = Palette.Ink.copy(alpha = 0.8f))
    }
}
