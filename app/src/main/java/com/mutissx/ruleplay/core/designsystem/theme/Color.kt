package com.mutissx.ruleplay.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Raw brand palette. Screens use [RulePlayColors] tokens, never these directly. */
object Palette {
    val Ink = Color(0xFF05070D)
    val Midnight = Color(0xFF0A0F1F)
    val Navy900 = Color(0xFF0E1428)
    val Navy800 = Color(0xFF131A33)
    val Navy700 = Color(0xFF1B2342)
    val Navy600 = Color(0xFF2A3358)
    val Navy500 = Color(0xFF3B4570)

    val Lime = Color(0xFFC6FF3D)
    val LimeSoft = Color(0xFFE2FF9E)
    val LimeDeep = Color(0xFF4A7300)

    val OffWhite = Color(0xFFF2F0EA)
    val Mist = Color(0xFFA9AEC6)
    /** Tertiary text on dark surfaces; ≥4.5:1 on Midnight through Navy700 (WCAG AA). */
    val Slate = Color(0xFF8890AE)

    val Paper = Color(0xFFF5F3EE)
    val PaperRaised = Color(0xFFFFFFFF)
    val PaperSunken = Color(0xFFEAE7DF)
    val InkText = Color(0xFF0D1226)
    val InkSecondary = Color(0xFF4A5070)
    /** Tertiary text on light surfaces; ≥4.5:1 on Paper and PaperSunken (WCAG AA). */
    val InkTertiary = Color(0xFF62677F)

    val Coral = Color(0xFFFF6B4A)
    val Danger = Color(0xFFFF5A6E)
    val DangerDeep = Color(0xFFC2183A)
    val Amber = Color(0xFFFFC145)
    val AmberDeep = Color(0xFF8A5A00)
}

/** Semantic colour tokens for RulePlay surfaces and content. */
@Immutable
data class RulePlayColors(
    val isDark: Boolean,
    /** Screen background. */
    val background: Color,
    /** Default card surface. */
    val surface: Color,
    /** Raised surface: sheets, selected cards, bars. */
    val surfaceRaised: Color,
    /** Recessed surface: inputs, tracks. */
    val surfaceSunken: Color,
    val outline: Color,
    val outlineStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    /** Brand accent used for fills (buttons, indicators). */
    val accent: Color,
    /** Content on top of [accent]. */
    val onAccent: Color,
    /** Accent tone that is legible as text/icons on [background]. */
    val accentText: Color,
    val accentSoft: Color,
    val warning: Color,
    val danger: Color,
    /** Always-dark scrim used over artwork regardless of theme. */
    val scrim: Color,
    /** Text placed over artwork (always light). */
    val onArt: Color,
    val onArtSecondary: Color,
)

val DarkRulePlayColors = RulePlayColors(
    isDark = true,
    background = Palette.Midnight,
    surface = Palette.Navy800,
    surfaceRaised = Palette.Navy700,
    surfaceSunken = Palette.Navy900,
    outline = Color(0x1FFFFFFF),
    outlineStrong = Color(0x3DFFFFFF),
    textPrimary = Palette.OffWhite,
    textSecondary = Palette.Mist,
    textTertiary = Palette.Slate,
    accent = Palette.Lime,
    onAccent = Palette.Ink,
    accentText = Palette.Lime,
    accentSoft = Color(0x26C6FF3D),
    warning = Palette.Amber,
    danger = Palette.Danger,
    scrim = Palette.Ink,
    onArt = Palette.OffWhite,
    onArtSecondary = Color(0xCCF2F0EA),
)

val LightRulePlayColors = RulePlayColors(
    isDark = false,
    background = Palette.Paper,
    surface = Palette.PaperRaised,
    surfaceRaised = Palette.PaperRaised,
    surfaceSunken = Palette.PaperSunken,
    outline = Color(0x1A0D1226),
    outlineStrong = Color(0x330D1226),
    textPrimary = Palette.InkText,
    textSecondary = Palette.InkSecondary,
    textTertiary = Palette.InkTertiary,
    accent = Palette.Lime,
    onAccent = Palette.Ink,
    accentText = Palette.LimeDeep,
    accentSoft = Color(0x334A7300),
    warning = Palette.AmberDeep,
    danger = Palette.DangerDeep,
    scrim = Palette.Ink,
    onArt = Palette.OffWhite,
    onArtSecondary = Color(0xCCF2F0EA),
)
