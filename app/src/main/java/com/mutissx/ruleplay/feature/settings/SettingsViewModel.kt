package com.mutissx.ruleplay.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.ruleplay.domain.model.AppLanguage
import com.mutissx.ruleplay.domain.model.GoverningBody
import com.mutissx.ruleplay.domain.model.PhotoCredit
import com.mutissx.ruleplay.domain.model.ReaderTextSize
import com.mutissx.ruleplay.domain.model.ThemeMode
import com.mutissx.ruleplay.domain.model.UserSettings
import com.mutissx.ruleplay.domain.repository.SettingsRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Reads and applies the per-app language through AppCompat (works back to API 21). */
class LocaleController {
    fun current(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return AppLanguage.SYSTEM
        val tag = locales[0]?.language
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.SYSTEM
    }

    fun apply(language: AppLanguage) {
        val list = language.tag?.let { LocaleListCompat.forLanguageTags(it) } ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(list)
    }
}

data class SourceEntry(val sportName: String, val body: GoverningBody)

data class PhotoEntry(val sportName: String, val credit: PhotoCredit)

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val language: AppLanguage = AppLanguage.SYSTEM,
    val contentVersion: String = "",
    val sportCount: Int = 0,
    val ruleCount: Int = 0,
    val verifiedLinks: Int = 0,
    val totalLinks: Int = 0,
    val sources: List<SourceEntry> = emptyList(),
    val photos: List<PhotoEntry> = emptyList(),
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val localeController: LocaleController,
    private val sportsRepository: SportsRepository,
) : ViewModel() {

    private val language = MutableStateFlow(localeController.current())

    private val content = flow {
        val catalog = sportsRepository.catalog()
        val sports = sportsRepository.allSports()
        val books = sports.flatMap { it.rulebooks }
        emit(
            SettingsUiState(
                contentVersion = catalog.version,
                sportCount = catalog.sports.size,
                ruleCount = sports.sumOf { it.allRules.size },
                verifiedLinks = books.count { it.isLinkVerified },
                totalLinks = books.size,
                photos = catalog.sports.mapNotNull { s -> s.photoCredit?.let { PhotoEntry(s.name, it) } }.sortedBy { it.sportName },
                sources = sports.flatMap { s -> s.governingBodies.map { SourceEntry(s.name, it) } }
                    .distinctBy { it.body.name }
                    .sortedBy { it.body.acronym ?: it.body.name },
            ),
        )
    }

    val state: StateFlow<SettingsUiState> = combine(settingsRepository.settings, language, content) { settings, lang, content ->
        content.copy(settings = settings, language = lang)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onThemeChange(mode: ThemeMode) { viewModelScope.launch { settingsRepository.setThemeMode(mode) } }
    fun onReduceMotionChange(enabled: Boolean) { viewModelScope.launch { settingsRepository.setReduceMotion(enabled) } }
    fun onReaderSizeChange(size: ReaderTextSize) { viewModelScope.launch { settingsRepository.setReaderTextSize(size) } }
    fun onReplayOnboarding() { viewModelScope.launch { settingsRepository.setOnboardingCompleted(false) } }

    fun onLanguageChange(value: AppLanguage) {
        language.value = value
        localeController.apply(value)
    }
}
