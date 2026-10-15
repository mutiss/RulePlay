package com.mutissx.ruleplay.di

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mutissx.ruleplay.data.content.BundledSportsRepository
import com.mutissx.ruleplay.data.content.ContentLanguageProvider
import com.mutissx.ruleplay.data.content.ContentSource
import com.mutissx.ruleplay.data.local.RulePlayDatabase
import com.mutissx.ruleplay.data.prefs.DataStoreSettingsRepository
import com.mutissx.ruleplay.data.repository.RoomFavoritesRepository
import com.mutissx.ruleplay.data.repository.RoomHistoryRepository
import com.mutissx.ruleplay.domain.repository.FavoritesRepository
import com.mutissx.ruleplay.domain.repository.HistoryRepository
import com.mutissx.ruleplay.domain.repository.SettingsRepository
import com.mutissx.ruleplay.domain.repository.SportsRepository
import com.mutissx.ruleplay.domain.usecase.DiscoveryUseCase
import com.mutissx.ruleplay.domain.usecase.FilterSportsUseCase
import com.mutissx.ruleplay.domain.usecase.SearchContentUseCase
import com.mutissx.ruleplay.feature.catalog.ExploreViewModel
import com.mutissx.ruleplay.feature.discover.DiscoverViewModel
import com.mutissx.ruleplay.feature.favorites.LibraryViewModel
import com.mutissx.ruleplay.feature.rulereader.RuleReaderViewModel
import com.mutissx.ruleplay.feature.search.SearchViewModel
import com.mutissx.ruleplay.feature.settings.LocaleController
import com.mutissx.ruleplay.feature.settings.SettingsViewModel
import com.mutissx.ruleplay.feature.sportdetail.SportDetailViewModel
import com.mutissx.ruleplay.navigation.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.Locale

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

val contentModule = module {
    // Lives as long as the process; used for hot caches that outlive individual screens.
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<ContentSource> {
        val assets = androidContext().assets
        ContentSource { path ->
            runCatching { assets.open("content/$path").bufferedReader().use { it.readText() } }.getOrNull()
        }
    }
    single<ContentLanguageProvider> {
        ContentLanguageProvider {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            val locale = if (appLocales.isEmpty) Locale.getDefault() else appLocales[0] ?: Locale.getDefault()
            locale.language.ifEmpty { "en" }
        }
    }
    single<SportsRepository> { BundledSportsRepository(get(), get(), Dispatchers.IO) }
}

val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), RulePlayDatabase::class.java, RulePlayDatabase.NAME).build()
    }
    single { get<RulePlayDatabase>().favoritesDao() }
    single { get<RulePlayDatabase>().historyDao() }
    single<FavoritesRepository> { RoomFavoritesRepository(get(), appScope = get()) }
    single<HistoryRepository> { RoomHistoryRepository(get()) }
    single<SettingsRepository> { DataStoreSettingsRepository(androidContext().settingsDataStore) }
    single { LocaleController() }
}

val domainModule = module {
    factory { FilterSportsUseCase() }
    factory { DiscoveryUseCase() }
    factory { SearchContentUseCase(get()) }
}

val viewModelModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::DiscoverViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::SportDetailViewModel)
    viewModelOf(::RuleReaderViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::SettingsViewModel)
}

val appModules = listOf(contentModule, dataModule, domainModule, viewModelModule)
