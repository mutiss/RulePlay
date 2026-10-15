package com.mutissx.ruleplay.domain.usecase

import com.mutissx.ruleplay.domain.model.MatchField
import com.mutissx.ruleplay.testing.TestContent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchContentUseCaseTest {

    private val search = SearchContentUseCase(TestContent.repository())

    @Test
    fun `exact sport name ranks first`() = runTest {
        val results = search("Tennis")
        assertEquals("tennis", results.sports.first().sport.id)
        assertEquals(MatchField.NAME, results.sports.first().field)
    }

    @Test
    fun `alternative names find the sport`() = runTest {
        val results = search("soccer")
        assertEquals("football", results.sports.first().sport.id)
        assertEquals(MatchField.ALT_NAME, results.sports.first().field)
    }

    @Test
    fun `search ignores accents and case`() = runTest {
        assertEquals("padel", search("PÁDEL").sports.first().sport.id)
    }

    @Test
    fun `small typos still find the sport`() = runTest {
        assertTrue(search("kabbadi").sports.any { it.sport.id == "kabaddi" })
    }

    @Test
    fun `rule titles and text are searchable`() = runTest {
        val results = search("offside")
        assertTrue("football offside rule", results.rules.any { it.sport.id == "football" && it.field == MatchField.RULE_TITLE })
        assertTrue("ice hockey offside rule", results.rules.any { it.sport.id == "ice-hockey" })
    }

    @Test
    fun `multi-word queries require every word`() = runTest {
        val results = search("shot clock")
        assertTrue(results.rules.isNotEmpty())
        results.rules.forEach { hit ->
            val text = TextMatching.normalize("${hit.rule.title} ${hit.rule.summary} ${hit.rule.body} ${hit.rule.example.orEmpty()}")
            assertTrue(text.contains("shot") && text.contains("clock"))
        }
    }

    @Test
    fun `glossary terms are searchable`() = runTest {
        assertTrue(search("kitchen").terms.any { it.sport.id == "pickleball" })
    }

    @Test
    fun `category names match sports in that category`() = runTest {
        val results = search("racket sports")
        assertTrue(results.sports.any { it.sport.id == "squash" && it.field == MatchField.CATEGORY })
    }

    @Test
    fun `spanish content is searchable in spanish`() = runTest {
        val spanish = SearchContentUseCase(TestContent.repository("es"))
        val results = spanish("fuera de juego")
        assertTrue(results.rules.any { it.sport.id == "football" })
        assertEquals("football", spanish("futbol").sports.first().sport.id)
    }

    @Test
    fun `blank and nonsense queries return nothing`() = runTest {
        assertTrue(search("   ").isEmpty)
        assertTrue(search("zzqxv").isEmpty)
    }

    @Test
    fun `rule snippets contain the matched word`() = runTest {
        search("tie-break").rules.forEach { hit ->
            assertTrue(hit.snippet, TextMatching.normalize(hit.snippet).contains("tie") || TextMatching.normalize(hit.rule.title).contains("tie"))
        }
    }
}
