package com.mutissx.ruleplay.domain.usecase

import java.text.Normalizer

/**
 * Accent- and case-insensitive text helpers. Normalisation is done char by char so indices in the
 * normalised string map 1:1 to the original — needed to highlight matches in the original text.
 */
object TextMatching {

    private val cache = HashMap<Char, Char>()

    fun normalize(text: String): String {
        val out = CharArray(text.length)
        for (i in text.indices) out[i] = fold(text[i])
        return String(out)
    }

    private fun fold(c: Char): Char {
        if (c.code < 128) return c.lowercaseChar()
        synchronized(cache) {
            return cache.getOrPut(c) {
                val decomposed = Normalizer.normalize(c.toString(), Normalizer.Form.NFD)
                (decomposed.firstOrNull { Character.getType(it) != Character.NON_SPACING_MARK.toInt() } ?: c)
                    .lowercaseChar()
            }
        }
    }

    fun tokens(query: String): List<String> =
        normalize(query).split(' ', '-', '/', ',', '.').map { it.trim() }.filter { it.isNotEmpty() }

    /** All index ranges in [text] where any query token occurs (accent-insensitive). */
    fun matchRanges(text: String, query: String): List<IntRange> {
        val all = tokens(query)
        // In multi-word queries, short connectives ("de", "of") would light up every sentence.
        val tokens = if (all.size > 1) all.filter { it.length >= 3 } else all.filter { it.length >= 2 || query.trim().length == 1 }
        val normalized = normalize(text)
        val ranges = mutableListOf<IntRange>()
        fun addAll(needle: String, wordStartOnly: Boolean) {
            var from = 0
            while (true) {
                val index = normalized.indexOf(needle, from)
                if (index < 0) break
                if (!wordStartOnly || index == 0 || !normalized[index - 1].isLetterOrDigit()) ranges += index until index + needle.length
                from = index + needle.length
            }
        }
        // The whole phrase first, so "fuera de juego" is emphasised as one run, connective included.
        if (all.size > 1) addAll(all.joinToString(" "), wordStartOnly = true)
        tokens.forEach { addAll(it, wordStartOnly = true) }
        return merge(ranges)
    }

    private fun merge(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return ranges
        val sorted = ranges.sortedBy { it.first }
        val merged = mutableListOf(sorted.first())
        for (r in sorted.drop(1)) {
            val last = merged.last()
            if (r.first <= last.last + 1) merged[merged.lastIndex] = last.first..maxOf(last.last, r.last)
            else merged += r
        }
        return merged
    }

    /** True when every token of [query] occurs in [text]. */
    fun containsAll(text: String, queryTokens: List<String>): Boolean {
        if (queryTokens.isEmpty()) return false
        val normalized = normalize(text)
        return queryTokens.all { normalized.contains(it) }
    }

    /** Small edit distance, used for typo tolerance on sport names ("kabbadi" → "kabaddi"). */
    fun editDistance(a: String, b: String, max: Int = 3): Int {
        if (kotlin.math.abs(a.length - b.length) > max) return max + 1
        var prev = IntArray(b.length + 1) { it }
        var curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            var rowMin = curr[0]
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost)
                rowMin = minOf(rowMin, curr[j])
            }
            if (rowMin > max) return max + 1
            val t = prev; prev = curr; curr = t
        }
        return prev[b.length]
    }

    /** Excerpt of [text] centred around the first match of [token] (normalised). */
    fun snippet(text: String, token: String, before: Int = 48, after: Int = 110): String {
        val index = normalize(text).indexOf(token)
        if (index < 0) return text.take(before + after).let { if (it.length < text.length) "$it…" else it }
        var start = (index - before).coerceAtLeast(0)
        var end = (index + token.length + after).coerceAtMost(text.length)
        if (start > 0) text.lastIndexOf(' ', start).takeIf { it in (start - 15) until start }?.let { start = it + 1 }
        if (end < text.length) text.indexOf(' ', end).takeIf { it in end..(end + 15) }?.let { end = it }
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < text.length) "…" else ""
        return prefix + text.substring(start, end).trim() + suffix
    }
}
