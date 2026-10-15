package com.mutissx.ruleplay.data.content

import kotlinx.serialization.Serializable

/*
 * Wire format of the bundled encyclopedia (assets/content). Kept separate from the domain models so the
 * JSON schema can evolve (new optional fields, translations) without touching the rest of the app.
 */

@Serializable
data class CatalogDto(
    val contentVersion: String,
    val categories: List<CategoryDto>,
    val sports: List<SportEntryDto>,
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String,
    val accent: String,
    val i18n: Map<String, CategoryI18nDto> = emptyMap(),
)

@Serializable
data class CategoryI18nDto(val name: String? = null, val description: String? = null)

@Serializable
data class SportEntryDto(
    val id: String,
    val name: String,
    val altNames: List<String> = emptyList(),
    val category: String,
    val secondary: List<String> = emptyList(),
    val tagline: String,
    val hook: String,
    val origin: String,
    val players: String,
    val popularity: Int,
    val curated: Int,
    val hiddenGem: Boolean = false,
    val art: ArtDto,
    /** Optional open-licensed cover photo in assets/content/photos, with the credit its licence requires. */
    val photo: PhotoDto? = null,
    val related: List<String> = emptyList(),
    val i18n: Map<String, SportEntryI18nDto> = emptyMap(),
)

@Serializable
data class SportEntryI18nDto(
    val name: String? = null,
    val tagline: String? = null,
    val hook: String? = null,
    val players: String? = null,
    val origin: String? = null,
)

@Serializable
data class ArtDto(
    val motif: String,
    val glyph: String,
    val colors: List<String>,
    val seed: Int = 0,
)

@Serializable
data class SportDto(
    val id: String,
    /** Language of this document. Translations live in sports/<lang>/<id>.json. */
    val language: String = "en",
    val status: String = "EDITORIAL_SUMMARY",
    val intro: String,
    val inThirtySeconds: List<String>,
    val objective: String,
    val facts: FactsDto,
    val scoring: List<ScoringDto> = emptyList(),
    val variants: List<VariantDto> = emptyList(),
    val bodies: List<BodyDto> = emptyList(),
    val rulebooks: List<RulebookDto> = emptyList(),
    val chapters: List<ChapterDto>,
    val fouls: List<FoulDto> = emptyList(),
    val flow: List<FlowDto> = emptyList(),
    val positions: List<PositionDto> = emptyList(),
    val field: FieldDto? = null,
    val glossary: List<TermDto> = emptyList(),
    val funFacts: List<String> = emptyList(),
)

@Serializable
data class FactsDto(
    val players: String,
    val duration: String,
    val environment: String,
    val equipment: List<String>,
)

@Serializable
data class ScoringDto(val label: String, val value: String, val detail: String)

@Serializable
data class VariantDto(val id: String, val name: String, val description: String, val body: String? = null)

@Serializable
data class BodyDto(
    val id: String,
    val name: String,
    val acronym: String? = null,
    val scope: String,
    val url: String? = null,
)

@Serializable
data class RulebookDto(
    val id: String,
    val title: String,
    val publisher: String? = null,
    val variant: String? = null,
    val url: String? = null,
    val edition: String? = null,
    val checked: String? = null,
)

@Serializable
data class ChapterDto(
    val id: String,
    val title: String,
    val summary: String,
    val rules: List<RuleDto>,
)

@Serializable
data class RuleDto(
    val id: String,
    val ref: String? = null,
    val title: String,
    val summary: String,
    val body: String,
    val example: String? = null,
    val important: Boolean = false,
    val variants: List<String> = emptyList(),
    val related: List<String> = emptyList(),
)

@Serializable
data class FoulDto(
    val name: String,
    val description: String,
    val consequence: String,
    val severity: String = "MINOR",
)

@Serializable
data class FlowDto(val title: String, val description: String)

@Serializable
data class PositionDto(val name: String, val role: String)

@Serializable
data class FieldDto(
    val name: String,
    val dimensions: String,
    val surface: String,
    val notes: List<String> = emptyList(),
)

@Serializable
data class TermDto(val term: String, val definition: String)

@Serializable
data class PhotoDto(
    val file: String,
    val author: String,
    val license: String,
    val licenseUrl: String? = null,
    val source: String,
)
