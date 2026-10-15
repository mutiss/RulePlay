package com.mutissx.ruleplay.data.repository

import app.cash.turbine.test
import com.mutissx.ruleplay.data.local.FavoriteEntity
import com.mutissx.ruleplay.data.local.FavoritesDao
import com.mutissx.ruleplay.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeFavoritesDao : FavoritesDao {
    val items = MutableStateFlow<Map<String, FavoriteEntity>>(emptyMap())
    override fun observeAll(): Flow<List<FavoriteEntity>> = items.map { it.values.sortedByDescending { e -> e.savedAt } }
    override suspend fun exists(key: String) = key in items.value
    override suspend fun insert(entity: FavoriteEntity) { items.value = items.value + (entity.key to entity) }
    override suspend fun delete(key: String) { items.value = items.value - key }
}

class FavoritesRepositoryTest {

    private val dao = FakeFavoritesDao()
    private var now = 1_000L
    private val repository = RoomFavoritesRepository(dao, clock = { now++ })

    @Test
    fun `toggling a sport saves then removes it`() = runTest {
        assertTrue(repository.toggleSport("padel"))
        assertEquals(listOf("padel"), repository.observeFavoritesNow().map { it.sportId })
        assertFalse(repository.toggleSport("padel"))
        assertTrue(repository.observeFavoritesNow().isEmpty())
    }

    @Test
    fun `rules and sports are stored independently`() = runTest {
        repository.toggleSport("football")
        repository.toggleRule("football", "offside-position")
        val favorites = repository.observeFavoritesNow()
        assertEquals(2, favorites.size)
        assertEquals(setOf(FavoriteType.SPORT, FavoriteType.RULE), favorites.map { it.type }.toSet())
        assertEquals("offside-position", favorites.first { it.type == FavoriteType.RULE }.ruleId)
    }

    @Test
    fun `same rule id in different sports does not collide`() = runTest {
        repository.toggleRule("tennis", "serve")
        repository.toggleRule("volleyball", "serve")
        assertEquals(2, repository.observeFavoritesNow().size)
    }

    @Test
    fun `newest favorites come first`() = runTest {
        repository.toggleSport("golf")
        repository.toggleSport("judo")
        assertEquals(listOf("judo", "golf"), repository.observeFavoritesNow().map { it.sportId })
    }

    @Test
    fun `removed favorites can be restored for undo`() = runTest {
        repository.toggleSport("curling")
        val saved = repository.observeFavoritesNow().single()
        repository.remove(saved)
        assertTrue(repository.observeFavoritesNow().isEmpty())
        repository.restore(saved)
        assertEquals(saved, repository.observeFavoritesNow().single())
    }

    @Test
    fun `changes are emitted to observers`() = runTest {
        repository.observeFavorites().test {
            assertTrue(awaitItem().isEmpty())
            repository.toggleSport("lacrosse")
            assertEquals("lacrosse", awaitItem().single().sportId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun RoomFavoritesRepository.observeFavoritesNow() = observeFavorites().first()
}
