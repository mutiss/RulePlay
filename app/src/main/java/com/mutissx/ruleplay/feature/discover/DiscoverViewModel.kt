package com.mutissx.ruleplay.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.ruleplay.domain.model.FavoriteType
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.Catalog
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.HistoryRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import com.mutissx.ruleplay.domain.usecase.DiscoveryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CategoryTile(val category: SportCategory, val count: Int, val showcase: SportSummary?)

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
    data class Content(
        val sportOfTheDay: SportSummary,
        val categories: List<CategoryTile>,
        val categoriesById: Map<String, SportCategory>,
        val popular: List<SportSummary>,
        val hiddenGems: List<SportSummary>,
        val becauseOf: SportSummary?,
        val recommendations: List<SportSummary>,
        val recent: List<SportSummary>,
        val favoriteSportIds: Set<String>,
        val savedCount: Int,
        val surprise: SportSummary?,
        val totalSports: Int,
        val contentVersion: String,
    ) : DiscoverUiState
}

class DiscoverViewModel(
    private val sportsRepository: SportsRepository,
    private val favoritesRepository: FavoritesRepository,
    historyRepository: HistoryRepository,
    private val discovery: DiscoveryUseCase,
) : ViewModel() {

    private val surprise = MutableStateFlow<SportSummary?>(null)
    private val catalog = flow { emit(runCatching { sportsRepository.catalog() }) }

    val state: StateFlow<DiscoverUiState> = combine(
        catalog,
        favoritesRepository.observeFavorites(),
        historyRepository.observeRecentSports(limit = 12),
        surprise,
    ) { catalogResult, favorites, recentIds, surprisePick ->
        catalogResult.fold(
            onSuccess = { build(it, favorites.filter { f -> f.type == FavoriteType.SPORT }.map { f -> f.sportId }.toSet(), favorites.size, recentIds, surprisePick) },
            onFailure = { DiscoverUiState.Error(it.message ?: "") },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DiscoverUiState.Loading)

    private var lastCatalog: Catalog? = null

    private fun build(
        catalog: Catalog,
        favoriteIds: Set<String>,
        savedCount: Int,
        recentIds: List<String>,
        surprisePick: SportSummary?,
    ): DiscoverUiState {
        lastCatalog = catalog
        val today = discovery.sportOfTheDay(catalog, LocalDate.now().toEpochDay())
            ?: return DiscoverUiState.Error("Empty catalog")
        val tiles = catalog.categories.map { category ->
            val sports = catalog.sportsIn(category.id)
            CategoryTile(category, sports.size, sports.maxByOrNull { it.popularity })
        }.filter { it.count > 0 }
        val recent = recentIds.mapNotNull { catalog.sport(it) }
        return DiscoverUiState.Content(
            sportOfTheDay = today,
            categories = tiles,
            categoriesById = catalog.categories.associateBy { it.id },
            popular = discovery.popular(catalog, limit = 10).filter { it.id != today.id },
            hiddenGems = discovery.hiddenGems(catalog),
            becauseOf = recent.firstOrNull(),
            recommendations = discovery.becauseYouViewed(catalog, recentIds),
            recent = recent,
            favoriteSportIds = favoriteIds,
            savedCount = savedCount,
            surprise = surprisePick,
            totalSports = catalog.sports.size,
            contentVersion = catalog.version,
        )
    }

    fun onSurpriseMe() {
        val catalog = lastCatalog ?: return
        surprise.value = discovery.surprise(catalog, excludeId = surprise.value?.id)
    }

    fun onToggleFavorite(sportId: String) {
        viewModelScope.launch { favoritesRepository.toggleSport(sportId) }
    }
}
