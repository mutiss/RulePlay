package com.mutissx.ruleplay.domain.usecase

import com.mutissx.ruleplay.domain.model.SortOrder
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.Catalog
import kotlin.random.Random

/** Criteria the Explore screen filters the catalog by. */
data class SportFilter(
    val categoryId: String? = null,
    val query: String = "",
    val sort: SortOrder = SortOrder.CURATED,
    val favoritesOnly: Boolean = false,
)

/** Pure filtering/sorting of the sports catalog. */
class FilterSportsUseCase {

    operator fun invoke(
        catalog: Catalog,
        filter: SportFilter,
        favoriteSportIds: Set<String> = emptySet(),
    ): List<SportSummary> {
        val tokens = TextMatching.tokens(filter.query)
        return catalog.sports
            .asSequence()
            .filter { filter.categoryId == null || filter.categoryId in it.allCategoryIds }
            .filter { !filter.favoritesOnly || it.id in favoriteSportIds }
            .filter { sport ->
                tokens.isEmpty() || TextMatching.containsAll(
                    buildString {
                        append(sport.name).append(' ')
                        sport.altNames.forEach { append(it).append(' ') }
                        append(sport.tagline)
                    },
                    tokens,
                )
            }
            .let { seq ->
                when (filter.sort) {
                    SortOrder.CURATED -> seq.sortedBy { it.curatedRank }
                    SortOrder.POPULAR -> seq.sortedWith(compareByDescending<SportSummary> { it.popularity }.thenBy { it.name })
                    SortOrder.ALPHABETICAL -> seq.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                }
            }
            .toList()
    }
}

/**
 * Discovery logic: the daily featured sport, "surprise me" picks and recommendations based on what
 * the user has been reading. All deterministic given inputs so they are easy to test.
 */
class DiscoveryUseCase {

    /** A stable pick for the given day; rotates through the whole catalog. */
    fun sportOfTheDay(catalog: Catalog, epochDay: Long): SportSummary? {
        val pool = catalog.sports.sortedBy { it.curatedRank }
        if (pool.isEmpty()) return null
        // Multiplying by a prime spreads consecutive days across categories.
        val index = Math.floorMod(epochDay * 7919L, pool.size.toLong()).toInt()
        return pool[index]
    }

    /** A random sport, favouring lesser-known ones, never the one currently shown. */
    fun surprise(catalog: Catalog, excludeId: String?, random: Random = Random.Default): SportSummary? {
        val candidates = catalog.sports.filter { it.id != excludeId }
        if (candidates.isEmpty()) return null
        val gems = candidates.filter { it.hiddenGem || it.popularity < 60 }
        val pool = if (gems.isNotEmpty() && random.nextFloat() < 0.75f) gems else candidates
        return pool[random.nextInt(pool.size)]
    }

    /** Sports related to recently viewed ones that the user has not opened yet, most connected first. */
    fun becauseYouViewed(catalog: Catalog, recentIds: List<String>, limit: Int = 6): List<SportSummary> {
        if (recentIds.isEmpty()) return emptyList()
        val seen = recentIds.toSet()
        val scores = LinkedHashMap<String, Int>()
        recentIds.forEachIndexed { index, id ->
            val weight = (recentIds.size - index)
            catalog.sport(id)?.relatedIds?.forEach { related ->
                if (related !in seen) scores[related] = (scores[related] ?: 0) + weight
            }
        }
        return scores.entries
            .sortedByDescending { it.value }
            .mapNotNull { catalog.sport(it.key) }
            .take(limit)
    }

    fun hiddenGems(catalog: Catalog): List<SportSummary> =
        catalog.sports.filter { it.hiddenGem }.sortedBy { it.curatedRank }

    fun popular(catalog: Catalog, limit: Int = 10): List<SportSummary> =
        catalog.sports.sortedByDescending { it.popularity }.take(limit)
}
