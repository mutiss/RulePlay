package com.mutissx.ruleplay.data.content

import com.mutissx.ruleplay.testing.classpathContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Validates the whole bundled encyclopedia: every file parses and every cross-reference resolves.
 * A failure here means a content edit broke the app, so it runs on every build.
 */
class ContentIntegrityTest {

    private val catalog = ContentParser.decodeCatalog(classpathContent.read("catalog.json")!!)
    private val sports = catalog.sports.associate { entry ->
        val text = classpathContent.read("sports/${entry.id}.json")
        assertNotNull("Missing sports/${entry.id}.json", text)
        entry.id to ContentParser.decodeSport(text!!)
    }

    @Test
    fun `catalog has at least forty sports with unique ids`() {
        val ids = catalog.sports.map { it.id }
        assertTrue("Expected 40+ sports, got ${ids.size}", ids.size >= 40)
        assertEquals("Duplicate sport ids", ids.size, ids.toSet().size)
    }

    @Test
    fun `every requested core sport is present`() {
        val required = listOf(
            "football", "basketball", "tennis", "padel", "volleyball", "handball", "rugby-union", "rugby-league",
            "cricket", "baseball", "american-football", "badminton", "table-tennis", "pickleball", "golf", "boxing",
            "judo", "taekwondo", "fencing", "swimming", "athletics", "cycling", "formula-1", "ice-hockey",
            "field-hockey", "water-polo", "surfing", "skateboarding", "curling", "archery",
        )
        val ids = catalog.sports.map { it.id }.toSet()
        required.forEach { assertTrue("Missing $it", it in ids) }
    }

    @Test
    fun `categories and related sports resolve`() {
        val categories = catalog.categories.map { it.id }.toSet()
        val ids = catalog.sports.map { it.id }.toSet()
        catalog.sports.forEach { sport ->
            assertTrue("${sport.id}: unknown category ${sport.category}", sport.category in categories)
            sport.secondary.forEach { assertTrue("${sport.id}: unknown secondary $it", it in categories) }
            sport.related.forEach {
                assertTrue("${sport.id}: unknown related sport $it", it in ids)
                assertTrue("${sport.id}: related to itself", it != sport.id)
            }
        }
        catalog.categories.forEach { category ->
            assertTrue("Empty category ${category.id}", catalog.sports.any { category.id == it.category || category.id in it.secondary })
        }
    }

    @Test
    fun `art specs are valid and unique per sport`() {
        val specs = catalog.sports.map { with(ContentParser) { it.art.toDomain() } }
        // Two sports may share a motif or glyph, but never the full combination of glyph and palette.
        val signatures = specs.map { Triple(it.glyph, it.base, it.mid) }
        assertEquals("Two sports share identical art", signatures.size, signatures.toSet().size)
    }

    @Test
    fun `every sport has a spanish name and tagline`() {
        catalog.sports.forEach { sport ->
            val es = sport.i18n["es"]
            assertNotNull("${sport.id}: missing Spanish translation", es)
            assertTrue("${sport.id}: missing Spanish name", !es!!.name.isNullOrBlank())
            assertTrue("${sport.id}: missing Spanish tagline", !es.tagline.isNullOrBlank())
        }
    }

    @Test
    fun `sport files are complete and internally consistent`() {
        sports.forEach { (id, sport) ->
            assertEquals("File id mismatch", id, sport.id)
            assertTrue("$id: needs at least 2 chapters", sport.chapters.size >= 2)
            assertTrue("$id: needs a 30-second summary", sport.inThirtySeconds.size >= 3)
            assertTrue("$id: needs at least one key rule", sport.chapters.flatMap { it.rules }.any { it.important })
            assertTrue("$id: needs fouls", sport.fouls.isNotEmpty())
            assertTrue("$id: needs a glossary", sport.glossary.size >= 3)
            assertNotNull("$id: needs field information", sport.field)

            val ruleIds = sport.chapters.flatMap { c -> c.rules.map { it.id } }
            assertEquals("$id: duplicate rule ids", ruleIds.size, ruleIds.toSet().size)
            val chapterIds = sport.chapters.map { it.id }
            assertEquals("$id: duplicate chapter ids", chapterIds.size, chapterIds.toSet().size)

            val variantIds = sport.variants.map { it.id }.toSet()
            sport.chapters.flatMap { it.rules }.forEach { rule ->
                rule.related.forEach { assertTrue("$id/${rule.id}: unknown related rule $it", it in ruleIds) }
                rule.variants.forEach { assertTrue("$id/${rule.id}: unknown variant $it", it in variantIds) }
                assertTrue("$id/${rule.id}: empty summary", rule.summary.isNotBlank())
            }
            // Each variant must keep at least one rule.
            sport.variants.forEach { v ->
                assertTrue("$id: variant ${v.id} has no rules", sport.chapters.flatMap { it.rules }.any { it.variants.isEmpty() || v.id in it.variants })
            }
        }
    }

    @Test
    fun `every cover photo is bundled and fully credited`() {
        val openLicence = Regex("""^(CC0|Public domain|CC BY(-SA)? \d\.\d( [A-Z]{2})?)$""")
        catalog.sports.mapNotNull { s -> s.photo?.let { s.id to it } }.forEach { (id, photo) ->
            assertNotNull("$id: missing photos/${photo.file}", javaClass.classLoader!!.getResource("content/photos/${photo.file}"))
            assertTrue("$id: author", photo.author.isNotBlank())
            assertTrue("$id: licence '${photo.license}'", openLicence.matches(photo.license))
            assertTrue("$id: licence link", photo.licenseUrl != null || photo.license == "Public domain")
            assertTrue("$id: source", photo.source.startsWith("https://commons.wikimedia.org/wiki/File:"))
        }
    }

    @Test
    fun `spanish entries mirror the english structure exactly`() {
        sports.forEach { (id, en) ->
            val text = classpathContent.read("sports/es/$id.json")
            assertNotNull("Missing sports/es/$id.json", text)
            val es = ContentParser.decodeSport(text!!)
            assertEquals("$id: language", "es", es.language)
            // Identical ids keep bookmarks and deep links working across languages.
            fun ruleShape(s: SportDto) = s.chapters.map { c -> c.id to c.rules.map { listOf(it.id, it.variants, it.related, it.important, it.ref != null) } }
            assertEquals("$id: chapters/rules", ruleShape(en), ruleShape(es))
            assertEquals("$id: variants", en.variants.map { it.id }, es.variants.map { it.id })
            assertEquals("$id: sources", en.rulebooks.map { Triple(it.id, it.url, it.checked) }, es.rulebooks.map { Triple(it.id, it.url, it.checked) })
            assertEquals("$id: governing bodies", en.bodies.map { it.id to it.url }, es.bodies.map { it.id to it.url })
            assertEquals("$id: foul severities", en.fouls.map { it.severity }, es.fouls.map { it.severity })
        }
    }

    @Test
    fun `sources never claim verification they do not have`() {
        sports.forEach { (id, sport) ->
            val bodyIds = sport.bodies.map { it.id }.toSet()
            sport.rulebooks.forEach { book ->
                book.publisher?.let { assertTrue("$id/${book.id}: unknown publisher $it", it in bodyIds) }
                book.variant?.let { v -> assertTrue("$id/${book.id}: unknown variant $v", sport.variants.any { it.id == v }) }
                book.checked?.let { date ->
                    assertNotNull("$id/${book.id}: a checked date requires a URL", book.url)
                    LocalDate.parse(date) // must be ISO formatted
                }
                book.url?.let { assertTrue("$id/${book.id}: links must be https", it.startsWith("https://")) }
            }
            assertEquals("$id: content must be labelled as an editorial summary", "EDITORIAL_SUMMARY", sport.status)
        }
    }
}
