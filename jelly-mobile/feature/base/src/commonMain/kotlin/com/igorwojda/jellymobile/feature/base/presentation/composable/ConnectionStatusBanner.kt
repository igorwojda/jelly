package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.igorwojda.jellymobile.feature.base.domain.model.ConnectionStatus

/**
 * Non-blocking strip telling the user the app works in a degraded mode (backend down or offline). Hidden while
 * [ConnectionStatus.ONLINE]; the screens stay fully usable underneath.
 */
@Composable
fun ConnectionStatusBanner(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    // Keeps the last message while the banner animates out after the connection comes back.
    var degradedStatus by remember { mutableStateOf(status) }
    if (status != ConnectionStatus.ONLINE) degradedStatus = status

    AnimatedVisibility(
        visible = status != ConnectionStatus.ONLINE,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Icon(imageVector = JellyIcons.CloudOff, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = degradedStatus.message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private val ConnectionStatus.message: String
    get() =
        when (this) {
            ConnectionStatus.ONLINE -> ""
            ConnectionStatus.BACKEND_UNAVAILABLE ->
                "Jelly server unavailable. Showing movies from TMDB, favorites will sync later."
            ConnectionStatus.OFFLINE -> "No connection. Showing saved movies and favorites."
        }

@Preview
@Composable
private fun ConnectionStatusBannerPreview() {
    JellyTheme {
        Column {
            ConnectionStatusBanner(status = ConnectionStatus.BACKEND_UNAVAILABLE)
            ConnectionStatusBanner(status = ConnectionStatus.OFFLINE)
        }
    }
}
