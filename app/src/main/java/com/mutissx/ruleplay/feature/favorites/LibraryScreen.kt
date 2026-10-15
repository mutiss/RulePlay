package com.mutissx.ruleplay.feature.favorites

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.EmptyState
import com.mutissx.ruleplay.core.designsystem.component.LoadingState
import com.mutissx.ruleplay.core.designsystem.component.ScreenTitle
import com.mutissx.ruleplay.core.designsystem.component.StatusBarScrim
import com.mutissx.ruleplay.core.designsystem.component.SegmentedControl
import com.mutissx.ruleplay.core.designsystem.component.SportRow
import com.mutissx.ruleplay.core.designsystem.component.Tag
import com.mutissx.ruleplay.core.designsystem.component.pressable
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.Favorite
import com.mutissx.ruleplay.feature.discover.BottomBarSpace
import com.mutissx.ruleplay.navigation.AppNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryRoute(navigator: AppNavigator, viewModel: LibraryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val removedMessage = stringResource(R.string.library_removed)
    val undoLabel = stringResource(R.string.action_undo)
    LaunchedEffect(Unit) {
        viewModel.removed.collect { item ->
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(removedMessage.format(item.label), actionLabel = undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.onUndo(item)
        }
    }
    Box(Modifier.fillMaxSize()) {
        LibraryScreen(
            state = state,
            onSelectTab = viewModel::onSelectTab,
            onOpenSport = { navigator.sport(it, "library") },
            onOpenRule = { sportId, ruleId -> navigator.reader(sportId, ruleId = ruleId) },
            onRemove = viewModel::onRemove,
            onExplore = navigator::explore,
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = BottomBarSpace))
    }
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onSelectTab: (LibraryTab) -> Unit,
    onOpenSport: (String) -> Unit,
    onOpenRule: (String, String) -> Unit,
    onRemove: (Favorite, String) -> Unit,
    onExplore: () -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val tabs = LibraryTab.entries
    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = BottomBarSpace),
    ) {
        item(key = "title") {
            Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = Spacing.xxl)) {
                ScreenTitle(
                    title = stringResource(R.string.library_title),
                    overline = stringResource(R.string.library_overline),
                    subtitle = stringResource(R.string.library_subtitle),
                )
            }
        }
        item(key = "tabs") {
            SegmentedControl(
                options = listOf(
                    stringResource(R.string.library_tab_sports, state.sports.size),
                    stringResource(R.string.library_tab_rules, state.rules.size),
                ),
                selectedIndex = tabs.indexOf(state.tab),
                onSelect = { onSelectTab(tabs[it]) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter).padding(top = Spacing.xl, bottom = Spacing.l),
            )
        }
        when {
            state.loading -> item(key = "loading") { LoadingState() }
            state.tab == LibraryTab.SPORTS && state.sports.isEmpty() -> item(key = "empty-sports") {
                EmptyState(
                    title = stringResource(R.string.library_empty_sports_title),
                    message = stringResource(R.string.library_empty_sports_body),
                    icon = Icons.Rounded.FavoriteBorder,
                    actionLabel = stringResource(R.string.action_explore_sports),
                    onAction = onExplore,
                )
            }
            state.tab == LibraryTab.RULES && state.rules.isEmpty() -> item(key = "empty-rules") {
                EmptyState(
                    title = stringResource(R.string.library_empty_rules_title),
                    message = stringResource(R.string.library_empty_rules_body),
                    icon = Icons.Rounded.BookmarkBorder,
                    actionLabel = stringResource(R.string.action_explore_sports),
                    onAction = onExplore,
                )
            }
            state.tab == LibraryTab.SPORTS -> items(state.sports, key = { it.favorite.key }) { item ->
                SwipeToRemove(onRemove = { onRemove(item.favorite, item.sport.name) }, label = item.sport.name) {
                    SportRow(
                        sport = item.sport,
                        category = state.categoriesById[item.sport.categoryId],
                        onClick = { onOpenSport(item.sport.id) },
                        modifier = Modifier.background(colors.background).padding(horizontal = Spacing.gutter),
                    )
                }
            }
            else -> items(state.rules, key = { it.favorite.key }) { item ->
                val saved = item.saved
                SwipeToRemove(onRemove = { onRemove(item.favorite, saved.rule.title) }, label = saved.rule.title) {
                    Column(
                        Modifier
                            .background(colors.background)
                            .padding(horizontal = Spacing.gutter, vertical = Spacing.s)
                            .fillMaxWidth()
                            .clip(Shapes.m)
                            .background(colors.surface)
                            .border(1.dp, colors.outline, Shapes.m)
                            .pressable(onClick = { onOpenRule(saved.sport.id, saved.rule.id) }, pressedScale = 0.985f)
                            .padding(Spacing.l),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${saved.sport.name} · ${saved.chapterTitle}".uppercase(), style = type.overline, color = colors.accentText,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                            )
                            saved.rule.reference?.let { Tag(it) }
                        }
                        Spacer(Modifier.height(Spacing.s))
                        Text(saved.rule.title, style = type.titleSmall, color = colors.textPrimary)
                        Spacer(Modifier.height(Spacing.xs))
                        Text(saved.rule.summary, style = type.body, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        val currentTabHasItems = if (state.tab == LibraryTab.SPORTS) state.sports.isNotEmpty() else state.rules.isNotEmpty()
        if (!state.loading && currentTabHasItems) {
            item(key = "hint") {
                Text(
                    pluralStringResource(R.plurals.library_hint, state.sports.size + state.rules.size, state.sports.size + state.rules.size),
                    style = type.caption, color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.l),
                )
            }
        }
    }
    StatusBarScrim()
    }
}

/** Swipe left to remove, with an accessibility action as the non-gesture alternative. */
@Composable
private fun SwipeToRemove(onRemove: () -> Unit, label: String, content: @Composable () -> Unit) {
    val colors = RulePlayTheme.colors
    val dismissState = rememberSwipeToDismissBoxState()
    val removeLabel = stringResource(R.string.library_remove_item, label)
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onRemove()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = Modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(removeLabel) { onRemove(); true })
        },
        backgroundContent = {
            Row(
                Modifier.fillMaxSize().background(colors.danger.copy(alpha = 0.16f)).padding(horizontal = Spacing.xl),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.action_remove), style = RulePlayTheme.type.label, color = colors.danger)
                Spacer(Modifier.width(Spacing.s))
                Icon(Icons.Rounded.DeleteOutline, null, tint = colors.danger, modifier = Modifier.size(22.dp))
            }
        },
    ) {
        content()
    }
}
