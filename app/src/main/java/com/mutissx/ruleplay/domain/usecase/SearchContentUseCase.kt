package com.mutissx.ruleplay.domain.usecase

import com.mutissx.ruleplay.domain.model.MatchField
import com.mutissx.ruleplay.domain.model.SearchResult
import com.mutissx.ruleplay.domain.model.SearchResults
import com.mutissx.ruleplay.domain.model.Sport
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.repository.Catalog
import com.mutissx.ruleplay.domain.repository.SportsRepository

/**
 * Local full-text search across sport names, alternative names, categories, rule titles, rule text
 * and glossary terms. Accent-insensitive, every query token must match, with light typo tolerance
 * on sport names.
 */
class SearchContentUseCase(private val repository: SportsRepository) {

    suspend operator fun invoke(query: String): SearchResults {
        val clean = query.trim()
        if (clean.isEmpty()) return SearchResults.empty(query)
        return search(clean, repository.catalog(), repository.allSports())
    }

    fun search(query: String, catalog: Catalog, sports: List<Sport>): SearchResults {
        val tokens = TextMatching.tokens(query)
        if (tokens.isEmpty()) return SearchResults.empty(query)
        val normalizedQuery = tokens.joinToString(" ")

        val sportHits = catalog.sports.mapNotNull { matchSport(it, catalog, tokens, normalizedQuery) }
            .sortedWith(compareByDescending<SearchResult.SportHit> { it.score }.thenBy { it.sport.name })

        val ruleHits = mutableListOf<SearchResult.RuleHit>()
        val termHits = mutableListOf<SearchResult.TermHit>()
        for (sport in sports) {
            for (chapter in sport.chapters) {
                for (rule in chapter.rules) {
                    val inTitle = TextMatching.containsAll(rule.title, tokens)
                    val text = "${rule.summary} ${rule.body} ${rule.example.orEmpty()}"
                    val inText = !inTitle && TextMatching.containsAll(text, tokens)
                    if (!inTitle && !inText) continue
                    val field = if (inTitle) MatchField.RULE_TITLE else MatchField.RULE_TEXT
                    val snippetSource = if (TextMatching.normalize(rule.summary).contains(tokens.first())) rule.summary
                    else if (TextMatching.normalize(rule.body).contains(tokens.first())) rule.body
                    else rule.summary
                    ruleHits += SearchResult.RuleHit(
                        sport = sport.summary,
                        chapterTitle = chapter.title,
                        rule = rule,
                        field = field,
                        snippet = TextMatching.snippet(snippetSource, tokens.first()),
                        score = field.weight + (if (rule.important) 8 else 0) + sport.summary.popularity / 20,
                    )
                }
            }
            for (term in sport.glossary) {
                val inTerm = TextMatching.containsAll(term.term, tokens)
                val inDefinition = !inTerm && TextMatching.containsAll(term.definition, tokens)
                if (!inTerm && !inDefinition) continue
                val score = if (inTerm) MatchField.TERM.weight else MatchField.RULE_TEXT.weight
                termHits += SearchResult.TermHit(sport.summary, term, score + sport.summary.popularity / 20)
            }
        }

        return SearchResults(
            query = query,
            sports = sportHits,
            rules = ruleHits.sortedByDescending { it.score }.take(MAX_RULES),
            terms = termHits.sortedByDescending { it.score }.take(MAX_TERMS),
        )
    }

    private fun matchSport(
        sport: SportSummary,
        catalog: Catalog,
        tokens: List<String>,
        normalizedQuery: String,
    ): SearchResult.SportHit? {
        val popularityBonus = sport.popularity / 10
        val name = TextMatching.normalize(sport.name)
        if (TextMatching.containsAll(sport.name, tokens)) {
            val bonus = when {
                name == normalizedQuery -> 40
                name.startsWith(normalizedQuery) -> 20
                else -> 0
            }
            return SearchResult.SportHit(sport, MatchField.NAME, sport.name, MatchField.NAME.weight + bonus + popularityBonus)
        }
        sport.altNames.firstOrNull { TextMatching.containsAll(it, tokens) }?.let { alt ->
            val bonus = if (TextMatching.normalize(alt) == normalizedQuery) 20 else 0
            return SearchResult.SportHit(sport, MatchField.ALT_NAME, alt, MatchField.ALT_NAME.weight + bonus + popularityBonus)
        }
        sport.allCategoryIds.mapNotNull { catalog.category(it) }
            .firstOrNull { TextMatching.containsAll(it.name, tokens) }
            ?.let { category ->
                return SearchResult.SportHit(sport, MatchField.CATEGORY, category.name, MatchField.CATEGORY.weight + popularityBonus)
            }
        // Typo tolerance: compare the whole query against each name/alt name and each of their words.
        if (normalizedQuery.length >= 4) {
            val allowed = if (normalizedQuery.length >= 6) 2 else 1
            val candidates = (listOf(sport.name) + sport.altNames).flatMap { n ->
                val norm = TextMatching.normalize(n)
                listOf(norm) + norm.split(' ', '-')
            }
            if (candidates.any { TextMatching.editDistance(it, normalizedQuery, allowed) <= allowed }) {
                return SearchResult.SportHit(sport, MatchField.NAME, sport.name, MatchField.NAME.weight - 30 + popularityBonus)
            }
        }
        return null
    }

    private companion object {
        const val MAX_RULES = 40
        const val MAX_TERMS = 20
    }
}
