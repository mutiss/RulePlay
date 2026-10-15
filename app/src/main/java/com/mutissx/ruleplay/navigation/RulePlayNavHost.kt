package com.mutissx.ruleplay.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.component.LocalNavAnimatedVisibilityScope
import com.mutissx.ruleplay.core.designsystem.component.LocalSharedTransitionScope
import com.mutissx.ruleplay.core.designsystem.component.NavItem
import com.mutissx.ruleplay.core.designsystem.component.RulePlayBottomBar
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.feature.catalog.ExploreRoute
import com.mutissx.ruleplay.feature.discover.DiscoverRoute
import com.mutissx.ruleplay.feature.favorites.LibraryRoute
import com.mutissx.ruleplay.feature.onboarding.OnboardingRoute
import com.mutissx.ruleplay.feature.rulereader.RuleReaderRoute
import com.mutissx.ruleplay.feature.search.SearchRoute
import com.mutissx.ruleplay.feature.settings.SettingsRoute
import com.mutissx.ruleplay.feature.settings.SourcesRoute
import com.mutissx.ruleplay.feature.sportdetail.SportDetailRoute

private val topLevel = listOf(Route.Discover, Route.Explore, Route.Library, Route.Settings)

/** Navigation actions shared by every screen. */
class AppNavigator(private val nav: NavHostController) {
    fun back() { nav.popBackStack() }
    fun sport(id: String, origin: String = "direct") = nav.navigate(Route.SportDetail(id, origin))
    fun reader(sportId: String, variantId: String? = null, chapterId: String? = null, ruleId: String? = null) =
        nav.navigate(Route.RuleReader(sportId, variantId, chapterId, ruleId))
    fun category(id: String) = nav.navigate(Route.Category(id))
    fun search() = nav.navigate(Route.Search) { launchSingleTop = true }
    fun sources() = nav.navigate(Route.Sources)
    fun library() = tab(Route.Library)
    fun explore() = tab(Route.Explore)

    fun tab(route: Route) = nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    fun finishOnboarding() = nav.navigate(Route.Discover) {
        popUpTo(Route.Onboarding) { inclusive = true }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RulePlayNavHost(
    navController: NavHostController,
    showOnboarding: Boolean,
    modifier: Modifier = Modifier,
) {
    val navigator = remember(navController) { AppNavigator(navController) }
    val motion = RulePlayTheme.motion
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val selectedTab = topLevel.indexOfFirst { route -> destination?.hierarchy?.any { it.hasRoute(route::class) } == true }

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (motion.reduced) EnterTransition.None
        else fadeIn(tween(320, delayMillis = 60)) + scaleIn(tween(380), initialScale = 0.97f)
    }
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (motion.reduced) ExitTransition.None else fadeOut(tween(200))
    }
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (motion.reduced) EnterTransition.None else fadeIn(tween(300))
    }
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (motion.reduced) ExitTransition.None else fadeOut(tween(220)) + scaleOut(tween(300), targetScale = 0.97f)
    }

    Box(modifier.fillMaxSize().background(RulePlayTheme.colors.background)) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = if (showOnboarding) Route.Onboarding else Route.Discover,
                    enterTransition = enter,
                    exitTransition = exit,
                    popEnterTransition = popEnter,
                    popExitTransition = popExit,
                ) {
                    composable<Route.Onboarding> {
                        Scoped { OnboardingRoute(onFinished = navigator::finishOnboarding) }
                    }
                    composable<Route.Discover> {
                        Scoped { DiscoverRoute(navigator) }
                    }
                    composable<Route.Explore> {
                        Scoped { ExploreRoute(navigator, categoryId = null, showBack = false) }
                    }
                    composable<Route.Category> { entry ->
                        val categoryId = entry.toRoute<Route.Category>().categoryId
                        Scoped { ExploreRoute(navigator, categoryId = categoryId, showBack = true) }
                    }
                    composable<Route.Library> {
                        Scoped { LibraryRoute(navigator) }
                    }
                    composable<Route.Settings> {
                        Scoped { SettingsRoute(navigator) }
                    }
                    composable<Route.SportDetail> {
                        Scoped { SportDetailRoute(navigator) }
                    }
                    composable<Route.RuleReader> {
                        Scoped { RuleReaderRoute(navigator) }
                    }
                    composable<Route.Search> {
                        Scoped { SearchRoute(navigator) }
                    }
                    composable<Route.Sources> {
                        Scoped { SourcesRoute(navigator) }
                    }
                }
            }
        }

        val items = listOf(
            NavItem(stringResource(R.string.nav_discover), Icons.Outlined.SportsSoccer, Icons.Rounded.SportsSoccer),
            NavItem(stringResource(R.string.nav_explore), Icons.Outlined.Explore, Icons.Rounded.Explore),
            NavItem(stringResource(R.string.nav_library), Icons.Outlined.AutoStories, Icons.Rounded.AutoStories),
            NavItem(stringResource(R.string.nav_settings), Icons.Outlined.Settings, Icons.Rounded.Settings),
        )
        AnimatedVisibility(
            visible = selectedTab >= 0,
            enter = if (motion.reduced) EnterTransition.None else slideInVertically(tween(320)) { it } + fadeIn(),
            exit = if (motion.reduced) ExitTransition.None else slideOutVertically(tween(260)) { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            RulePlayBottomBar(
                items = items,
                selectedIndex = selectedTab,
                onSelect = { index -> navigator.tab(topLevel[index]) },
            )
        }
    }
}

/** Exposes the destination's animated scope to shared-element aware components. */
@Composable
private fun AnimatedContentScope.Scoped(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
}
