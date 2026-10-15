package com.mutissx.ruleplay.feature.rulereader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutissx.ruleplay.domain.model.FavoriteType
import com.mutissx.ruleplay.domain.model.ReaderTextSize
import com.mutissx.ruleplay.domain.model.RuleChapter
import com.mutissx.ruleplay.domain.model.Rulebook
import com.mutissx.ruleplay.domain.model.Sport
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.SettingsRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import com.mutissx.ruleplay.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data object NotFound : ReaderUiState
    data class Content(
        val sport: Sport,
        val category: SportCategory?,
        val variantId: String?,
        val chapters: List<RuleChapter>,
        val expandedRuleIds: Set<String>,
        val savedRuleIds: Set<String>,
        val textSize: ReaderTextSize,
        /** Rule or chapter to bring into view when the reader opens. */
        val initialRuleId: String?,
        val initialChapterId: String?,
    ) : ReaderUiState {
        val variantName: String? get() = sport.variants.firstOrNull { it.id == variantId }?.name
        val rulebook: Rulebook? get() = sport.rulebooks.firstOrNull { it.variantId == variantId } ?: sport.rulebooks.firstOrNull()
        val ruleTitles: Map<String, String> by lazy { sport.allRules.associate { it.id to it.title } }
        val ruleCount: Int get() = chapters.sumOf { it.rules.size }
    }
}

class RuleReaderViewModel(
    savedStateHandle: SavedStateHandle,
    private val sportsRepository: SportsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.RuleReader>()
    private val variant = savedStateHandle.getMutableStateFlow(KEY_VARIANT, route.variantId)
    // The rule the user arrived for starts expanded.
    private val expanded = MutableStateFlow(setOfNotNull(route.ruleId))

    val state: StateFlow<ReaderUiState> = combine(
        flow { emit(sportsRepository.catalog() to sportsRepository.sport(route.sportId)) },
        favoritesRepository.observeFavorites(),
        variant,
        expanded,
        settingsRepository.settings,
    ) { (catalog, sport), favorites, requestedVariant, expandedIds, settings ->
        if (sport == null) return@combine ReaderUiState.NotFound
        val variantId = sport.resolveVariant(requestedVariant)
        ReaderUiState.Content(
            sport = sport,
            category = catalog.category(sport.summary.categoryId),
            variantId = variantId,
            chapters = sport.chaptersFor(variantId),
            expandedRuleIds = expandedIds,
            savedRuleIds = favorites.filter { it.type == FavoriteType.RULE && it.sportId == sport.id }.mapNotNull { it.ruleId }.toSet(),
            textSize = settings.readerTextSize,
            initialRuleId = route.ruleId,
            initialChapterId = route.chapterId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderUiState.Loading)

    fun onToggleExpanded(ruleId: String) {
        expanded.value = expanded.value.let { if (ruleId in it) it - ruleId else it + ruleId }
    }

    fun onExpand(ruleId: String) {
        expanded.value = expanded.value + ruleId
    }

    fun onExpandAll(expand: Boolean) {
        val content = state.value as? ReaderUiState.Content ?: return
        expanded.value = if (expand) content.chapters.flatMap { c -> c.rules.map { it.id } }.toSet() else emptySet()
    }

    fun onToggleSave(ruleId: String) {
        viewModelScope.launch { favoritesRepository.toggleRule(route.sportId, ruleId) }
    }

    fun onSelectVariant(variantId: String) { variant.value = variantId }

    /** Cycles through reader text sizes (persisted as a user preference). */
    fun onCycleTextSize() {
        viewModelScope.launch {
            val current = settingsRepository.settings.first().readerTextSize
            val all = ReaderTextSize.entries
            settingsRepository.setReaderTextSize(all[(all.indexOf(current) + 1) % all.size])
        }
    }

    private companion object {
        const val KEY_VARIANT = "reader.variant"
    }
}
