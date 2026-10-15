package com.mutissx.ruleplay.feature.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.ruleplay.domain.model.FavoriteType
import com.mutissx.ruleplay.domain.model.SortOrder
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import com.mutissx.ruleplay.domain.usecase.FilterSportsUseCase
import com.mutissx.ruleplay.domain.usecase.SportFilter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryOption(val category: SportCategory, val count: Int)

data class ExploreUiState(
    val loading: Boolean = true,
    val filter: SportFilter = SportFilter(),
    val categories: List<CategoryOption> = emptyList(),
    val categoriesById: Map<String, SportCategory> = emptyMap(),
    val results: List<SportSummary> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val totalSports: Int = 0,
    val error: Boolean = false,
) {
    val selectedCategory: SportCategory? get() = filter.categoryId?.let { categoriesById[it] }
}

class ExploreViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sportsRepository: SportsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val filterSports: FilterSportsUseCase,
) : ViewModel() {

    // Filter lives in SavedStateHandle so it survives process death and configuration changes.
    private val filterFlow = combine(
        savedStateHandle.getStateFlow(KEY_CATEGORY, savedStateHandle.get<String>("categoryId")),
        savedStateHandle.getStateFlow(KEY_QUERY, ""),
        savedStateHandle.getStateFlow(KEY_SORT, SortOrder.CURATED.name),
        savedStateHandle.getStateFlow(KEY_FAVORITES, false),
    ) { category, query, sort, favoritesOnly ->
        SportFilter(category, query, SortOrder.valueOf(sort), favoritesOnly)
    }

    private val favoriteIds = favoritesRepository.observeFavorites()
        .map { list -> list.filter { it.type == FavoriteType.SPORT }.map { it.sportId }.toSet() }

    val state: StateFlow<ExploreUiState> = combine(
        flow { emit(runCatching { sportsRepository.catalog() }) },
        filterFlow,
        favoriteIds,
    ) { catalogResult, filter, favorites ->
        val catalog = catalogResult.getOrNull() ?: return@combine ExploreUiState(loading = false, error = true, filter = filter)
        ExploreUiState(
            loading = false,
            filter = filter,
            categories = catalog.categories.map { CategoryOption(it, catalog.sportsIn(it.id).size) }.filter { it.count > 0 },
            categoriesById = catalog.categories.associateBy { it.id },
            results = filterSports(catalog, filter, favorites),
            favoriteIds = favorites,
            totalSports = catalog.sports.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun onQueryChange(query: String) { savedStateHandle[KEY_QUERY] = query }
    fun onCategorySelect(categoryId: String?) { savedStateHandle[KEY_CATEGORY] = categoryId }
    fun onSortChange(sort: SortOrder) { savedStateHandle[KEY_SORT] = sort.name }
    fun onFavoritesOnlyChange(enabled: Boolean) { savedStateHandle[KEY_FAVORITES] = enabled }

    fun onClearFilters() {
        savedStateHandle[KEY_QUERY] = ""
        savedStateHandle[KEY_CATEGORY] = null
        savedStateHandle[KEY_FAVORITES] = false
    }

    fun onToggleFavorite(sportId: String) {
        viewModelScope.launch { favoritesRepository.toggleSport(sportId) }
    }

    private companion object {
        const val KEY_CATEGORY = "explore.category"
        const val KEY_QUERY = "explore.query"
        const val KEY_SORT = "explore.sort"
        const val KEY_FAVORITES = "explore.favorites"
    }
}
