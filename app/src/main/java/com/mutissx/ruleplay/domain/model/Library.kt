package com.mutissx.ruleplay.domain.model

enum class FavoriteType { SPORT, RULE }

/** A saved item. For rules, [ruleId] identifies the rule inside [sportId]. */
data class Favorite(
    val type: FavoriteType,
    val sportId: String,
    val ruleId: String?,
    val savedAt: Long,
) {
    val key: String get() = favoriteKey(type, sportId, ruleId)
}

fun favoriteKey(type: FavoriteType, sportId: String, ruleId: String?): String =
    if (type == FavoriteType.SPORT) "sport:$sportId" else "rule:$sportId:$ruleId"

/** A saved rule resolved together with its sport for display. */
data class SavedRule(
    val sport: SportSummary,
    val chapterTitle: String,
    val rule: Rule,
    val savedAt: Long,
)

enum class SortOrder { CURATED, POPULAR, ALPHABETICAL }

enum class ThemeMode { SYSTEM, DARK, LIGHT }

enum class AppLanguage(val tag: String?) { SYSTEM(null), ENGLISH("en"), SPANISH("es") }

enum class ReaderTextSize(val scale: Float) { SMALL(0.9f), DEFAULT(1f), LARGE(1.15f), XLARGE(1.3f) }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
    val readerTextSize: ReaderTextSize = ReaderTextSize.DEFAULT,
    val onboardingCompleted: Boolean = false,
)
