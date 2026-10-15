package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.domain.usecase.TextMatching

/** Builds [text] with every (accent-insensitive) occurrence of the query's words emphasised. */
fun highlightMatches(text: String, query: String, style: SpanStyle): AnnotatedString {
    val ranges = TextMatching.matchRanges(text, query)
    if (ranges.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        ranges.forEach { addStyle(style, it.first, it.last + 1) }
    }
}

@Composable
fun rememberHighlighted(text: String, query: String): AnnotatedString {
    val colors = RulePlayTheme.colors
    return remember(text, query, colors) {
        highlightMatches(
            text, query,
            SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold, background = colors.accentSoft),
        )
    }
}
