package com.mutissx.ruleplay.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.EmptyState
import com.mutissx.ruleplay.core.designsystem.component.RoundIconButton
import com.mutissx.ruleplay.core.designsystem.component.SearchField
import com.mutissx.ruleplay.core.designsystem.component.SharedKeys
import com.mutissx.ruleplay.core.designsystem.component.SportCategoryChip
import com.mutissx.ruleplay.core.designsystem.component.SportRow
import com.mutissx.ruleplay.core.designsystem.component.Tag
import com.mutissx.ruleplay.core.designsystem.component.accentColor
import com.mutissx.ruleplay.core.designsystem.component.pressable
import com.mutissx.ruleplay.core.designsystem.component.rememberHighlighted
import com.mutissx.ruleplay.core.designsystem.component.sharedBoundsOrNone
import com.mutissx.ruleplay.core.designsystem.component.spokenAs
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.MatchField
import com.mutissx.ruleplay.domain.model.SearchResult
import com.mutissx.ruleplay.navigation.AppNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(navigator: AppNavigator, viewModel: SearchViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onBack = navigator::back,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::onCommit,
        onOpenSport = { viewModel.onCommit(); navigator.sport(it, "search") },
        onOpenRule = { sportId, ruleId -> viewModel.onCommit(); navigator.reader(sportId, ruleId = ruleId) },
        onOpenCategory = navigator::category,
        onRemoveRecent = viewModel::onRemoveRecent,
        onClearRecent = viewModel::onClearRecent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenSport: (String) -> Unit,
    onOpenRule: (String, String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearRecent: () -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val tryTerms = stringArrayResource(R.array.search_try_terms).toList()

    LaunchedEffect(Unit) {
        if (state.query.isBlank()) runCatching { focusRequester.requestFocus() }
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .padding(start = Spacing.s, end = Spacing.gutter, top = Spacing.s, bottom = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(Spacing.s))
            SearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.search_placeholder),
                focusRequester = focusRequester,
                onSubmit = { onSubmit(); keyboard?.hide() },
                modifier = Modifier.weight(1f).sharedBoundsOrNone(SharedKeys.SEARCH_BAR),
            )
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.xxxl),
        ) {
            if (state.showIdle) {
                if (state.recentSearches.isNotEmpty()) {
                    item(key = "recent-header") {
                        Row(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.l), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.search_recent).uppercase(), style = type.overline, color = colors.textTertiary, modifier = Modifier.weight(1f).spokenAs(stringResource(R.string.search_recent)).semantics { heading() })
                            Text(
                                stringResource(R.string.action_clear_all), style = type.label, color = colors.accentText,
                                modifier = Modifier.clip(Shapes.pill).pressable(onClick = onClearRecent).padding(horizontal = Spacing.m, vertical = Spacing.m),
                            )
                        }
                    }
                    items(state.recentSearches, key = { "recent-$it" }) { recent ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .pressable(onClick = { onQueryChange(recent) }, pressedScale = 0.99f)
                                .padding(start = Spacing.gutter, end = Spacing.s),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.History, null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(Spacing.l))
                            Text(recent, style = type.bodyLarge, color = colors.textPrimary, modifier = Modifier.weight(1f).padding(vertical = Spacing.m))
                            Box(
                                Modifier.size(48.dp).clip(CircleShape).pressable(onClick = { onRemoveRecent(recent) }, onClickLabel = stringResource(R.string.action_remove)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.Close, stringResource(R.string.search_remove_recent, recent), tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                item(key = "try") {
                    Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl)) {
                        Text(stringResource(R.string.search_try).uppercase(), style = type.overline, color = colors.textTertiary, modifier = Modifier.spokenAs(stringResource(R.string.search_try)).semantics { heading() })
                        Spacer(Modifier.height(Spacing.m))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            tryTerms.forEach { term ->
                                Box(
                                    Modifier
                                        .clip(Shapes.pill)
                                        .border(1.dp, colors.outlineStrong, Shapes.pill)
                                        .pressable(onClick = { onQueryChange(term) }, pressedScale = 0.94f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(term, style = type.label, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
                item(key = "categories") {
                    Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl)) {
                        Text(stringResource(R.string.search_browse_categories).uppercase(), style = type.overline, color = colors.textTertiary, modifier = Modifier.spokenAs(stringResource(R.string.search_browse_categories)).semantics { heading() })
                        Spacer(Modifier.height(Spacing.m))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            state.categories.forEach { category ->
                                SportCategoryChip(category.name, Color(category.accent), selected = false, onClick = { onOpenCategory(category.id) })
                            }
                        }
                    }
                }
                item(key = "suggested") {
                    Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl)) {
                        Text(stringResource(R.string.search_suggested).uppercase(), style = type.overline, color = colors.textTertiary, modifier = Modifier.spokenAs(stringResource(R.string.search_suggested)).semantics { heading() })
                        Spacer(Modifier.height(Spacing.s))
                        state.suggestedSports.forEach { sport ->
                            SportRow(sport, state.categoriesById[sport.categoryId], onClick = { onOpenSport(sport.id) }, supporting = sport.hook)
                        }
                    }
                }
            } else {
                val results = state.results
                item(key = "summary") {
                    val summary = when {
                        results == null -> stringResource(R.string.search_searching)
                        results.isEmpty -> stringResource(R.string.search_no_results_short)
                        else -> pluralStringResource(R.plurals.search_results_count, results.sports.size + results.rules.size + results.terms.size, results.sports.size + results.rules.size + results.terms.size)
                    }
                    Text(
                        summary, style = type.caption, color = colors.textTertiary,
                        modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.s).semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
                if (results != null && results.isEmpty && !state.searching) {
                    item(key = "empty") {
                        EmptyState(
                            title = stringResource(R.string.search_no_results_title, state.query),
                            message = stringResource(R.string.search_no_results_body),
                            icon = Icons.Rounded.SearchOff,
                        )
                    }
                }
                if (results != null) {
                    if (results.sports.isNotEmpty()) {
                        item(key = "sports-header") { ResultHeader(stringResource(R.string.search_section_sports)) }
                        items(results.sports, key = { "s-${it.sport.id}" }) { hit ->
                            val supporting = when (hit.field) {
                                MatchField.ALT_NAME -> stringResource(R.string.search_also_known_as, hit.matchedText)
                                MatchField.CATEGORY -> hit.matchedText
                                else -> hit.sport.tagline
                            }
                            SportHitRow(hit, state.query, supporting, state, onOpenSport)
                        }
                    }
                    if (results.rules.isNotEmpty()) {
                        item(key = "rules-header") { ResultHeader(stringResource(R.string.search_section_rules)) }
                        items(results.rules, key = { "r-${it.sport.id}-${it.rule.id}" }) { hit ->
                            RuleHitCard(hit, state.query, onClick = { onOpenRule(hit.sport.id, hit.rule.id) })
                        }
                    }
                    if (results.terms.isNotEmpty()) {
                        item(key = "terms-header") { ResultHeader(stringResource(R.string.search_section_terms)) }
                        items(results.terms, key = { "t-${it.sport.id}-${it.term.term}" }) { hit ->
                            TermHitRow(hit, state.query, onClick = { onOpenSport(hit.sport.id) })
                        }
                    }
                }
            }
            item(key = "ime") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime)) }
        }
    }
}

@Composable
private fun ResultHeader(text: String) {
    Text(
        text.uppercase(), style = RulePlayTheme.type.overline, color = RulePlayTheme.colors.accentText,
        modifier = Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl, bottom = Spacing.s).spokenAs(text).semantics { heading() },
    )
}

@Composable
private fun SportHitRow(hit: SearchResult.SportHit, query: String, supporting: String, state: SearchUiState, onOpenSport: (String) -> Unit) {
    SportRow(
        sport = hit.sport,
        category = state.categoriesById[hit.sport.categoryId],
        onClick = { onOpenSport(hit.sport.id) },
        supporting = supporting,
        title = rememberHighlighted(hit.sport.name, query),
        modifier = Modifier.padding(horizontal = Spacing.gutter),
    )
}

@Composable
private fun RuleHitCard(hit: SearchResult.RuleHit, query: String, onClick: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val title = rememberHighlighted(hit.rule.title, query)
    val snippet = rememberHighlighted(hit.snippet, query)
    Column(
        Modifier
            .padding(horizontal = Spacing.gutter)
            .padding(bottom = Spacing.m)
            .fillMaxWidth()
            .clip(Shapes.m)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.m)
            .pressable(onClick = onClick, pressedScale = 0.985f)
            .padding(Spacing.l),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = colors.textTertiary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "${hit.sport.name} · ${hit.chapterTitle}".uppercase(), style = type.overline, color = colors.textTertiary,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            hit.rule.reference?.let { Tag(it) }
        }
        Spacer(Modifier.height(Spacing.s))
        Text(title, style = type.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.height(Spacing.xs))
        Text(snippet, style = type.body, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TermHitRow(hit: SearchResult.TermHit, query: String, onClick: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val term = rememberHighlighted(hit.term.term, query)
    val definition = rememberHighlighted(hit.term.definition, query)
    Row(
        Modifier
            .fillMaxWidth()
            .pressable(onClick = onClick, pressedScale = 0.99f)
            .padding(horizontal = Spacing.gutter, vertical = Spacing.m),
    ) {
        Icon(Icons.Rounded.Translate, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(Spacing.m))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(term, style = type.titleSmall, color = colors.textPrimary, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(Spacing.s))
                Tag(hit.sport.name, dot = null)
            }
            Text(definition, style = type.body, color = colors.textSecondary)
        }
    }
}
