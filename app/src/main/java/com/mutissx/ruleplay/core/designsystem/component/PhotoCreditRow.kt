package com.mutissx.ruleplay.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mutissx.ruleplay.R
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.core.designsystem.theme.Spacing
import com.mutissx.ruleplay.domain.model.PhotoCredit

/** Attribution for an open-licensed cover photo; opens the photo's source page, which carries the full licence. */
@Composable
fun PhotoCreditRow(credit: PhotoCredit, label: String, modifier: Modifier = Modifier) {
    val colors = RulePlayTheme.colors
    val type = RulePlayTheme.type
    val uriHandler = LocalUriHandler.current
    Row(
        modifier
            .fillMaxWidth()
            .pressable(onClick = { runCatching { uriHandler.openUri(credit.sourceUrl) } }, pressedScale = 0.99f)
            .padding(vertical = Spacing.m)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceRaised), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.PhotoCamera, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Spacing.m))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.photo_credit_by, credit.author), style = type.titleSmall, color = colors.textPrimary)
            Text("$label · ${credit.license} · Wikimedia Commons", style = type.caption, color = colors.textSecondary)
        }
        Icon(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(R.string.action_open_photo_source), tint = colors.textTertiary, modifier = Modifier.size(18.dp))
    }
}
