package com.mutissx.ruleplay.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.ruleplay.BuildConfig
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.Panel
import com.mutissx.ruleplay.core.designsystem.component.PhotoCreditRow
import com.mutissx.ruleplay.core.designsystem.component.RulePlayTopBar
import com.mutissx.ruleplay.core.designsystem.component.RulePlayWordmark
import com.mutissx.ruleplay.core.designsystem.component.ScreenTitle
import com.mutissx.ruleplay.core.designsystem.component.StatusBarScrim
import com.mutissx.ruleplay.core.designsystem.component.SegmentedControl
import com.mutissx.ruleplay.core.designsystem.component.pressable
import com.mutissx.ruleplay.core.designsystem.theme.Palette
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.AppLanguage
import com.mutissx.ruleplay.domain.model.ReaderTextSize
import com.mutissx.ruleplay.domain.model.ThemeMode
import com.mutissx.ruleplay.feature.discover.BottomBarSpace
import com.mutissx.ruleplay.navigation.AppNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(navigator: AppNavigator, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onThemeChange = viewModel::onThemeChange,
        onLanguageChange = viewModel::onLanguageChange,
        onReaderSizeChange = viewModel::onReaderSizeChange,
        onReduceMotionChange = viewModel::onReduceMotionChange,
        onReplayOnboarding = viewModel::onReplayOnboarding,
        onOpenSources = navigator::sources,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onReaderSizeChange: (ReaderTextSize) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenSources: () -> Unit,
) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val themes = ThemeMode.entries
    val sizes = ReaderTextSize.entries
    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = BottomBarSpace),
    ) {
        item(key = "title") {
            Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = Spacing.xxl)) {
                ScreenTitle(title = stringResource(R.string.settings_title), overline = stringResource(R.string.settings_overline))
            }
        }

        item(key = "appearance") {
            Group(stringResource(R.string.settings_appearance)) {
                SettingHeader(Icons.Rounded.Palette, stringResource(R.string.settings_theme), stringResource(R.string.settings_theme_body))
                SegmentedControl(
                    options = listOf(stringResource(R.string.theme_system), stringResource(R.string.theme_dark), stringResource(R.string.theme_light)),
                    selectedIndex = themes.indexOf(state.settings.themeMode),
                    onSelect = { onThemeChange(themes[it]) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l).padding(bottom = Spacing.l),
                )
            }
        }

        item(key = "language") {
            Group(stringResource(R.string.settings_language)) {
                SettingHeader(Icons.Rounded.Language, stringResource(R.string.settings_language), stringResource(R.string.settings_language_body))
                AppLanguage.entries.forEach { language ->
                    val label = when (language) {
                        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                        AppLanguage.ENGLISH -> "English"
                        AppLanguage.SPANISH -> "Español"
                    }
                    RadioRow(label, selected = state.language == language, onClick = { onLanguageChange(language) })
                }
                Spacer(Modifier.height(Spacing.s))
            }
        }

        item(key = "reading") {
            Group(stringResource(R.string.settings_reading)) {
                SettingHeader(Icons.Rounded.TextFields, stringResource(R.string.settings_reader_size), stringResource(R.string.settings_reader_size_body))
                SegmentedControl(
                    options = listOf("S", "M", "L", "XL"),
                    selectedIndex = sizes.indexOf(state.settings.readerTextSize),
                    onSelect = { onReaderSizeChange(sizes[it]) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l).padding(bottom = Spacing.l),
                )
            }
        }

        item(key = "accessibility") {
            Group(stringResource(R.string.settings_accessibility)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(state.settings.reduceMotion, role = Role.Switch, onValueChange = onReduceMotionChange)
                        .padding(Spacing.l),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBadge(Icons.Rounded.Animation)
                    Spacer(Modifier.width(Spacing.l))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_reduce_motion), style = type.titleSmall, color = colors.textPrimary)
                        Text(stringResource(R.string.settings_reduce_motion_body), style = type.caption, color = colors.textSecondary)
                    }
                    Spacer(Modifier.width(Spacing.m))
                    Switch(
                        checked = state.settings.reduceMotion,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Palette.Ink, checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary, uncheckedTrackColor = colors.surfaceSunken,
                            uncheckedBorderColor = colors.outlineStrong,
                        ),
                    )
                }
            }
        }

        item(key = "about") {
            Group(stringResource(R.string.settings_about)) {
                Column(Modifier.padding(Spacing.l)) {
                    RulePlayWordmark()
                    Spacer(Modifier.height(Spacing.m))
                    Text(stringResource(R.string.settings_about_body), style = type.body, color = colors.textSecondary)
                }
                NavRow(Icons.Rounded.AccountBalance, stringResource(R.string.settings_sources), stringResource(R.string.settings_sources_body), onOpenSources)
                NavRow(Icons.Rounded.Replay, stringResource(R.string.settings_replay_onboarding), stringResource(R.string.settings_replay_onboarding_body), onReplayOnboarding)
                InfoRow(Icons.Rounded.Inventory2, stringResource(R.string.settings_content_version), state.contentVersion)
                InfoRow(Icons.Rounded.Verified, stringResource(R.string.settings_catalog), stringResource(R.string.settings_catalog_value, state.sportCount, state.ruleCount))
                InfoRow(Icons.Rounded.Info, stringResource(R.string.settings_app_version), BuildConfig.VERSION_NAME)
            }
        }
    }
    StatusBarScrim()
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    val colors = RulePlayTheme.colors
    Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xl)) {
        Text(title.uppercase(), style = RulePlayTheme.type.overline, color = colors.textTertiary, modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.s).semantics { heading() })
        Panel { Column { content() } }
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    val colors = RulePlayTheme.colors
    Box(Modifier.size(40.dp).clip(CircleShape).background(colors.accentSoft), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = colors.accentText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingHeader(icon: ImageVector, title: String, body: String) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon)
        Spacer(Modifier.width(Spacing.l))
        Column {
            Text(title, style = type.titleSmall, color = colors.textPrimary)
            Text(body, style = type.caption, color = colors.textSecondary)
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = RulePlayTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(56.dp))
        Text(label, style = RulePlayTheme.type.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
        if (selected) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(colors.accent), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, body: String, onClick: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(
        Modifier.fillMaxWidth().pressable(onClick = onClick, pressedScale = 0.99f).padding(Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon)
        Spacer(Modifier.width(Spacing.l))
        Column(Modifier.weight(1f)) {
            Text(title, style = type.titleSmall, color = colors.textPrimary)
            Text(body, style = type.caption, color = colors.textSecondary)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, value: String) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    Row(Modifier.fillMaxWidth().padding(Spacing.l).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon)
        Spacer(Modifier.width(Spacing.l))
        Text(title, style = type.titleSmall, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = type.body, color = colors.textSecondary)
    }
}

@Composable
fun SourcesRoute(navigator: AppNavigator, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SourcesScreen(state, onBack = navigator::back)
}

@Composable
fun SourcesScreen(state: SettingsUiState, onBack: () -> Unit) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val uriHandler = LocalUriHandler.current
    Box(Modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 120.dp)) {
            item(key = "intro") {
                Column(Modifier.padding(horizontal = Spacing.gutter)) {
                    Text(stringResource(R.string.sources_title).uppercase(), style = type.displayMedium, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
                    Spacer(Modifier.height(Spacing.l))
                    Text(stringResource(R.string.sources_policy), style = type.bodyLarge, color = colors.textPrimary)
                    Spacer(Modifier.height(Spacing.m))
                    Text(stringResource(R.string.sources_links_checked, state.verifiedLinks, state.totalLinks), style = type.body, color = colors.textSecondary)
                }
            }
            item(key = "bodies-header") {
                Text(
                    stringResource(R.string.sources_governing_bodies).uppercase(), style = type.overline, color = colors.accentText,
                    modifier = Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xxl, bottom = Spacing.s).semantics { heading() },
                )
            }
            items(state.sources, key = { it.body.name }) { entry ->
                val body = entry.body
                Row(
                    Modifier
                        .fillMaxWidth()
                        .then(if (body.url != null) Modifier.pressable(onClick = { runCatching { uriHandler.openUri(body.url) } }, pressedScale = 0.99f) else Modifier)
                        .padding(horizontal = Spacing.gutter, vertical = Spacing.m)
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(body.acronym?.let { "$it — ${body.name}" } ?: body.name, style = type.titleSmall, color = colors.textPrimary)
                        Text("${entry.sportName} · ${body.scope}", style = type.caption, color = colors.textSecondary)
                    }
                    if (body.url != null) Icon(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(R.string.action_open_website), tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                }
            }
            if (state.photos.isNotEmpty()) {
                item(key = "photos-header") {
                    Text(
                        stringResource(R.string.sources_photos).uppercase(), style = type.overline, color = colors.accentText,
                        modifier = Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xxl, bottom = Spacing.s).semantics { heading() },
                    )
                }
                items(state.photos, key = { "photo-" + it.sportName }) { entry ->
                    PhotoCreditRow(entry.credit, label = entry.sportName, modifier = Modifier.padding(horizontal = Spacing.gutter))
                }
            }
            item(key = "ack") {
                Column(Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.xxl)) {
                    Text(stringResource(R.string.sources_acknowledgements).uppercase(), style = type.overline, color = colors.accentText, modifier = Modifier.semantics { heading() })
                    Spacer(Modifier.height(Spacing.m))
                    Text(stringResource(R.string.sources_fonts), style = type.body, color = colors.textPrimary)
                    Spacer(Modifier.height(Spacing.m))
                    Text(stringResource(R.string.sources_art), style = type.body, color = colors.textPrimary)
                    Spacer(Modifier.height(Spacing.m))
                    Text(stringResource(R.string.sources_trademarks), style = type.body, color = colors.textSecondary)
                    Spacer(Modifier.height(Spacing.xxxl))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
        RulePlayTopBar(title = stringResource(R.string.sources_title), onBack = onBack)
    }
}
