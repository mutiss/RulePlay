package com.mutissx.ruleplay.domain.model

/** Where a match was found — drives grouping and ranking. */
enum class MatchField(val weight: Int) {
    NAME(100), ALT_NAME(80), CATEGORY(60), RULE_TITLE(50), TERM(40), RULE_TEXT(20),
}

sealed interface SearchResult {
    val score: Int

    data class SportHit(
        val sport: SportSummary,
        val field: MatchField,
        val matchedText: String,
        override val score: Int,
    ) : SearchResult

    data class RuleHit(
        val sport: SportSummary,
        val chapterTitle: String,
        val rule: Rule,
        val field: MatchField,
        /** Short excerpt around the match. */
        val snippet: String,
        override val score: Int,
    ) : SearchResult

    data class TermHit(
        val sport: SportSummary,
        val term: Term,
        override val score: Int,
    ) : SearchResult
}

data class SearchResults(
    val query: String,
    val sports: List<SearchResult.SportHit>,
    val rules: List<SearchResult.RuleHit>,
    val terms: List<SearchResult.TermHit>,
) {
    val isEmpty: Boolean get() = sports.isEmpty() && rules.isEmpty() && terms.isEmpty()

    companion object {
        fun empty(query: String) = SearchResults(query, emptyList(), emptyList(), emptyList())
    }
}
