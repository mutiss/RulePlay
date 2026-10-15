package com.mutissx.ruleplay.navigation

import kotlinx.serialization.Serializable

/** Type-safe destinations. */
sealed interface Route {
    @Serializable data object Onboarding : Route

    @Serializable data object Discover : Route

    @Serializable data object Explore : Route

    @Serializable data object Library : Route

    @Serializable data object Settings : Route

    /** Explore pre-filtered to one category, pushed on top of the current tab. */
    @Serializable data class Category(val categoryId: String) : Route

    /** @param origin identifies the card the user tapped, so the artwork can morph into the hero. */
    @Serializable data class SportDetail(val sportId: String, val origin: String = "direct") : Route

    @Serializable data class RuleReader(
        val sportId: String,
        val variantId: String? = null,
        val chapterId: String? = null,
        val ruleId: String? = null,
    ) : Route

    @Serializable data object Search : Route

    @Serializable data object Sources : Route
}
