package com.mutissx.ruleplay.data.repository

import com.mutissx.ruleplay.data.local.FavoriteEntity
import com.mutissx.ruleplay.data.local.FavoritesDao
import com.mutissx.ruleplay.data.local.HistoryDao
import com.mutissx.ruleplay.data.local.RecentSearchEntity
import com.mutissx.ruleplay.data.local.RecentSportEntity
import com.mutissx.ruleplay.domain.model.Favorite
import com.mutissx.ruleplay.domain.model.FavoriteType
import com.mutissx.ruleplay.domain.model.favoriteKey
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RoomFavoritesRepository(
    private val dao: FavoritesDao,
    /** When provided, favorites are kept hot so new screens get them on their first frame. */
    appScope: CoroutineScope? = null,
    private val clock: () -> Long = System::currentTimeMillis,
) : FavoritesRepository {

    private val source: Flow<List<Favorite>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    // A screen opened during a transition would otherwise wait for Room's first emission and render a loading
    // state mid-animation. Keeping the latest list in memory makes the first emission synchronous.
    private val hot = appScope?.let { scope -> source.stateIn(scope, SharingStarted.Eagerly, null) }

    override fun observeFavorites(): Flow<List<Favorite>> = hot?.filterNotNull() ?: source

    override fun currentFavorites(): List<Favorite>? = hot?.value

    override suspend fun toggleSport(sportId: String): Boolean = toggle(FavoriteType.SPORT, sportId, null)

    override suspend fun toggleRule(sportId: String, ruleId: String): Boolean =
        toggle(FavoriteType.RULE, sportId, ruleId)

    override suspend fun remove(favorite: Favorite) = dao.delete(favorite.key)

    override suspend fun restore(favorite: Favorite) = dao.insert(favorite.toEntity())

    private suspend fun toggle(type: FavoriteType, sportId: String, ruleId: String?): Boolean {
        val key = favoriteKey(type, sportId, ruleId)
        return if (dao.exists(key)) {
            dao.delete(key)
            false
        } else {
            dao.insert(Favorite(type, sportId, ruleId, clock()).toEntity())
            true
        }
    }

    private fun FavoriteEntity.toDomain() = Favorite(FavoriteType.valueOf(type), sportId, ruleId, savedAt)

    private fun Favorite.toEntity() = FavoriteEntity(key, type.name, sportId, ruleId, savedAt)
}

class RoomHistoryRepository(
    private val dao: HistoryDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : HistoryRepository {

    override fun observeRecentSports(limit: Int): Flow<List<String>> = dao.observeRecentSports(limit)

    override suspend fun recordSportView(sportId: String) {
        dao.upsertSport(RecentSportEntity(sportId, clock()))
        dao.trimSports(KEEP)
    }

    override fun observeRecentSearches(limit: Int): Flow<List<String>> = dao.observeRecentSearches(limit)

    override suspend fun addRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        dao.upsertSearch(RecentSearchEntity(clean, clock()))
        dao.trimSearches(KEEP)
    }

    override suspend fun removeRecentSearch(query: String) = dao.deleteSearch(query)

    override suspend fun clearRecentSearches() = dao.clearSearches()

    private companion object {
        const val KEEP = 30
    }
}
