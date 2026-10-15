package com.mutissx.ruleplay.domain.model

/** A top-level grouping used for browsing (team sports, racket sports…). */
data class SportCategory(
    val id: String,
    val name: String,
    val description: String,
    /** ARGB accent used for chips, badges and category art. */
    val accent: Long,
)

/**
 * Lightweight catalog entry: everything discovery surfaces need without loading a full rulebook.
 */
data class SportSummary(
    val id: String,
    val name: String,
    val altNames: List<String>,
    val categoryId: String,
    val secondaryCategoryIds: List<String>,
    val tagline: String,
    /** One-line discovery hook, e.g. "A raid on a single breath". */
    val hook: String,
    val origin: String,
    val playersLabel: String,
    val popularity: Int,
    val curatedRank: Int,
    val hiddenGem: Boolean,
    val art: ArtSpec,
    /** Credit for the cover photo, when the cover uses one. */
    val photoCredit: PhotoCredit? = null,
    val relatedIds: List<String>,
) {
    val allCategoryIds: List<String> get() = listOf(categoryId) + secondaryCategoryIds
}

/** Author, licence and source page of an open-licensed photo. */
data class PhotoCredit(
    val author: String,
    val license: String,
    val licenseUrl: String?,
    val sourceUrl: String,
)

/** Description of a sport's original cover art, rendered on-device. */
data class ArtSpec(
    val motif: ArtMotif,
    val glyph: ArtGlyph,
    /** Deep base colour, mid colour and highlight colour (ARGB). */
    val base: Long,
    val mid: Long,
    val highlight: Long,
    val seed: Int,
    /** Asset path of an open-licensed photo used as the cover backdrop; null for fully drawn art. */
    val photo: String? = null,
)

enum class ArtMotif {
    PITCH, COURT, NET_COURT, PADEL_CAGE, DIAMOND, GRIDIRON, OVAL, TABLE, GREEN, RING, MAT, PISTE,
    POOL, TRACK, ROAD, CIRCUIT, RINK, WAVE, PARK, SHEET, RANGE, RIVER, CLOTH, WALL_COURT, FIELD,
}

enum class ArtGlyph {
    FOOTBALL, BASKETBALL, TENNIS_BALL, PADEL_RACKET, VOLLEYBALL, HANDBALL, RUGBY_BALL, CRICKET, BASEBALL,
    AMERICAN_FOOTBALL, SHUTTLECOCK, TABLE_TENNIS, PICKLEBALL, GOLF, BOXING_GLOVE, BELT, OCTAGON, FENCING,
    GOGGLES, HURDLE, WHEEL, CHEQUERED_FLAG, PUCK, HOCKEY_STICK, WATER_POLO_BALL, SURFBOARD, SKATEBOARD,
    CURLING_STONE, TARGET, RAIDER, LACROSSE, RATTAN_BALL, HURLEY, NETBALL_HOOP, DISC, SQUASH, WRESTLING,
    OARS, SNOOKER,
}

/** Full encyclopedia entry for a single sport. */
data class Sport(
    val summary: SportSummary,
    val intro: String,
    val inThirtySeconds: List<String>,
    val objective: String,
    val facts: QuickFacts,
    val scoring: List<ScoringItem>,
    val variants: List<Variant>,
    val governingBodies: List<GoverningBody>,
    val rulebooks: List<Rulebook>,
    val chapters: List<RuleChapter>,
    val fouls: List<Foul>,
    val matchFlow: List<FlowStep>,
    val positions: List<Position>,
    val field: FieldInfo?,
    val glossary: List<Term>,
    val funFacts: List<String>,
    /** Whether the text is an editorial summary or verified official wording. */
    val contentStatus: ContentStatus,
    /** True when shown in English because no translation exists for the active language. */
    val isFallbackLanguage: Boolean = false,
) {
    val id: String get() = summary.id
    val name: String get() = summary.name

    val allRules: List<Rule> get() = chapters.flatMap { it.rules }

    fun rule(ruleId: String): Rule? = allRules.firstOrNull { it.id == ruleId }

    fun chapterOf(ruleId: String): RuleChapter? = chapters.firstOrNull { c -> c.rules.any { it.id == ruleId } }

    /** Chapters restricted to the rules that apply to [variantId]; empty chapters are dropped. */
    fun chaptersFor(variantId: String?): List<RuleChapter> =
        if (variantId == null) chapters
        else chapters.mapNotNull { chapter ->
            val rules = chapter.rules.filter { it.appliesTo(variantId) }
            if (rules.isEmpty()) null else chapter.copy(rules = rules)
        }

    /** Resolves a requested variant, falling back to the first one when unknown. */
    fun resolveVariant(requested: String?): String? =
        variants.firstOrNull { it.id == requested }?.id ?: variants.firstOrNull()?.id
}

data class QuickFacts(
    val players: String,
    val duration: String,
    val environment: String,
    val equipment: List<String>,
)

data class ScoringItem(val label: String, val value: String, val detail: String)

/** A recognised rule set, e.g. FIBA vs NBA basketball. Rules can target specific variants. */
data class Variant(
    val id: String,
    val name: String,
    val description: String,
    val governingBodyId: String?,
)

data class GoverningBody(
    val id: String,
    val name: String,
    val acronym: String?,
    val scope: String,
    val url: String?,
)

data class Rulebook(
    val id: String,
    val title: String,
    val publisherId: String?,
    val variantId: String?,
    val url: String?,
    /** Edition label as published by the organisation — only set when confirmed. */
    val edition: String?,
    /** ISO date on which the link was checked to be reachable. Null when never checked. */
    val linkCheckedOn: String?,
) {
    val isLinkVerified: Boolean get() = url != null && linkCheckedOn != null
}

enum class ContentStatus { EDITORIAL_SUMMARY, OFFICIAL_TEXT }

data class RuleChapter(
    val id: String,
    val title: String,
    val summary: String,
    val rules: List<Rule>,
)

data class Rule(
    val id: String,
    /** Official numbering (e.g. "Law 11") — only present when confirmed against the rulebook. */
    val reference: String?,
    val title: String,
    /** The rule in one or two plain sentences. */
    val summary: String,
    val body: String,
    val example: String?,
    val important: Boolean,
    /** Variant ids this rule applies to; empty means every variant. */
    val variantIds: List<String>,
    val relatedRuleIds: List<String>,
) {
    fun appliesTo(variantId: String?): Boolean =
        variantId == null || variantIds.isEmpty() || variantId in variantIds
}

enum class FoulSeverity { MINOR, MAJOR, SEVERE }

data class Foul(
    val name: String,
    val description: String,
    val consequence: String,
    val severity: FoulSeverity,
)

data class FlowStep(val title: String, val description: String)

data class Position(val name: String, val role: String)

data class FieldInfo(
    val name: String,
    val dimensions: String,
    val surface: String,
    val notes: List<String>,
)

data class Term(val term: String, val definition: String)
