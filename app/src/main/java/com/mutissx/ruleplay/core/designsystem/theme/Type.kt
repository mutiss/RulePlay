package com.mutissx.ruleplay.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mutissx.ruleplay.R

/** Condensed editorial display face (Bebas Neue, SIL OFL). Uppercase by design. */
val DisplayFamily = FontFamily(Font(R.font.bebas_neue, FontWeight.Normal))

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: Int) = Font(
    R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Body and UI face (Inter variable, SIL OFL). */
val TextFamily = FontFamily(inter(400), inter(500), inter(600), inter(700), inter(800))

private val trimmed = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)

/** Typographic scale. Display styles are for editorial moments; the rest carry information. */
@Immutable
data class RulePlayTypography(
    val displayHero: TextStyle = TextStyle(fontFamily = DisplayFamily, fontSize = 68.sp, lineHeight = 62.sp, letterSpacing = 0.01.em, lineHeightStyle = trimmed),
    val displayLarge: TextStyle = TextStyle(fontFamily = DisplayFamily, fontSize = 52.sp, lineHeight = 50.sp, letterSpacing = 0.01.em, lineHeightStyle = trimmed),
    val displayMedium: TextStyle = TextStyle(fontFamily = DisplayFamily, fontSize = 38.sp, lineHeight = 38.sp, letterSpacing = 0.015.em, lineHeightStyle = trimmed),
    val displaySmall: TextStyle = TextStyle(fontFamily = DisplayFamily, fontSize = 28.sp, lineHeight = 29.sp, letterSpacing = 0.02.em, lineHeightStyle = trimmed),
    val headline: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.02).em),
    val title: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.01).em),
    val titleSmall: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    val bodyLarge: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 27.sp),
    val body: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    val bodyStrong: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 23.sp),
    val label: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    /** Small uppercase kicker above titles. */
    val overline: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.14.em),
    val caption: TextStyle = TextStyle(fontFamily = TextFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    val numeric: TextStyle = TextStyle(fontFamily = DisplayFamily, fontSize = 34.sp, lineHeight = 34.sp, lineHeightStyle = trimmed),
)
