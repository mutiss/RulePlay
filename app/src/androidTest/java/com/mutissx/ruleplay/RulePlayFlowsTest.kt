package com.mutissx.ruleplay

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end checks of the critical journeys on the real app (real content, Room and DataStore).
 * Strings are resolved from resources so the tests work in any app language.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class RulePlayFlowsTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun str(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    @Before
    fun skipOnboardingIfShown() {
        rule.waitForIdle()
        val skip = rule.onAllNodesWithText(str(R.string.action_skip))
        if (skip.fetchSemanticsNodes().isNotEmpty()) skip.onFirst().performClick()
        rule.waitUntilAtLeastOneExists(hasContentDescription(str(R.string.nav_explore)), 5_000)
    }

    @Test
    fun bottomNavigationSwitchesBetweenTabs() {
        rule.onNodeWithContentDescription(str(R.string.nav_explore)).performClick()
        rule.onNodeWithText(str(R.string.explore_title).uppercase()).assertExists()

        rule.onNodeWithContentDescription(str(R.string.nav_library)).performClick()
        rule.onNodeWithText(str(R.string.library_title).uppercase()).assertExists()

        rule.onNodeWithContentDescription(str(R.string.nav_settings)).performClick()
        rule.onNodeWithText(str(R.string.settings_title).uppercase()).assertExists()
    }

    @Test
    fun searchFindsRulesAcrossSports() {
        rule.onAllNodesWithContentDescription(str(R.string.search_open)).onFirst().performClick()
        rule.waitUntilAtLeastOneExists(hasSetTextAction(), 5_000)
        rule.onNode(hasSetTextAction()).performTextInput("offside")
        rule.waitUntilAtLeastOneExists(hasText("Being in an offside position"), 5_000)
    }

    @Test
    fun exploreFilterOpensSportAndRuleReader() {
        rule.onNodeWithContentDescription(str(R.string.nav_explore)).performClick()
        rule.onNode(hasSetTextAction()).performTextInput("curling")
        rule.waitUntilAtLeastOneExists(hasContentDescription("Curling", substring = true), 5_000)
        rule.onAllNodesWithContentDescription("Curling", substring = true).onFirst().performClick()

        val readerCta = hasText(str(R.string.detail_open_reader))
        rule.waitUntilAtLeastOneExists(hasScrollAction(), 5_000)
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(readerCta)
        rule.onNode(readerCta).performClick()

        rule.waitUntilAtLeastOneExists(hasText(str(R.string.reader_overline).uppercase()), 5_000)
    }

    @Test
    fun savingASportAddsItToTheLibrary() {
        val save = hasContentDescription(str(R.string.action_save_sport, "Kabaddi"))
        val unsave = hasContentDescription(str(R.string.action_unsave_sport, "Kabaddi"))

        rule.onNodeWithContentDescription(str(R.string.nav_explore)).performClick()
        rule.onNode(hasSetTextAction()).performTextInput("kabaddi")
        rule.waitUntilAtLeastOneExists(hasContentDescription("Kabaddi", substring = true), 5_000)
        rule.onAllNodesWithContentDescription("Kabaddi", substring = true).onFirst().performClick()

        rule.waitUntilAtLeastOneExists(save or unsave, 5_000)
        if (rule.onAllNodes(unsave).fetchSemanticsNodes().isEmpty()) rule.onNode(save).performClick()
        rule.waitUntilAtLeastOneExists(unsave, 5_000)

        rule.onNodeWithContentDescription(str(R.string.action_back)).performClick()
        rule.onNodeWithContentDescription(str(R.string.nav_library)).performClick()
        rule.waitUntilAtLeastOneExists(hasText("Kabaddi"), 5_000)

        // Clean up so the test can be re-run: open it from the library and unsave.
        rule.onNodeWithText("Kabaddi").performClick()
        rule.waitUntilAtLeastOneExists(unsave, 5_000)
        rule.onNode(unsave).performClick()
        rule.waitUntilAtLeastOneExists(save, 5_000)
    }

    // --- Accessibility -------------------------------------------------------------------------------------

    private val tabs get() = listOf(R.string.nav_discover, R.string.nav_explore, R.string.nav_library, R.string.nav_settings).map(::str)

    private fun hasPaneTitle(title: String) = SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, title)

    @Test
    fun everyTabAnnouncesItselfAndHasAHeading() {
        for (tab in tabs) {
            rule.onNodeWithContentDescription(tab).performClick()
            rule.waitUntilAtLeastOneExists(hasPaneTitle(tab), 5_000)
            assertTrue("No heading on $tab", rule.onAllNodes(isHeading()).fetchSemanticsNodes().isNotEmpty())
        }
    }

    @Test
    fun everyClickableControlIsLabelledAndAtLeast48dp() {
        val problems = mutableListOf<String>()
        for (tab in tabs) {
            rule.onNodeWithContentDescription(tab).performClick()
            rule.waitUntilAtLeastOneExists(hasPaneTitle(tab), 5_000)
            problems += clickableProblems(tab)
        }

        rule.onNodeWithContentDescription(str(R.string.nav_discover)).performClick()
        rule.onAllNodesWithContentDescription(str(R.string.search_open)).onFirst().performClick()
        rule.waitUntilAtLeastOneExists(hasSetTextAction(), 5_000)
        problems += clickableProblems("Search")
        rule.onNode(hasSetTextAction()).performTextInput("offside")
        rule.waitUntilAtLeastOneExists(hasText("Being in an offside position"), 5_000)
        problems += clickableProblems("Search results")
        rule.onNodeWithContentDescription(str(R.string.action_back)).performClick()

        rule.onNodeWithContentDescription(str(R.string.nav_explore)).performClick()
        rule.onNode(hasSetTextAction()).performTextInput("curling")
        rule.waitUntilAtLeastOneExists(hasContentDescription("Curling", substring = true), 5_000)
        rule.onAllNodesWithContentDescription("Curling", substring = true).onFirst().performClick()
        rule.waitUntilAtLeastOneExists(hasPaneTitle("Curling"), 5_000)
        problems += clickableProblems("Sport detail")
        val readerCta = hasText(str(R.string.detail_open_reader))
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(readerCta)
        problems += clickableProblems("Sport detail (scrolled)")
        rule.onNode(readerCta).performClick()
        rule.waitUntilAtLeastOneExists(hasPaneTitle(str(R.string.reader_title, "Curling")), 5_000)
        problems += clickableProblems("Rule reader")

        assertTrue(problems.distinct().joinToString("\n"), problems.isEmpty())
    }

    /** Clickable nodes on screen with no spoken label or a touch target under 48dp. */
    private fun clickableProblems(screen: String): List<String> {
        rule.waitForIdle()
        val minPx = with(rule.density) { 48.dp.roundToPx() }
        return rule.onAllNodes(hasClickAction()).fetchSemanticsNodes().flatMap { node ->
            val label = listOfNotNull(
                node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(),
                node.config.getOrNull(SemanticsProperties.Text)?.joinToString(),
                node.config.getOrNull(SemanticsProperties.EditableText)?.text,
            ).joinToString().trim()
            listOfNotNull(
                "$screen: unlabelled clickable at ${node.boundsInRoot}".takeIf { label.isEmpty() },
                "$screen: \"$label\" is only ${node.size} px".takeIf { node.size.width < minPx || node.size.height < minPx },
            )
        }
    }
}
