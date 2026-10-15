package com.mutissx.ruleplay.feature.rulereader

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.ButtonEmphasis
import com.mutissx.ruleplay.core.designsystem.component.EditorialBadge
import com.mutissx.ruleplay.core.designsystem.component.EmptyState
import com.mutissx.ruleplay.core.designsystem.component.HighlightKind
import com.mutissx.ruleplay.core.designsystem.component.LoadingState
import com.mutissx.ruleplay.core.designsystem.component.ProgressTrack
import com.mutissx.ruleplay.core.designsystem.component.RoundIconButton
import com.mutissx.ruleplay.core.designsystem.component.RuleHighlight
import com.mutissx.ruleplay.core.designsystem.component.RulePlayButton
import com.mutissx.ruleplay.core.designsystem.component.RuleSectionCard
import com.mutissx.ruleplay.core.designsystem.component.SegmentedControl
import com.mutissx.ruleplay.core.designsystem.component.SourceStatusBadge
import com.mutissx.ruleplay.core.designsystem.component.accentColor
import com.mutissx.ruleplay.core.designsystem.component.pressable
import com.mutissx.ruleplay.core.designsystem.component.scaled
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.Rule
import com.mutissx.ruleplay.domain.model.RuleChapter
import com.mutissx.ruleplay.navigation.AppNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun RuleReaderRoute(navigator: AppNavigator, viewModel: RuleReaderViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RuleReaderScreen(
        state = state,
        onBack = navigator::back,
        onToggleExpanded = viewModel::onToggleExpanded,
        onExpand = viewModel::onExpand,
        onExpandAll = viewModel::onExpandAll,
        onToggleSave = viewModel::onToggleSave,
        onSelectVariant = viewModel::onSelectVariant,
        onCycleTextSize = viewModel::onCycleTextSize,
    )
}

private sealed interface ReaderItem {
    val key: String

    data object Intro : ReaderItem { override val key = "intro" }
    data class ChapterHeader(val chapter: RuleChapter, val number: Int) : ReaderItem { override val key = "c-${chapter.id}" }
    data class RuleItem(val rule: Rule, val chapterNumber: Int, val number: Int) : ReaderItem { override val key = "r-${rule.id}" }
    data object Footer : ReaderItem { override val key = "footer" }
}

private fun buildItems(chapters: List<RuleChapter>): List<ReaderItem> = buildList {
    add(ReaderItem.Intro)
    chapters.forEachIndexed { c, chapter ->
        add(ReaderItem.ChapterHeader(chapter, c + 1))
        chapter.rules.forEachIndexed { r, rule -> add(ReaderItem.RuleItem(rule, c + 1, r + 1)) }
    }
    add(ReaderItem.Footer)
}

@Composable
fun RuleReaderScreen(
    state: ReaderUiState,
    onBack: () -> Unit,
    onToggleExpanded: (String) -> Unit,
    onExpand: (String) -> Unit,
    onExpandAll: (Boolean) -> Unit,
    onToggleSave: (String) -> Unit,
    onSelectVariant: (String) -> Unit,
    onCycleTextSize: () -> Unit,
) {
    val colors = RulePlayTheme.colors
    when (state) {
        ReaderUiState.Loading -> Box(Modifier.fillMaxSize().background(colors.background)) { LoadingState(Modifier.align(Alignment.Center)) }
        ReaderUiState.NotFound -> Box(Modifier.fillMaxSize().background(colors.background)) {
            EmptyState(
                stringResource(R.string.detail_not_found_title), stringResource(R.string.detail_not_found_body),
                actionLabel = stringResource(R.string.action_back), onAction = onBack, modifier = Modifier.align(Alignment.Center),
            )
        }
        is ReaderUiState.Content -> ReaderContent(state, onBack, onToggleExpanded, onExpand, onExpandAll, onToggleSave, onSelectVariant, onCycleTextSize)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderContent(
    state: ReaderUiState.Content,
    onBack: () -> Unit,
    onToggleExpanded: (String) -> Unit,
    onExpand: (String) -> Unit,
    onExpandAll: (Boolean) -> Unit,
    onToggleSave: (String) -> Unit,
    onSelectVariant: (String) -> Unit,
    onCycleTextSize: () -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val motion = RulePlayTheme.motion
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val items = remember(state.chapters) { buildItems(state.chapters) }
    val indexOfKey = remember(items) { items.withIndex().associate { (i, item) -> item.key to i } }
    val textScale = state.textSize.scale
    var showToc by rememberSaveable { mutableStateOf(false) }
    var didInitialScroll by rememberSaveable { mutableStateOf(false) }

    val currentChapter by remember(items) {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            (first downTo 0).asSequence().map { items.getOrNull(it) }.firstNotNullOfOrNull {
                when (it) {
                    is ReaderItem.ChapterHeader -> it.number
                    is ReaderItem.RuleItem -> it.chapterNumber
                    else -> null
                }
            } ?: 1
        }
    }
    val progress by remember(items) {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            if (total <= 1) 0f else {
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (lastVisible >= total - 1) 1f else listState.firstVisibleItemIndex / (total - 1).toFloat()
            }
        }
    }

    fun scrollTo(key: String) {
        val index = indexOfKey[key] ?: return
        scope.launch { if (motion.reduced) listState.scrollToItem(index) else listState.animateScrollToItem(index) }
    }

    LaunchedEffect(state.initialRuleId, state.initialChapterId, items) {
        if (didInitialScroll) return@LaunchedEffect
        didInitialScroll = true
        val key = state.initialRuleId?.let { "r-$it" } ?: state.initialChapterId?.let { "c-$it" } ?: return@LaunchedEffect
        indexOfKey[key]?.let { listState.scrollToItem(it) }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize()) {
            ReaderTopBar(
                title = state.sport.name,
                subtitle = stringResource(R.string.reader_chapter_of, currentChapter, state.chapters.size),
                progress = progress,
                allExpanded = state.expandedRuleIds.size >= state.ruleCount,
                onBack = onBack,
                onToc = { showToc = true },
                onTextSize = onCycleTextSize,
                onExpandAll = onExpandAll,
            )
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Spacing.xxxl)) {
                items(items, key = { it.key }) { item ->
                    when (item) {
                        ReaderItem.Intro -> ReaderIntro(state, onSelectVariant)
                        is ReaderItem.ChapterHeader -> ChapterHeader(item.chapter, item.number, textScale)
                        is ReaderItem.RuleItem -> RuleSectionCard(
                            rule = item.rule,
                            index = item.number,
                            expanded = item.rule.id in state.expandedRuleIds,
                            onToggle = { onToggleExpanded(item.rule.id) },
                            saved = item.rule.id in state.savedRuleIds,
                            onToggleSave = { onToggleSave(item.rule.id) },
                            relatedTitles = item.rule.relatedRuleIds.mapNotNull { id -> state.ruleTitles[id]?.let { id to it } },
                            onRelatedClick = { id -> onExpand(id); scrollTo("r-$id") },
                            textScale = textScale,
                            modifier = Modifier.padding(horizontal = Spacing.gutter).padding(bottom = Spacing.m),
                        )
                        ReaderItem.Footer -> ReaderFooter(state)
                    }
                }
                item(key = "nav-space") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
            }
        }
    }

    if (showToc) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showToc = false },
            sheetState = sheetState,
            containerColor = colors.surfaceRaised,
        ) {
            TableOfContents(
                chapters = state.chapters,
                current = currentChapter,
                onSelect = { chapter ->
                    scope.launch {
                        sheetState.hide()
                        showToc = false
                        indexOfKey["c-${chapter.id}"]?.let { listState.scrollToItem(it) }
                    }
                },
            )
        }
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    subtitle: String,
    progress: Float,
    allExpanded: Boolean,
    onBack: () -> Unit,
    onToc: () -> Unit,
    onTextSize: () -> Unit,
    onExpandAll: (Boolean) -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(Modifier.fillMaxWidth().background(colors.background).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back), onBack)
            Column(Modifier.weight(1f).padding(horizontal = Spacing.s)) {
                Text(title, style = type.titleSmall, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = type.caption, color = colors.textSecondary, maxLines = 1)
            }
            RoundIconButton(
                if (allExpanded) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                stringResource(if (allExpanded) R.string.reader_collapse_all else R.string.reader_expand_all),
                { onExpandAll(!allExpanded) },
            )
            RoundIconButton(Icons.Rounded.TextFields, stringResource(R.string.reader_text_size), onTextSize)
            RoundIconButton(Icons.AutoMirrored.Rounded.ViewList, stringResource(R.string.reader_contents), onToc)
        }
        ProgressTrack(progress, Modifier.fillMaxWidth())
    }
}

@Composable
private fun ReaderIntro(state: ReaderUiState.Content, onSelectVariant: (String) -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val sport = state.sport
    Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl, bottom = Spacing.l)) {
        Text(stringResource(R.string.reader_overline).uppercase(), style = type.overline, color = colors.accentText)
        Spacer(Modifier.height(Spacing.s))
        Text(
            stringResource(R.string.reader_title, sport.name).uppercase(),
            style = type.displayMedium, color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(Spacing.m))
        Text(
            pluralStringResource(R.plurals.chapters_count, state.chapters.size, state.chapters.size) + " · " +
                pluralStringResource(R.plurals.rules_count, state.ruleCount, state.ruleCount),
            style = type.body, color = colors.textSecondary,
        )
        if (sport.variants.size > 1) {
            Spacer(Modifier.height(Spacing.l))
            val ids = sport.variants.map { it.id }
            SegmentedControl(
                options = sport.variants.map { it.name },
                selectedIndex = ids.indexOf(state.variantId).coerceAtLeast(0),
                onSelect = { onSelectVariant(ids[it]) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(Spacing.l))
        RuleHighlight(stringResource(R.string.reader_disclaimer), kind = HighlightKind.NOTE, title = stringResource(R.string.label_editorial_summary))
        if (sport.isFallbackLanguage) {
            Spacer(Modifier.height(Spacing.m))
            RuleHighlight(stringResource(R.string.detail_fallback_language), kind = HighlightKind.NOTE, title = stringResource(R.string.label_language))
        }
    }
}

@Composable
private fun ChapterHeader(chapter: RuleChapter, number: Int, textScale: Float) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(
        Modifier
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.xxl, bottom = Spacing.l)
            .semantics(mergeDescendants = true) { heading() },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.reader_chapter, number).uppercase(), style = type.overline, color = colors.accentText)
            Spacer(Modifier.width(Spacing.m))
            Box(Modifier.weight(1f).height(1.dp).background(colors.outline))
        }
        Spacer(Modifier.height(Spacing.m))
        Text(chapter.title, style = type.headline.scaled(textScale), color = colors.textPrimary)
        Spacer(Modifier.height(Spacing.xs))
        Text(chapter.summary, style = type.body.scaled(textScale), color = colors.textSecondary)
    }
}

@Composable
private fun ReaderFooter(state: ReaderUiState.Content) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val uriHandler = LocalUriHandler.current
    val book = state.rulebook
    val publisher = book?.let { b -> state.sport.governingBodies.firstOrNull { it.id == b.publisherId } }
    Column(
        Modifier
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.xxl)
            .fillMaxWidth()
            .clip(Shapes.l)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.l)
            .padding(Spacing.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(state.category.accentColor().copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = state.category.accentColor())
            }
            Spacer(Modifier.width(Spacing.m))
            Text(stringResource(R.string.reader_source_title), style = type.title, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Spacer(Modifier.height(Spacing.l))
        if (book != null) {
            SourceLine(stringResource(R.string.reader_source_rulebook), book.title)
            publisher?.let { SourceLine(stringResource(R.string.reader_source_publisher), it.acronym?.let { a -> "$a — ${it.name}" } ?: it.name) }
            SourceLine(stringResource(R.string.reader_source_edition), book.edition ?: stringResource(R.string.reader_source_edition_unknown))
            state.variantName?.let { SourceLine(stringResource(R.string.detail_rule_set), it) }
            Spacer(Modifier.height(Spacing.s))
            SourceStatusBadge(book.linkCheckedOn)
            book.url?.let { url ->
                Spacer(Modifier.height(Spacing.l))
                RulePlayButton(
                    stringResource(R.string.action_open_official_source),
                    onClick = { runCatching { uriHandler.openUri(url) } },
                    emphasis = ButtonEmphasis.SECONDARY,
                    trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Text(stringResource(R.string.reader_source_none), style = type.body, color = colors.textSecondary)
        }
        Spacer(Modifier.height(Spacing.l))
        EditorialBadge()
    }
}

@Composable
private fun SourceLine(label: String, value: String) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(Modifier.padding(bottom = Spacing.m).semantics(mergeDescendants = true) {}) {
        Text(label.uppercase(), style = type.overline, color = colors.textTertiary)
        Text(value, style = type.body, color = colors.textPrimary)
    }
}

@Composable
private fun TableOfContents(chapters: List<RuleChapter>, current: Int, onSelect: (RuleChapter) -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(Modifier.padding(horizontal = Spacing.gutter).padding(bottom = Spacing.xxl)) {
        Text(stringResource(R.string.reader_contents).uppercase(), style = type.displaySmall, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(Spacing.l))
        chapters.forEachIndexed { i, chapter ->
            val selected = i + 1 == current
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Shapes.m)
                    .background(if (selected) colors.accentSoft else androidx.compose.ui.graphics.Color.Transparent)
                    .pressable(onClick = { onSelect(chapter) }, pressedScale = 0.98f)
                    .padding(Spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("%02d".format(i + 1), style = type.numeric, color = if (selected) colors.accentText else colors.textTertiary, modifier = Modifier.width(52.dp))
                Column(Modifier.weight(1f)) {
                    Text(chapter.title, style = type.titleSmall, color = colors.textPrimary)
                    Text(pluralStringResource(R.plurals.rules_count, chapter.rules.size, chapter.rules.size), style = type.caption, color = colors.textSecondary)
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

