package com.mutissx.ruleplay.feature.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.ruleplay.domain.model.Favorite
import com.mutissx.ruleplay.domain.model.FavoriteType
import com.mutissx.ruleplay.domain.model.SavedRule
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab { SPORTS, RULES }

data class SavedSport(val sport: SportSummary, val favorite: Favorite)

data class SavedRuleItem(val saved: SavedRule, val favorite: Favorite)

data class LibraryUiState(
    val loading: Boolean = true,
    val tab: LibraryTab = LibraryTab.SPORTS,
    val sports: List<SavedSport> = emptyList(),
    val rules: List<SavedRuleItem> = emptyList(),
    val categoriesById: Map<String, SportCategory> = emptyMap(),
)

/** A removal the user can undo from the snackbar. */
data class RemovedItem(val favorite: Favorite, val label: String)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favoritesRepository: FavoritesRepository,
    private val sportsRepository: SportsRepository,
) : ViewModel() {

    private val tab = savedStateHandle.getStateFlow(KEY_TAB, LibraryTab.SPORTS.name)

    private val _removed = MutableSharedFlow<RemovedItem>(extraBufferCapacity = 1)
    val removed: SharedFlow<RemovedItem> = _removed

    private val resolved = favoritesRepository.observeFavorites().mapLatest { favorites ->
        val catalog = sportsRepository.catalog()
        val sports = favorites.filter { it.type == FavoriteType.SPORT }
            .mapNotNull { fav -> catalog.sport(fav.sportId)?.let { SavedSport(it, fav) } }
        val rules = favorites.filter { it.type == FavoriteType.RULE }.mapNotNull { fav ->
            val sport = sportsRepository.sport(fav.sportId) ?: return@mapNotNull null
            val ruleId = fav.ruleId ?: return@mapNotNull null
            val rule = sport.rule(ruleId) ?: return@mapNotNull null
            SavedRuleItem(SavedRule(sport.summary, sport.chapterOf(ruleId)?.title.orEmpty(), rule, fav.savedAt), fav)
        }
        Triple(sports, rules, catalog.categories.associateBy { it.id })
    }

    val state: StateFlow<LibraryUiState> = combine(resolved, tab) { (sports, rules, categories), selectedTab ->
        LibraryUiState(
            loading = false,
            tab = LibraryTab.valueOf(selectedTab),
            sports = sports,
            rules = rules,
            categoriesById = categories,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onSelectTab(value: LibraryTab) { savedStateHandle[KEY_TAB] = value.name }

    fun onRemove(favorite: Favorite, label: String) {
        viewModelScope.launch {
            favoritesRepository.remove(favorite)
            _removed.emit(RemovedItem(favorite, label))
        }
    }

    fun onUndo(item: RemovedItem) {
        viewModelScope.launch { favoritesRepository.restore(item.favorite) }
    }

    private companion object {
        const val KEY_TAB = "library.tab"
    }
}
