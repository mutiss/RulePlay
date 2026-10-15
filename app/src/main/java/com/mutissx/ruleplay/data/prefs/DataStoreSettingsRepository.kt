package com.mutissx.ruleplay.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mutissx.ruleplay.domain.model.ReaderTextSize
import com.mutissx.ruleplay.domain.model.ThemeMode
import com.mutissx.ruleplay.domain.model.UserSettings
import com.mutissx.ruleplay.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserSettings(
                themeMode = prefs[THEME].toEnum(ThemeMode.SYSTEM),
                reduceMotion = prefs[REDUCE_MOTION] ?: false,
                readerTextSize = prefs[READER_SIZE].toEnum(ReaderTextSize.DEFAULT),
                onboardingCompleted = prefs[ONBOARDING_DONE] ?: false,
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME] = mode.name }
    }

    override suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { it[REDUCE_MOTION] = enabled }
    }

    override suspend fun setReaderTextSize(size: ReaderTextSize) {
        dataStore.edit { it[READER_SIZE] = size.name }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_DONE] = completed }
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val READER_SIZE = stringPreferencesKey("reader_text_size")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
    }
}
