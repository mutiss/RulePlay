package com.mutissx.ruleplay.domain.model

import com.mutissx.ruleplay.testing.TestContent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleRetrievalTest {

    private val repository = TestContent.repository()

    @Test
    fun `rules are found by id together with their chapter`() = runTest {
        val football = repository.sport("football")!!
        val offside = football.rule("offside-position")
        assertNotNull(offside)
        assertEquals("Law 11", offside!!.reference)
        assertEquals("offside", football.chapterOf("offside-position")?.id)
    }

    @Test
    fun `unknown sport or rule returns null`() = runTest {
        assertNull(repository.sport("quidditch"))
        assertNull(repository.sport("football")!!.rule("does-not-exist"))
    }

    @Test
    fun `related rule ids point to real rules`() = runTest {
        val football = repository.sport("football")!!
        football.allRules.flatMap { it.relatedRuleIds }.forEach { assertNotNull(it, football.rule(it)) }
    }

    @Test
    fun `sport entries are cached`() = runTest {
        assertTrue(repository.sport("judo") === repository.sport("judo"))
    }

    @Test
    fun `all sports load for the search index`() = runTest {
        assertEquals(repository.catalog().sports.size, repository.allSports().size)
    }
}

class VariantSelectionTest {

    private val repository = TestContent.repository()

    @Test
    fun `nba variant hides fiba-only rules`() = runTest {
        val basketball = repository.sport("basketball")!!
        val nbaRules = basketball.chaptersFor("nba").flatMap { it.rules }.map { it.id }
        assertTrue("periods-nba" in nbaRules)
        assertFalse("periods-fiba" in nbaRules)
        assertTrue("shared rules stay", "shot-clock" in nbaRules)
    }

    @Test
    fun `fiba variant hides nba-only rules`() = runTest {
        val rules = repository.sport("basketball")!!.chaptersFor("fiba").flatMap { it.rules }.map { it.id }
        assertTrue("foul-out-fiba" in rules)
        assertFalse("foul-out-nba" in rules)
    }

    @Test
    fun `chapters left empty by a variant are dropped`() = runTest {
        val tennis = repository.sport("tennis")!!
        assertFalse(tennis.chaptersFor("singles").any { it.id == "doubles" })
        assertTrue(tennis.chaptersFor("doubles").any { it.id == "doubles" })
    }

    @Test
    fun `unknown variant falls back to the first one`() = runTest {
        val basketball = repository.sport("basketball")!!
        assertEquals("fiba", basketball.resolveVariant("wnba"))
        assertEquals("nba", basketball.resolveVariant("nba"))
        assertNull(repository.sport("football")!!.resolveVariant(null))
    }

    @Test
    fun `no variant returns every rule`() = runTest {
        val basketball = repository.sport("basketball")!!
        assertEquals(basketball.allRules.size, basketball.chaptersFor(null).sumOf { it.rules.size })
    }
}

class LanguageFallbackTest {

    @Test
    fun `spanish catalog translates names and keeps english searchable`() = runTest {
        val catalog = TestContent.repository("es").catalog()
        val football = catalog.sport("football")!!
        assertEquals("Fútbol", football.name)
        assertTrue("Football" in football.altNames)
        assertEquals("Deportes de equipo", catalog.category("team")!!.name)
    }

    @Test
    fun `spanish entries load translated rule content`() = runTest {
        val sport = TestContent.repository("es").sport("football")!!
        assertFalse(sport.isFallbackLanguage)
        assertEquals("Regla 11", sport.rule("offside-position")!!.reference)
        assertEquals("Estar en posición de fuera de juego", sport.rule("offside-position")!!.title)
    }

    @Test
    fun `untranslated languages fall back to english and say so`() = runTest {
        val sport = TestContent.repository("fr").sport("football")!!
        assertTrue(sport.isFallbackLanguage)
        assertEquals("Being in an offside position", sport.rule("offside-position")!!.title)
        assertFalse(TestContent.repository("en").sport("football")!!.isFallbackLanguage)
    }
}
