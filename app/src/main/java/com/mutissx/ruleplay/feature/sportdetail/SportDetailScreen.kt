package com.mutissx.ruleplay.feature.sportdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.art.ArtStyle
import com.mutissx.ruleplay.core.designsystem.art.FieldDiagram
import com.mutissx.ruleplay.core.designsystem.art.SportIllustration
import com.mutissx.ruleplay.core.designsystem.component.ArtScrim
import com.mutissx.ruleplay.core.designsystem.component.ButtonEmphasis
import com.mutissx.ruleplay.core.designsystem.component.EditorialBadge
import com.mutissx.ruleplay.core.designsystem.component.EmptyState
import com.mutissx.ruleplay.core.designsystem.component.FlowStepItem
import com.mutissx.ruleplay.core.designsystem.component.HighlightKind
import com.mutissx.ruleplay.core.designsystem.component.LoadingState
import com.mutissx.ruleplay.core.designsystem.component.MetaItem
import com.mutissx.ruleplay.core.designsystem.component.RuleHighlight
import com.mutissx.ruleplay.core.designsystem.component.RulePlayButton
import com.mutissx.ruleplay.core.designsystem.component.RulePlayTopBar
import com.mutissx.ruleplay.core.designsystem.component.RuleSectionCard
import com.mutissx.ruleplay.core.designsystem.component.SaveToggle
import com.mutissx.ruleplay.core.designsystem.component.SegmentedControl
import com.mutissx.ruleplay.core.designsystem.component.SharedKeys
import com.mutissx.ruleplay.core.designsystem.component.SourceStatusBadge
import com.mutissx.ruleplay.core.designsystem.component.SportMetadataRow
import com.mutissx.ruleplay.core.designsystem.component.SportThumb
import com.mutissx.ruleplay.core.designsystem.component.StatusBarOverArt
import com.mutissx.ruleplay.core.designsystem.component.Tag
import com.mutissx.ruleplay.core.designsystem.component.accentColor
import com.mutissx.ruleplay.core.designsystem.component.PhotoCreditRow
import com.mutissx.ruleplay.core.designsystem.component.pressable
import com.mutissx.ruleplay.core.designsystem.component.sharedBoundsOrNone
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Shapes
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.FieldInfo
import com.mutissx.ruleplay.domain.model.FlowStep
import com.mutissx.ruleplay.domain.model.Foul
import com.mutissx.ruleplay.domain.model.FoulSeverity
import com.mutissx.ruleplay.domain.model.Position
import com.mutissx.ruleplay.domain.model.Rule
import com.mutissx.ruleplay.domain.model.RuleChapter
import com.mutissx.ruleplay.domain.model.ScoringItem
import com.mutissx.ruleplay.domain.model.Term
import com.mutissx.ruleplay.navigation.AppNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SportDetailRoute(navigator: AppNavigator, viewModel: SportDetailViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SportDetailScreen(
        state = state,
        onBack = navigator::back,
        onOpenSport = { navigator.sport(it, "related") },
        onOpenReader = { variant, chapter, rule -> (state as? SportDetailUiState.Content)?.let { navigator.reader(it.sport.id, variant, chapter, rule) } },
        onSelectVariant = viewModel::onSelectVariant,
        onToggleRule = viewModel::onToggleRuleExpanded,
        onToggleSave = viewModel::onToggleSave,
        onToggleRuleSave = viewModel::onToggleRuleSave,
    )
}

/** Sections reachable from the sticky index. */
private enum class Section(val label: Int) {
    OVERVIEW(R.string.detail_tab_overview),
    RULES(R.string.detail_tab_rules),
    FOULS(R.string.detail_tab_fouls),
    FLOW(R.string.detail_tab_flow),
    POSITIONS(R.string.detail_tab_positions),
    FIELD(R.string.detail_tab_field),
    SOURCES(R.string.detail_tab_sources),
}

/** Flattened page model: each block is one lazy item, which makes section indices trivial. */
private sealed interface Block {
    val key: String

    data class Header(val section: Section, val title: Int, val overline: Int) : Block { override val key = "h-${section.name}" }
    data object Intro : Block { override val key = "intro" }
    data object Facts : Block { override val key = "facts" }
    data object Variants : Block { override val key = "variants" }
    data class Scoring(val items: List<ScoringItem>) : Block { override val key = "scoring" }
    data class KeyRule(val rule: Rule, val index: Int) : Block { override val key = "rule-${rule.id}" }
    data class Chapters(val chapters: List<RuleChapter>) : Block { override val key = "chapters" }
    data class FoulItem(val foul: Foul, val index: Int) : Block { override val key = "foul-$index" }
    data class Flow(val step: FlowStep, val index: Int, val last: Boolean) : Block { override val key = "flow-$index" }
    data class Positions(val positions: List<Position>) : Block { override val key = "positions" }
    data class Field(val field: FieldInfo) : Block { override val key = "field" }
    data class Glossary(val terms: List<Term>) : Block { override val key = "glossary" }
    data class FunFacts(val facts: List<String>) : Block { override val key = "facts-fun" }
    data object Sources : Block { override val key = "sources" }
    data object Related : Block { override val key = "related" }
}

private fun buildBlocks(state: SportDetailUiState.Content): List<Block> = buildList {
    val sport = state.sport
    add(Block.Intro)
    add(Block.Facts)
    if (sport.variants.size > 1) add(Block.Variants)
    if (sport.scoring.isNotEmpty()) add(Block.Scoring(sport.scoring))

    add(Block.Header(Section.RULES, R.string.detail_essential_rules, R.string.detail_essential_overline))
    state.keyRules.forEachIndexed { i, rule -> add(Block.KeyRule(rule, i + 1)) }
    add(Block.Chapters(state.chapters))

    if (sport.fouls.isNotEmpty()) {
        add(Block.Header(Section.FOULS, R.string.detail_fouls, R.string.detail_fouls_overline))
        sport.fouls.forEachIndexed { i, foul -> add(Block.FoulItem(foul, i)) }
    }
    if (sport.matchFlow.isNotEmpty()) {
        add(Block.Header(Section.FLOW, R.string.detail_flow, R.string.detail_flow_overline))
        sport.matchFlow.forEachIndexed { i, step -> add(Block.Flow(step, i, i == sport.matchFlow.lastIndex)) }
    }
    if (sport.positions.isNotEmpty()) {
        add(Block.Header(Section.POSITIONS, R.string.detail_positions, R.string.detail_positions_overline))
        add(Block.Positions(sport.positions))
    }
    sport.field?.let {
        add(Block.Header(Section.FIELD, R.string.detail_field, R.string.detail_field_overline))
        add(Block.Field(it))
    }
    if (sport.glossary.isNotEmpty()) add(Block.Glossary(sport.glossary))
    if (sport.funFacts.isNotEmpty()) add(Block.FunFacts(sport.funFacts))
    add(Block.Header(Section.SOURCES, R.string.detail_sources, R.string.detail_sources_overline))
    add(Block.Sources)
    if (state.related.isNotEmpty()) add(Block.Related)
}

private const val LEADING_ITEMS = 2 // hero + sticky index

@Composable
fun SportDetailScreen(
    state: SportDetailUiState,
    onBack: () -> Unit,
    onOpenSport: (String) -> Unit,
    onOpenReader: (variantId: String?, chapterId: String?, ruleId: String?) -> Unit,
    onSelectVariant: (String) -> Unit,
    onToggleRule: (String) -> Unit,
    onToggleSave: () -> Unit,
    onToggleRuleSave: (String) -> Unit,
) {
    val colors = RulePlayTheme.colors
    when (state) {
        SportDetailUiState.Loading -> Box(Modifier.fillMaxSize().background(colors.background)) { LoadingState(Modifier.align(Alignment.Center)) }
        SportDetailUiState.NotFound -> Box(Modifier.fillMaxSize().background(colors.background)) {
            EmptyState(
                title = stringResource(R.string.detail_not_found_title),
                message = stringResource(R.string.detail_not_found_body),
                actionLabel = stringResource(R.string.action_back),
                onAction = onBack,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        is SportDetailUiState.Content -> DetailContent(state, onBack, onOpenSport, onOpenReader, onSelectVariant, onToggleRule, onToggleSave, onToggleRuleSave)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailContent(
    state: SportDetailUiState.Content,
    onBack: () -> Unit,
    onOpenSport: (String) -> Unit,
    onOpenReader: (String?, String?, String?) -> Unit,
    onSelectVariant: (String) -> Unit,
    onToggleRule: (String) -> Unit,
    onToggleSave: () -> Unit,
    onToggleRuleSave: (String) -> Unit,
) {
    val colors = RulePlayTheme.colors
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val blocks = remember(state) { buildBlocks(state) }
    val sectionIndex = remember(blocks) {
        buildMap {
            put(Section.OVERVIEW, LEADING_ITEMS)
            blocks.forEachIndexed { i, b -> if (b is Block.Header) put(b.section, i + LEADING_ITEMS) }
        }
    }
    val sections = remember(sectionIndex) { Section.entries.filter { it in sectionIndex } }
    val heroPx = with(density) { 440.dp.toPx() }
    // Height covered by the top bar plus the pinned section index.
    val overlayPx = WindowInsets.statusBars.getTop(density) + with(density) { (64.dp + 68.dp).toPx() }
    // Positions are compared against the bottom edge of the top bar, so state flips exactly when content meets it.
    val topBarPx = WindowInsets.statusBars.getTop(density) + with(density) { 64.dp.toPx() }
    val solid by remember(topBarPx) {
        derivedStateOf {
            val items = listState.layoutInfo.visibleItemsInfo
            val hero = items.firstOrNull { it.index == 0 }
            items.isNotEmpty() && (hero == null || hero.offset + hero.size <= topBarPx)
        }
    }
    val pinned by remember(topBarPx) {
        derivedStateOf {
            val index = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == 1 }
            if (index != null) index.offset <= topBarPx else listState.firstVisibleItemIndex >= 1
        }
    }
    StatusBarOverArt(!solid)
    val currentSection by remember(sectionIndex, overlayPx) {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            var current = Section.OVERVIEW
            for (section in sections) {
                val index = sectionIndex.getValue(section)
                val item = visible.firstOrNull { it.index == index }
                val passed = if (item != null) item.offset <= overlayPx + 8 else index < listState.firstVisibleItemIndex
                if (passed) current = section
            }
            current
        }
    }
    val accent = state.category.accentColor()
    val onSelectSection: (Section) -> Unit = { section ->
        sectionIndex[section]?.let { index -> scope.launch { listState.animateScrollToItem(index, scrollOffset = -overlayPx.toInt()) } }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item(key = "hero") {
                DetailHero(
                    state = state,
                    parallax = {
                        if (listState.firstVisibleItemIndex > 0) 1f
                        else (listState.firstVisibleItemScrollOffset / heroPx).coerceIn(0f, 1f)
                    },
                )
            }
            item(key = "index") {
                SectionIndex(sections = sections, current = currentSection, onSelect = onSelectSection)
            }
            items(blocks, key = { it.key }) { block ->
                DetailBlock(block, state, accent, onOpenSport, onOpenReader, onSelectVariant, onToggleRule, onToggleRuleSave)
            }
            item(key = "bottom-space") {
                Spacer(Modifier.height(Spacing.xxxl))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // Pinned copy of the section index once the in-list one scrolls away.
        Column(Modifier.fillMaxWidth()) {
            Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(64.dp))
            androidx.compose.animation.AnimatedVisibility(
                visible = pinned,
                enter = androidx.compose.animation.fadeIn(RulePlayTheme.motion.standard()),
                exit = androidx.compose.animation.fadeOut(RulePlayTheme.motion.standard(120)),
            ) {
                SectionIndex(sections = sections, current = currentSection, onSelect = onSelectSection)
            }
        }

        RulePlayTopBar(
            title = state.sport.name,
            onBack = onBack,
            solidness = { if (solid) 1f else 0f },
            overArt = !solid,
        ) {
            SaveToggle(
                saved = state.saved,
                onToggle = onToggleSave,
                contentDescription = stringResource(if (state.saved) R.string.action_unsave_sport else R.string.action_save_sport, state.sport.name),
                onArt = !solid,
            )
        }
    }
}

@Composable
private fun DetailHero(state: SportDetailUiState.Content, parallax: () -> Float) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val sport = state.sport
    val accent = state.category.accentColor()
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 460.dp)
            .background(Palette.Ink),
    ) {
        SportIllustration(
            art = sport.summary.art,
            style = ArtStyle.COVER,
            parallax = parallax,
            contentDescription = stringResource(R.string.detail_art_description, sport.name),
            modifier = Modifier.matchParentSize().sharedBoundsOrNone(SharedKeys.art(state.origin, sport.id), scaleContent = true),
        )
        ArtScrim()
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Spacing.gutter)
                .padding(top = 80.dp, bottom = Spacing.xl),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(Spacing.s))
                Text(state.category?.name.orEmpty().uppercase(), style = type.overline, color = colors.onArtSecondary)
                Text("  ·  ${sport.summary.origin}".uppercase(), style = type.overline, color = colors.onArtSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(Spacing.s))
            Text(sport.name.uppercase(), style = type.displayHero, color = colors.onArt, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(Spacing.s))
            Text(sport.summary.tagline, style = type.bodyLarge, color = colors.onArtSecondary)
        }
    }
}

@Composable
private fun SectionIndex(sections: List<Section>, current: Section, onSelect: (Section) -> Unit) {
    val colors = RulePlayTheme.colors
    val motion = RulePlayTheme.motion
    val rowState = rememberLazyListState()
    val currentIndex = sections.indexOf(current)
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            if (motion.reduced) rowState.scrollToItem(currentIndex) else rowState.animateScrollToItem((currentIndex - 1).coerceAtLeast(0))
        }
    }
    Column(Modifier.fillMaxWidth().background(colors.background)) {
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            items(sections, key = { it.name }) { section ->
                val selected = section == current
                val bg by animateColorAsState(if (selected) colors.textPrimary else colors.surface, motion.standard(), label = "tabBg")
                val fg by animateColorAsState(if (selected) colors.background else colors.textSecondary, motion.standard(), label = "tabFg")
                Box(
                    Modifier
                        .clip(Shapes.pill)
                        .background(bg)
                        .border(1.dp, if (selected) Color.Transparent else colors.outline, Shapes.pill)
                        .semantics { this.selected = selected }
                        .pressable(onClick = { onSelect(section) }, role = Role.Tab, pressedScale = 0.94f)
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(section.label), style = RulePlayTheme.type.label, color = fg)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outline))
    }
}

@Composable
private fun DetailBlock(
    block: Block,
    state: SportDetailUiState.Content,
    accent: Color,
    onOpenSport: (String) -> Unit,
    onOpenReader: (String?, String?, String?) -> Unit,
    onSelectVariant: (String) -> Unit,
    onToggleRule: (String) -> Unit,
    onToggleRuleSave: (String) -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val sport = state.sport
    val gutter = Modifier.padding(horizontal = Spacing.gutter)
    when (block) {
        is Block.Header -> Column(gutter.padding(top = Spacing.xxxl, bottom = Spacing.l).semantics(mergeDescendants = true) { heading() }) {
            Text(stringResource(block.overline).uppercase(), style = type.overline, color = colors.accentText)
            Spacer(Modifier.height(Spacing.xs))
            Text(stringResource(block.title), style = type.headline, color = colors.textPrimary)
        }

        Block.Intro -> Column(gutter.padding(top = Spacing.xl)) {
            if (sport.isFallbackLanguage) {
                RuleHighlight(stringResource(R.string.detail_fallback_language), kind = HighlightKind.NOTE, title = stringResource(R.string.label_language))
                Spacer(Modifier.height(Spacing.l))
            }
            Text(sport.intro, style = type.bodyLarge, color = colors.textPrimary)
            Spacer(Modifier.height(Spacing.xl))
            ThirtySeconds(sport.inThirtySeconds)
            Spacer(Modifier.height(Spacing.l))
            RuleHighlight(sport.objective, kind = HighlightKind.KEY, title = stringResource(R.string.detail_objective))
        }

        Block.Facts -> Column(gutter.padding(top = Spacing.xl)) {
            SportMetadataRow(
                items = listOf(
                    MetaItem(Icons.Rounded.Groups, stringResource(R.string.detail_players), sport.facts.players),
                    MetaItem(Icons.Rounded.Timer, stringResource(R.string.detail_duration), sport.facts.duration),
                    MetaItem(Icons.Rounded.Landscape, stringResource(R.string.detail_environment), sport.facts.environment),
                    MetaItem(Icons.Rounded.SportsScore, stringResource(R.string.detail_equipment), sport.facts.equipment.joinToString(" · ")),
                ),
            )
        }

        Block.Variants -> Column(gutter.padding(top = Spacing.xl)) {
            Text(stringResource(R.string.detail_rule_set).uppercase(), style = type.overline, color = colors.textTertiary)
            Spacer(Modifier.height(Spacing.s))
            val ids = sport.variants.map { it.id }
            SegmentedControl(
                options = sport.variants.map { it.name },
                selectedIndex = ids.indexOf(state.selectedVariantId).coerceAtLeast(0),
                onSelect = { onSelectVariant(ids[it]) },
                modifier = Modifier.fillMaxWidth(),
            )
            sport.variants.firstOrNull { it.id == state.selectedVariantId }?.let {
                Spacer(Modifier.height(Spacing.m))
                Text(it.description, style = type.body, color = colors.textSecondary)
            }
        }

        is Block.Scoring -> Column(Modifier.padding(top = Spacing.xxl)) {
            Text(
                stringResource(R.string.detail_scoring).uppercase(), style = type.overline, color = colors.textTertiary,
                modifier = gutter.semantics { heading() },
            )
            Spacer(Modifier.height(Spacing.m))
            LazyRow(contentPadding = PaddingValues(horizontal = Spacing.gutter), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                items(block.items) { item -> ScoringCard(item, accent) }
            }
        }

        is Block.KeyRule -> RuleSectionCard(
            rule = block.rule,
            index = block.index,
            expanded = block.rule.id in state.expandedRuleIds,
            onToggle = { onToggleRule(block.rule.id) },
            saved = block.rule.id in state.savedRuleIds,
            onToggleSave = { onToggleRuleSave(block.rule.id) },
            relatedTitles = block.rule.relatedRuleIds.mapNotNull { id -> state.ruleTitles[id]?.let { id to it } },
            onRelatedClick = { onOpenReader(state.selectedVariantId, null, it) },
            modifier = gutter.padding(bottom = Spacing.m),
        )

        is Block.Chapters -> Column(gutter.padding(top = Spacing.m)) {
            ReaderCta(
                chapters = block.chapters.size,
                rules = state.ruleCount,
                onClick = { onOpenReader(state.selectedVariantId, null, null) },
            )
            Spacer(Modifier.height(Spacing.l))
            block.chapters.forEachIndexed { index, chapter ->
                ChapterRow(index + 1, chapter, onClick = { onOpenReader(state.selectedVariantId, chapter.id, null) })
            }
        }

        is Block.FoulItem -> FoulCard(block.foul, gutter.padding(bottom = Spacing.m))

        is Block.Flow -> FlowStepItem(block.index + 1, block.step.title, block.step.description, block.last, accent, gutter)

        is Block.Positions -> PositionsGrid(block.positions, gutter)

        is Block.Field -> FieldPanel(block.field, state, gutter)

        is Block.Glossary -> GlossaryPanel(block.terms, gutter.padding(top = Spacing.xxxl))

        is Block.FunFacts -> Column(Modifier.padding(top = Spacing.xxl)) {
            Text(stringResource(R.string.detail_did_you_know).uppercase(), style = type.overline, color = colors.accentText, modifier = gutter)
            Spacer(Modifier.height(Spacing.m))
            LazyRow(contentPadding = PaddingValues(horizontal = Spacing.gutter), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                items(block.facts) { fact ->
                    Box(
                        Modifier
                            .width(280.dp)
                            .clip(Shapes.l)
                            .background(accent.copy(alpha = 0.14f))
                            .padding(Spacing.xl),
                    ) {
                        Text(fact, style = type.bodyLarge, color = colors.textPrimary)
                    }
                }
            }
        }

        Block.Sources -> SourcesPanel(state, gutter)

        Block.Related -> Column(Modifier.padding(top = Spacing.xxxl)) {
            Column(gutter.semantics(mergeDescendants = true) { heading() }) {
                Text(stringResource(R.string.detail_related_overline).uppercase(), style = type.overline, color = colors.accentText)
                Spacer(Modifier.height(Spacing.xs))
                Text(stringResource(R.string.detail_related, sport.name), style = type.headline, color = colors.textPrimary)
            }
            Spacer(Modifier.height(Spacing.l))
            LazyRow(contentPadding = PaddingValues(horizontal = Spacing.gutter), horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                items(state.related, key = { it.id }) { related ->
                    SportThumb(
                        sport = related,
                        category = state.categoriesById[related.categoryId],
                        onClick = { onOpenSport(related.id) },
                        subtitle = related.hook,
                        modifier = Modifier.width(168.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThirtySeconds(points: List<String>) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(
        Modifier
            .fillMaxWidth()
            .clip(Shapes.l)
            .background(if (colors.isDark) Palette.Navy700 else Palette.InkText)
            .padding(Spacing.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Timer, null, tint = Palette.Lime, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.s))
            Text(stringResource(R.string.detail_thirty_seconds).uppercase(), style = type.overline, color = Palette.Lime, modifier = Modifier.semantics { heading() })
        }
        Spacer(Modifier.height(Spacing.l))
        points.forEachIndexed { i, point ->
            Row(Modifier.padding(bottom = if (i == points.lastIndex) 0.dp else Spacing.m).semantics(mergeDescendants = true) {}) {
                Text("${i + 1}", style = type.displaySmall, color = Palette.Lime, modifier = Modifier.width(30.dp))
                Text(point, style = type.body, color = Palette.OffWhite)
            }
        }
    }
}

@Composable
private fun ScoringCard(item: ScoringItem, accent: Color) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Column(
        Modifier
            .width(232.dp)
            .clip(Shapes.l)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.l)
            .padding(Spacing.xl)
            .semantics(mergeDescendants = true) {},
    ) {
        Text(item.value, style = type.displayLarge, color = accent)
        Spacer(Modifier.height(Spacing.xs))
        Text(item.label, style = type.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.height(Spacing.xs))
        Text(item.detail, style = type.caption, color = colors.textSecondary)
    }
}

@Composable
private fun ReaderCta(chapters: Int, rules: Int, onClick: () -> Unit) {
    val type = RulePlayTheme.type
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Shapes.l)
            .background(Palette.Lime)
            .pressable(onClick = onClick)
            .padding(Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = Palette.Ink, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(Spacing.l))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.detail_open_reader), style = type.title, color = Palette.Ink)
            Text(
                pluralStringResource(R.plurals.chapters_count, chapters, chapters) + " · " + pluralStringResource(R.plurals.rules_count, rules, rules),
                style = type.caption, color = Palette.Ink.copy(alpha = 0.7f),
            )
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Palette.Ink)
    }
}

@Composable
private fun ChapterRow(number: Int, chapter: RuleChapter, onClick: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Shapes.m)
            .pressable(onClick = onClick, pressedScale = 0.985f)
            .padding(vertical = Spacing.m),
        verticalAlignment = Alignment.Top,
    ) {
        Text("%02d".format(number), style = type.numeric, color = colors.textTertiary, modifier = Modifier.width(52.dp))
        Column(Modifier.weight(1f)) {
            Text(chapter.title, style = type.titleSmall, color = colors.textPrimary)
            Text(chapter.summary, style = type.caption, color = colors.textSecondary)
        }
        Spacer(Modifier.width(Spacing.s))
        Text(
            pluralStringResource(R.plurals.rules_count, chapter.rules.size, chapter.rules.size),
            style = type.caption, color = colors.textTertiary,
        )
    }
}

@Composable
private fun FoulCard(foul: Foul, modifier: Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val (tone, label) = when (foul.severity) {
        FoulSeverity.MINOR -> colors.textSecondary to stringResource(R.string.severity_minor)
        FoulSeverity.MAJOR -> colors.warning to stringResource(R.string.severity_major)
        FoulSeverity.SEVERE -> colors.danger to stringResource(R.string.severity_severe)
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(Shapes.m)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.m)
            .padding(Spacing.l)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(foul.name, style = type.titleSmall, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Tag(label, color = tone, container = tone.copy(alpha = 0.12f), dot = tone)
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(foul.description, style = type.body, color = colors.textSecondary)
        Spacer(Modifier.height(Spacing.m))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Flag, null, tint = tone, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(Spacing.s))
            Text(foul.consequence, style = type.bodyStrong, color = colors.textPrimary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PositionsGrid(positions: List<Position>, modifier: Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
        maxItemsInEachRow = 2,
    ) {
        positions.forEach { position ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(Shapes.m)
                    .background(colors.surface)
                    .border(1.dp, colors.outline, Shapes.m)
                    .padding(Spacing.l)
                    .semantics(mergeDescendants = true) {},
            ) {
                Text(position.name, style = type.titleSmall, color = colors.textPrimary)
                Spacer(Modifier.height(Spacing.xs))
                Text(position.role, style = type.caption, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun FieldPanel(field: FieldInfo, state: SportDetailUiState.Content, modifier: Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val accent = state.category.accentColor()
    Column(
        modifier
            .fillMaxWidth()
            .clip(Shapes.l)
            .background(colors.surface)
            .border(1.dp, colors.outline, Shapes.l),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .background(Color(state.sport.summary.art.base))
                .padding(Spacing.m),
        ) {
            FieldDiagram(
                art = state.sport.summary.art,
                lineColor = Palette.OffWhite.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(Modifier.padding(Spacing.xl)) {
            Text(field.name, style = type.title, color = colors.textPrimary)
            Spacer(Modifier.height(Spacing.s))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(Spacing.s))
                Text(field.dimensions, style = type.bodyStrong, color = colors.textPrimary)
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(field.surface, style = type.body, color = colors.textSecondary)
            if (field.notes.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.l))
                field.notes.forEach { note ->
                    Row(Modifier.padding(bottom = Spacing.s)) {
                        Text("—", style = type.body, color = accent)
                        Spacer(Modifier.width(Spacing.s))
                        Text(note, style = type.body, color = colors.textPrimary)
                    }
                }
            }
            Spacer(Modifier.height(Spacing.s))
            Text(stringResource(R.string.detail_diagram_note), style = type.caption, color = colors.textTertiary)
        }
    }
}

@Composable
private fun GlossaryPanel(terms: List<Term>, modifier: Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    var expanded by rememberSaveable { mutableStateOf(false) }
    val visible = if (expanded) terms else terms.take(4)
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.semantics(mergeDescendants = true) { heading() }) {
            Text(stringResource(R.string.detail_glossary_overline).uppercase(), style = type.overline, color = colors.accentText)
            Spacer(Modifier.height(Spacing.xs))
            Text(stringResource(R.string.detail_glossary), style = type.headline, color = colors.textPrimary)
        }
        Spacer(Modifier.height(Spacing.l))
        visible.forEach { term ->
            Column(Modifier.padding(bottom = Spacing.l).semantics(mergeDescendants = true) {}) {
                Text(term.term, style = type.titleSmall, color = colors.textPrimary)
                Text(term.definition, style = type.body, color = colors.textSecondary)
            }
        }
        if (terms.size > 4) {
            RulePlayButton(
                text = stringResource(if (expanded) R.string.action_show_less else R.string.action_show_all_terms, terms.size),
                onClick = { expanded = !expanded },
                emphasis = ButtonEmphasis.GHOST,
            )
        }
    }
}

@Composable
private fun SourcesPanel(state: SportDetailUiState.Content, modifier: Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val uriHandler = LocalUriHandler.current
    val sport = state.sport
    Column(modifier.fillMaxWidth()) {
        RuleHighlight(stringResource(R.string.detail_sources_disclaimer), kind = HighlightKind.NOTE, title = stringResource(R.string.label_editorial_summary))
        Spacer(Modifier.height(Spacing.l))
        sport.rulebooks.forEach { book ->
            val publisher = sport.governingBodies.firstOrNull { it.id == book.publisherId }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.m)
                    .clip(Shapes.m)
                    .background(colors.surface)
                    .border(1.dp, colors.outline, Shapes.m)
                    .padding(Spacing.l),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = colors.accentText, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.m))
                    Column(Modifier.weight(1f)) {
                        Text(book.title, style = type.titleSmall, color = colors.textPrimary)
                        val meta = listOfNotNull(publisher?.acronym ?: publisher?.name, book.edition, sport.variants.firstOrNull { it.id == book.variantId }?.name)
                        if (meta.isNotEmpty()) Text(meta.joinToString(" · "), style = type.caption, color = colors.textSecondary)
                    }
                }
                Spacer(Modifier.height(Spacing.m))
                SourceStatusBadge(book.linkCheckedOn)
                book.url?.let { url ->
                    Spacer(Modifier.height(Spacing.m))
                    RulePlayButton(
                        text = stringResource(R.string.action_open_official_source),
                        onClick = { runCatching { uriHandler.openUri(url) } },
                        emphasis = ButtonEmphasis.SECONDARY,
                        trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (sport.governingBodies.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.m))
            Text(stringResource(R.string.detail_governing_bodies).uppercase(), style = type.overline, color = colors.textTertiary)
            Spacer(Modifier.height(Spacing.s))
            sport.governingBodies.forEach { body ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Shapes.s)
                        .then(if (body.url != null) Modifier.pressable(onClick = { runCatching { uriHandler.openUri(body.url) } }, pressedScale = 0.99f) else Modifier)
                        .padding(vertical = Spacing.m)
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceRaised), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AccountBalance, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(Spacing.m))
                    Column(Modifier.weight(1f)) {
                        Text(body.acronym?.let { "$it — ${body.name}" } ?: body.name, style = type.titleSmall, color = colors.textPrimary)
                        Text(body.scope, style = type.caption, color = colors.textSecondary)
                    }
                    if (body.url != null) Icon(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(R.string.action_open_website), tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                }
            }
        }
        sport.summary.photoCredit?.let { credit ->
            Spacer(Modifier.height(Spacing.m))
            Text(stringResource(R.string.detail_cover_photo).uppercase(), style = type.overline, color = colors.textTertiary)
            PhotoCreditRow(credit, label = sport.name)
        }
        if (sport.isFallbackLanguage) {
            Spacer(Modifier.height(Spacing.m))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Translate, null, tint = colors.textTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.detail_fallback_language), style = type.caption, color = colors.textTertiary)
            }
        }
        Spacer(Modifier.height(Spacing.m))
        EditorialBadge()
    }
}
