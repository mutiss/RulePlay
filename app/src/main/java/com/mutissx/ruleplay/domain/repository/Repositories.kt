package com.mutissx.ruleplay.domain.repository

import com.mutissx.ruleplay.domain.model.Favorite
import com.mutissx.ruleplay.domain.model.ReaderTextSize
import com.mutissx.ruleplay.domain.model.Sport
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.model.ThemeMode
import com.mutissx.ruleplay.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** The browsable index of the encyclopedia. */
data class Catalog(
    val version: String,
    val categories: List<SportCategory>,
    val sports: List<SportSummary>,
) {
    private val sportsById by lazy { sports.associateBy { it.id } }
    private val categoriesById by lazy { categories.associateBy { it.id } }

    fun sport(id: String): SportSummary? = sportsById[id]
    fun category(id: String): SportCategory? = categoriesById[id]
    fun sportsIn(categoryId: String): List<SportSummary> = sports.filter { categoryId in it.allCategoryIds }
}

/** Read-only access to the bundled, offline encyclopedia content. */
interface SportsRepository {
    suspend fun catalog(): Catalog

    /** Full entry for [id], or null if it does not exist. */
    suspend fun sport(id: String): Sport?

    /** The catalog if already loaded, without suspending — used to render the first frame synchronously. */
    fun cachedCatalog(): Catalog?

    /** A sport if already parsed, without suspending. */
    fun cachedSport(id: String): Sport?

    /** Every full entry — used to build the search index. */
    suspend fun allSports(): List<Sport>
}

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Favorite>>

    /** Latest known favorites without suspending, or null if not loaded yet. */
    fun currentFavorites(): List<Favorite>?

    /** Toggles a saved sport; returns true when it is now saved. */
    suspend fun toggleSport(sportId: String): Boolean

    /** Toggles a saved rule; returns true when it is now saved. */
    suspend fun toggleRule(sportId: String, ruleId: String): Boolean

    suspend fun remove(favorite: Favorite)

    suspend fun restore(favorite: Favorite)
}

interface HistoryRepository {
    /** Most recently viewed sport ids, newest first. */
    fun observeRecentSports(limit: Int = 10): Flow<List<String>>

    suspend fun recordSportView(sportId: String)

    fun observeRecentSearches(limit: Int = 8): Flow<List<String>>

    suspend fun addRecentSearch(query: String)

    suspend fun removeRecentSearch(query: String)

    suspend fun clearRecentSearches()
}

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun setReaderTextSize(size: ReaderTextSize)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
