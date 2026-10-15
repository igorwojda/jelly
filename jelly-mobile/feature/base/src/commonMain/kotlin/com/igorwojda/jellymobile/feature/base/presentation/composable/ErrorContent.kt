package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Full-screen error state with a retry button. */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageContent(
        icon = JellyIcons.CloudOff,
        title = "Something went wrong",
        message = message,
        modifier = modifier,
    ) {
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Preview
@Composable
private fun ErrorContentPreview() {
    JellyTheme {
        ErrorContent(message = "No internet connection", onRetry = {})
    }
}
