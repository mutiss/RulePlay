package com.mutissx.ruleplay.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.ruleplay.domain.model.UserSettings
import com.mutissx.ruleplay.domain.repository.SettingsRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** App-wide state needed before the first screen renders: theme, motion and onboarding status. */
class AppViewModel(
    settingsRepository: SettingsRepository,
    sportsRepository: SportsRepository,
) : ViewModel() {

    sealed interface AppState {
        data object Loading : AppState
        data class Ready(val settings: UserSettings) : AppState
    }

    val state: StateFlow<AppState> = settingsRepository.settings
        .map<UserSettings, AppState> { AppState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState.Loading)

    init {
        // Warm the catalog while the splash screen is visible so Discover renders instantly, then parse every
        // sport in the background so opening a sport (and the first search) never waits on JSON parsing.
        viewModelScope.launch {
            runCatching {
                sportsRepository.catalog()
                sportsRepository.allSports()
            }
        }
    }
}
