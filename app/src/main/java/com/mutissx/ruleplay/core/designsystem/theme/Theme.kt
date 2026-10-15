package com.mutissx.ruleplay.core.designsystem.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.mutissx.ruleplay.domain.model.ThemeMode

val LocalRulePlayColors = staticCompositionLocalOf { DarkRulePlayColors }
val LocalRulePlayTypography = staticCompositionLocalOf { RulePlayTypography() }
val LocalRulePlayMotion = staticCompositionLocalOf { RulePlayMotion(reduced = false) }

/**
 * RulePlay's theme. Material 3 is configured underneath (for text fields, ripples and sheets), but
 * screens read the bespoke tokens through [RulePlayTheme.colors], [RulePlayTheme.type] and
 * [RulePlayTheme.motion].
 */
@Composable
fun RulePlayTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colors = if (dark) DarkRulePlayColors else LightRulePlayColors
    val type = remember { RulePlayTypography() }
    val systemReducedMotion = rememberSystemReducedMotion()
    val motion = remember(reduceMotion, systemReducedMotion) { RulePlayMotion(reduced = reduceMotion || systemReducedMotion) }

    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.accent, onPrimary = colors.onAccent,
            secondary = colors.accentText, onSecondary = colors.onAccent,
            background = colors.background, onBackground = colors.textPrimary,
            surface = colors.background, onSurface = colors.textPrimary,
            surfaceVariant = colors.surface, onSurfaceVariant = colors.textSecondary,
            surfaceContainer = colors.surface, surfaceContainerHigh = colors.surfaceRaised,
            surfaceContainerLow = colors.surfaceSunken, surfaceContainerHighest = colors.surfaceRaised,
            outline = colors.outlineStrong, outlineVariant = colors.outline, error = colors.danger,
            inverseSurface = colors.textPrimary, inverseOnSurface = colors.background,
        )
    } else {
        lightColorScheme(
            primary = colors.accentText, onPrimary = Palette.OffWhite,
            secondary = colors.accentText, onSecondary = Palette.OffWhite,
            background = colors.background, onBackground = colors.textPrimary,
            surface = colors.background, onSurface = colors.textPrimary,
            surfaceVariant = colors.surface, onSurfaceVariant = colors.textSecondary,
            surfaceContainer = colors.surface, surfaceContainerHigh = colors.surfaceRaised,
            surfaceContainerLow = colors.surfaceSunken, surfaceContainerHighest = colors.surfaceSunken,
            outline = colors.outlineStrong, outlineVariant = colors.outline, error = colors.danger,
            inverseSurface = colors.textPrimary, inverseOnSurface = colors.background,
        )
    }
    val materialType = remember(type) {
        Typography(
            bodyLarge = type.bodyLarge, bodyMedium = type.body, bodySmall = type.caption,
            titleLarge = type.headline, titleMedium = type.title, titleSmall = type.titleSmall,
            labelLarge = type.label, labelMedium = type.caption, labelSmall = type.caption,
        )
    }

    CompositionLocalProvider(
        LocalRulePlayColors provides colors,
        LocalRulePlayTypography provides type,
        LocalRulePlayMotion provides motion,
    ) {
        MaterialTheme(colorScheme = scheme, typography = materialType, content = content)
    }
}

object RulePlayTheme {
    val colors: RulePlayColors
        @Composable @ReadOnlyComposable get() = LocalRulePlayColors.current
    val type: RulePlayTypography
        @Composable @ReadOnlyComposable get() = LocalRulePlayTypography.current
    val motion: RulePlayMotion
        @Composable @ReadOnlyComposable get() = LocalRulePlayMotion.current
}

/** True when the user disabled animations system-wide (Developer options / Accessibility "Remove animations"). */
@Composable
private fun rememberSystemReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false)
    }
}
