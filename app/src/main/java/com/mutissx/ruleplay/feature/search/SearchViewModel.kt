package com.mutissx.ruleplay.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.ruleplay.domain.model.SearchResults
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.HistoryRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import com.mutissx.ruleplay.domain.usecase.DiscoveryUseCase
import com.mutissx.ruleplay.domain.usecase.SearchContentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val searching: Boolean = false,
    val results: SearchResults? = null,
    val recentSearches: List<String> = emptyList(),
    val suggestedSports: List<SportSummary> = emptyList(),
    val categories: List<SportCategory> = emptyList(),
    val categoriesById: Map<String, SportCategory> = emptyMap(),
) {
    val showIdle: Boolean get() = query.isBlank()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val search: SearchContentUseCase,
    private val historyRepository: HistoryRepository,
    private val sportsRepository: SportsRepository,
    private val discovery: DiscoveryUseCase,
) : ViewModel() {

    private val query = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private sealed interface Outcome {
        data object Idle : Outcome
        data object Searching : Outcome
        data class Done(val results: SearchResults) : Outcome
    }

    private val outcome = query
        .debounce { if (it.isBlank()) 0L else 160L }
        .distinctUntilChanged()
        .mapLatest { q -> if (q.isBlank()) Outcome.Idle else Outcome.Done(search(q)) }
        .onStart { emit(Outcome.Idle) }

    private val catalog = flow { emit(sportsRepository.catalog()) }

    val state: StateFlow<SearchUiState> = combine(
        query,
        outcome,
        historyRepository.observeRecentSearches(),
        catalog,
    ) { q, result, recent, catalog ->
        val done = result as? Outcome.Done
        SearchUiState(
            query = q,
            // Results that belong to an older query are still shown while the new one runs.
            searching = q.isNotBlank() && (done == null || done.results.query != q.trim()),
            results = done?.results,
            recentSearches = recent,
            suggestedSports = discovery.hiddenGems(catalog).take(3) + discovery.popular(catalog, 3),
            categories = catalog.categories,
            categoriesById = catalog.categories.associateBy { it.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onQueryChange(value: String) { savedStateHandle[KEY_QUERY] = value }

    /** Remembers the query once the user commits to it (submits or opens a result). */
    fun onCommit() {
        val q = query.value
        if (q.isNotBlank()) viewModelScope.launch { historyRepository.addRecentSearch(q) }
    }

    fun onRemoveRecent(value: String) {
        viewModelScope.launch { historyRepository.removeRecentSearch(value) }
    }

    fun onClearRecent() {
        viewModelScope.launch { historyRepository.clearRecentSearches() }
    }

    private companion object {
        const val KEY_QUERY = "search.query"
    }
}
