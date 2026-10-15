package com.mutissx.ruleplay

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.domain.model.ThemeMode
import com.mutissx.ruleplay.navigation.AppViewModel
import com.mutissx.ruleplay.navigation.IntroOverlay
import com.mutissx.ruleplay.navigation.RulePlayNavHost
import org.koin.androidx.viewmodel.ext.android.viewModel

// AppCompatActivity is required for per-app language switching on Android 12L and below.
class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { appViewModel.state.value is AppViewModel.AppState.Loading }

        setContent {
            val state by appViewModel.state.collectAsStateWithLifecycle()
            val ready = state as? AppViewModel.AppState.Ready ?: return@setContent
            val settings = ready.settings
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            LaunchedEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                    navigationBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                )
            }
            // The onboarding decision is taken once per launch so finishing it doesn't rebuild the graph.
            val showOnboarding = rememberSaveable { !settings.onboardingCompleted }
            var introDone by rememberSaveable { mutableStateOf(savedInstanceState != null) }

            RulePlayTheme(themeMode = settings.themeMode, reduceMotion = settings.reduceMotion) {
                val navController = rememberNavController()
                RulePlayNavHost(navController = navController, showOnboarding = showOnboarding)
                if (!introDone) IntroOverlay(onFinished = { introDone = true })
            }
        }
    }
}
