package com.mutissx.ruleplay.data.content

import com.mutissx.ruleplay.domain.model.ArtGlyph
import com.mutissx.ruleplay.domain.model.ArtMotif
import com.mutissx.ruleplay.domain.model.ArtSpec
import com.mutissx.ruleplay.domain.model.ContentStatus
import com.mutissx.ruleplay.domain.model.FieldInfo
import com.mutissx.ruleplay.domain.model.FlowStep
import com.mutissx.ruleplay.domain.model.Foul
import com.mutissx.ruleplay.domain.model.FoulSeverity
import com.mutissx.ruleplay.domain.model.GoverningBody
import com.mutissx.ruleplay.domain.model.PhotoCredit
import com.mutissx.ruleplay.domain.model.Position
import com.mutissx.ruleplay.domain.model.QuickFacts
import com.mutissx.ruleplay.domain.model.Rule
import com.mutissx.ruleplay.domain.model.RuleChapter
import com.mutissx.ruleplay.domain.model.Rulebook
import com.mutissx.ruleplay.domain.model.ScoringItem
import com.mutissx.ruleplay.domain.model.Sport
import com.mutissx.ruleplay.domain.model.SportCategory
import com.mutissx.ruleplay.domain.model.SportSummary
import com.mutissx.ruleplay.domain.model.Term
import com.mutissx.ruleplay.domain.model.Variant
import com.mutissx.ruleplay.domain.repository.Catalog
import kotlinx.serialization.json.Json

/** Decodes bundled JSON and maps it to domain models, applying translations where available. */
object ContentParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    /** Photos are bundled as Android assets next to the content JSON. */
    const val PHOTO_DIR = "content/photos/"

    fun decodeCatalog(text: String): CatalogDto = json.decodeFromString(CatalogDto.serializer(), text)

    fun decodeSport(text: String): SportDto = json.decodeFromString(SportDto.serializer(), text)

    fun parseCatalog(text: String, language: String): Catalog = decodeCatalog(text).toDomain(language)

    fun CatalogDto.toDomain(language: String): Catalog = Catalog(
        version = contentVersion,
        categories = categories.map { it.toDomain(language) },
        sports = sports.map { it.toDomain(language) },
    )

    fun CategoryDto.toDomain(language: String): SportCategory {
        val t = i18n[language]
        return SportCategory(
            id = id,
            name = t?.name ?: name,
            description = t?.description ?: description,
            accent = parseColor(accent),
        )
    }

    fun SportEntryDto.toDomain(language: String): SportSummary {
        val t = i18n[language]
        return SportSummary(
            id = id,
            name = t?.name ?: name,
            // The English name stays searchable when the UI is translated.
            altNames = (altNames + if (t?.name != null && t.name != name) listOf(name) else emptyList()).distinct(),
            categoryId = category,
            secondaryCategoryIds = secondary,
            tagline = t?.tagline ?: tagline,
            hook = t?.hook ?: hook,
            origin = t?.origin ?: origin,
            playersLabel = t?.players ?: players,
            popularity = popularity,
            curatedRank = curated,
            hiddenGem = hiddenGem,
            art = art.toDomain().copy(photo = photo?.let { PHOTO_DIR + it.file }),
            photoCredit = photo?.let { PhotoCredit(it.author, it.license, it.licenseUrl, it.source) },
            relatedIds = related,
        )
    }

    fun ArtDto.toDomain(): ArtSpec {
        require(colors.size >= 3) { "Art needs three colours" }
        return ArtSpec(
            motif = ArtMotif.valueOf(motif),
            glyph = ArtGlyph.valueOf(glyph),
            base = parseColor(colors[0]),
            mid = parseColor(colors[1]),
            highlight = parseColor(colors[2]),
            seed = seed,
        )
    }

    fun SportDto.toDomain(summary: SportSummary, isFallback: Boolean): Sport = Sport(
        summary = summary,
        intro = intro,
        inThirtySeconds = inThirtySeconds,
        objective = objective,
        facts = QuickFacts(facts.players, facts.duration, facts.environment, facts.equipment),
        scoring = scoring.map { ScoringItem(it.label, it.value, it.detail) },
        variants = variants.map { v ->
            Variant(v.id, v.name, v.description, governingBodyId = v.body)
        },
        governingBodies = bodies.map { GoverningBody(it.id, it.name, it.acronym, it.scope, it.url) },
        rulebooks = rulebooks.map {
            Rulebook(it.id, it.title, it.publisher, it.variant, it.url, it.edition, it.checked)
        },
        chapters = chapters.map { c ->
            RuleChapter(
                id = c.id,
                title = c.title,
                summary = c.summary,
                rules = c.rules.map { r ->
                    Rule(
                        id = r.id,
                        reference = r.ref,
                        title = r.title,
                        summary = r.summary,
                        body = r.body,
                        example = r.example,
                        important = r.important,
                        variantIds = r.variants,
                        relatedRuleIds = r.related,
                    )
                },
            )
        },
        fouls = fouls.map { Foul(it.name, it.description, it.consequence, FoulSeverity.valueOf(it.severity)) },
        matchFlow = flow.map { FlowStep(it.title, it.description) },
        positions = positions.map { Position(it.name, it.role) },
        field = field?.let { FieldInfo(it.name, it.dimensions, it.surface, it.notes) },
        glossary = glossary.map { Term(it.term, it.definition) },
        funFacts = funFacts,
        contentStatus = ContentStatus.valueOf(status),
        isFallbackLanguage = isFallback,
    )

    /** Parses `#RRGGBB` or `#AARRGGBB` into an ARGB long. */
    fun parseColor(hex: String): Long {
        val clean = hex.removePrefix("#")
        return when (clean.length) {
            6 -> 0xFF000000L or clean.toLong(16)
            8 -> clean.toLong(16)
            else -> throw IllegalArgumentException("Invalid colour $hex")
        }
    }
}
