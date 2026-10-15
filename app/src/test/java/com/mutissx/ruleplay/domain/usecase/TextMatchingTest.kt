package com.mutissx.ruleplay.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextMatchingTest {

    @Test
    fun `normalize folds accents and case without changing length`() {
        val text = "Pádel Ñandú ÉPÉE"
        val normalized = TextMatching.normalize(text)
        assertEquals("padel nandu epee", normalized)
        assertEquals(text.length, normalized.length)
    }

    @Test
    fun `match ranges point at the original text`() {
        val text = "El pádel se juega en pareja"
        val range = TextMatching.matchRanges(text, "padel").single()
        assertEquals("pádel", text.substring(range))
    }

    @Test
    fun `overlapping ranges are merged`() {
        val ranges = TextMatching.matchRanges("offside offside", "off offside")
        assertEquals(listOf(0..6, 8..14), ranges)
    }

    @Test
    fun `phrases highlight as one run and skip short words elsewhere`() {
        val text = "Fuera de juego si puede jugar de nuevo"
        val ranges = TextMatching.matchRanges(text, "fuera de juego")
        assertEquals(listOf("Fuera de juego"), ranges.map { text.substring(it) })
    }

    @Test
    fun `edit distance tolerates small typos`() {
        assertEquals(2, TextMatching.editDistance("kabbadi", "kabaddi"))
        assertTrue(TextMatching.editDistance("tenis", "tennis") <= 1)
        assertTrue(TextMatching.editDistance("football", "curling", 2) > 2)
    }

    @Test
    fun `snippet centres on the match and adds ellipses`() {
        val text = "A".repeat(200) + " offside " + "B".repeat(200)
        val snippet = TextMatching.snippet(text, "offside")
        assertTrue(snippet.contains("offside"))
        assertTrue(snippet.startsWith("…") && snippet.endsWith("…"))
    }
}
