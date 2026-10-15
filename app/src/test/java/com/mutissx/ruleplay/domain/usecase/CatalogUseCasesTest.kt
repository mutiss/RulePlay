package com.mutissx.ruleplay.domain.usecase

import com.mutissx.ruleplay.domain.model.SortOrder
import com.mutissx.ruleplay.domain.repository.Catalog
import com.mutissx.ruleplay.testing.TestContent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FilterSportsUseCaseTest {

    private val catalog: Catalog = runBlocking { TestContent.repository().catalog() }
    private val filter = FilterSportsUseCase()

    @Test
    fun `no filter returns the whole catalog in curated order`() {
        val result = filter(catalog, SportFilter())
        assertEquals(catalog.sports.size, result.size)
        assertEquals(result.sortedBy { it.curatedRank }, result)
    }

    @Test
    fun `category filter includes secondary categories`() {
        val winter = filter(catalog, SportFilter(categoryId = "winter")).map { it.id }
        assertTrue("curling (primary)", "curling" in winter)
        assertTrue("ice hockey (secondary)", "ice-hockey" in winter)
        assertFalse("football", "football" in winter)
    }

    @Test
    fun `query matches alternative names`() {
        val result = filter(catalog, SportFilter(query = "ping-pong")).map { it.id }
        assertEquals(listOf("table-tennis"), result)
    }

    @Test
    fun `favorites only keeps saved sports`() {
        val result = filter(catalog, SportFilter(favoritesOnly = true), setOf("golf", "judo"))
        assertEquals(setOf("golf", "judo"), result.map { it.id }.toSet())
    }

    @Test
    fun `alphabetical sort is case-insensitive`() {
        val names = filter(catalog, SportFilter(sort = SortOrder.ALPHABETICAL)).map { it.name }
        assertEquals(names.sortedWith(String.CASE_INSENSITIVE_ORDER), names)
    }

    @Test
    fun `popular sort puts the most popular first`() {
        val result = filter(catalog, SportFilter(sort = SortOrder.POPULAR))
        assertTrue(result.zipWithNext().all { (a, b) -> a.popularity >= b.popularity })
    }

    @Test
    fun `filters combine`() {
        val result = filter(catalog, SportFilter(categoryId = "racket", query = "tennis"))
        // Padel matches through its alternative name "Paddle tennis".
        assertEquals(setOf("tennis", "table-tennis", "padel"), result.map { it.id }.toSet())
    }
}

class DiscoveryUseCaseTest {

    private val catalog: Catalog = runBlocking { TestContent.repository().catalog() }
    private val discovery = DiscoveryUseCase()

    @Test
    fun `sport of the day is stable for a day and changes the next`() {
        val today = discovery.sportOfTheDay(catalog, 20_000)
        assertEquals(today, discovery.sportOfTheDay(catalog, 20_000))
        assertNotEquals(today, discovery.sportOfTheDay(catalog, 20_001))
    }

    @Test
    fun `sport of the day rotates through the catalog`() {
        val picks = (0L until catalog.sports.size.toLong()).mapNotNull { discovery.sportOfTheDay(catalog, it)?.id }.toSet()
        assertEquals(catalog.sports.size, picks.size)
    }

    @Test
    fun `surprise never repeats the current sport`() {
        val random = Random(42)
        repeat(50) { assertNotEquals("curling", discovery.surprise(catalog, "curling", random)?.id) }
    }

    @Test
    fun `recommendations exclude sports already viewed`() {
        val recs = discovery.becauseYouViewed(catalog, listOf("padel", "tennis")).map { it.id }
        assertTrue(recs.isNotEmpty())
        assertFalse("padel" in recs || "tennis" in recs)
        assertTrue("squash is related to both", "squash" in recs)
    }

    @Test
    fun `hidden gems are all flagged`() {
        assertTrue(discovery.hiddenGems(catalog).all { it.hiddenGem })
        assertTrue(discovery.hiddenGems(catalog).any { it.id == "kabaddi" })
    }
}
