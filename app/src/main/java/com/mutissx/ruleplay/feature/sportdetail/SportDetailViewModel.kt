package com.mutissx.ruleplay.feature.sportdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutissx.ruleplay.domain.model.Favorite
import com.mutissx.ruleplay.domain.model.FavoriteType
import com.mutissx.ruleplay.domain.model.Rule
import com.mutissx.ruleplay.domain.model.RuleChapter
import com.mutissx.ruleplay.domain.model.Sport
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.Catalog
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.HistoryRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import com.mutissx.ruleplay.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SportDetailUiState {
    data object Loading : SportDetailUiState
    data object NotFound : SportDetailUiState
    data class Content(
        val sport: Sport,
        val origin: String,
        val category: SportCategory?,
        val categoriesById: Map<String, SportCategory>,
        val related: List<SportSummary>,
        val selectedVariantId: String?,
        val chapters: List<RuleChapter>,
        val keyRules: List<Rule>,
        val expandedRuleIds: Set<String>,
        val saved: Boolean,
        val savedRuleIds: Set<String>,
    ) : SportDetailUiState {
        val ruleCount: Int get() = chapters.sumOf { it.rules.size }
        val ruleTitles: Map<String, String> by lazy { sport.allRules.associate { it.id to it.title } }
    }
}

class SportDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val sportsRepository: SportsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.SportDetail>()
    private val selectedVariant = savedStateHandle.getMutableStateFlow<String?>(KEY_VARIANT, null)
    private val expanded = MutableStateFlow<Set<String>>(emptySet())

    private val data = flow {
        val catalog = sportsRepository.catalog()
        emit(catalog to sportsRepository.sport(route.sportId))
    }

    val state: StateFlow<SportDetailUiState> = combine(
        data,
        favoritesRepository.observeFavorites(),
        selectedVariant,
        expanded,
    ) { (catalog, sport), favorites, requestedVariant, expandedIds ->
        build(catalog, sport, favorites, requestedVariant, expandedIds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState())

    /**
     * When content and favorites are already cached (the normal case, as they are warmed at start-up), the
     * screen's very first frame shows the full page. That lets the artwork morph from the tapped card straight
     * into the hero, instead of a loading state appearing mid-transition.
     */
    private fun initialState(): SportDetailUiState {
        val catalog = sportsRepository.cachedCatalog() ?: return SportDetailUiState.Loading
        val sport = sportsRepository.cachedSport(route.sportId) ?: return SportDetailUiState.Loading
        val favorites = favoritesRepository.currentFavorites() ?: return SportDetailUiState.Loading
        return build(catalog, sport, favorites, selectedVariant.value, expanded.value)
    }

    private fun build(
        catalog: Catalog,
        sport: Sport?,
        favorites: List<Favorite>,
        requestedVariant: String?,
        expandedIds: Set<String>,
    ): SportDetailUiState {
        if (sport == null) return SportDetailUiState.NotFound
        val variantId = sport.resolveVariant(requestedVariant)
        val chapters = sport.chaptersFor(variantId)
        return SportDetailUiState.Content(
            sport = sport,
            origin = route.origin,
            category = catalog.category(sport.summary.categoryId),
            categoriesById = catalog.categories.associateBy { it.id },
            related = sport.summary.relatedIds.mapNotNull { catalog.sport(it) },
            selectedVariantId = variantId,
            chapters = chapters,
            keyRules = chapters.flatMap { it.rules }.filter { it.important }.take(MAX_KEY_RULES),
            expandedRuleIds = expandedIds,
            saved = favorites.any { it.type == FavoriteType.SPORT && it.sportId == sport.id },
            savedRuleIds = favorites.filter { it.type == FavoriteType.RULE && it.sportId == sport.id }.mapNotNull { it.ruleId }.toSet(),
        )
    }

    init {
        viewModelScope.launch { historyRepository.recordSportView(route.sportId) }
    }

    fun onSelectVariant(variantId: String) { selectedVariant.value = variantId }

    fun onToggleRuleExpanded(ruleId: String) {
        expanded.value = expanded.value.let { if (ruleId in it) it - ruleId else it + ruleId }
    }

    fun onToggleSave() {
        viewModelScope.launch { favoritesRepository.toggleSport(route.sportId) }
    }

    fun onToggleRuleSave(ruleId: String) {
        viewModelScope.launch { favoritesRepository.toggleRule(route.sportId, ruleId) }
    }

    private companion object {
        const val KEY_VARIANT = "detail.variant"
        const val MAX_KEY_RULES = 5
    }
}
