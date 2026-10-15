package com.igorwojda.jellymobile.feature.base.presentation.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/** Material 3 theme that follows the system light / dark setting. */
@Composable
fun JellyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}

@Preview
@Composable
private fun JellyThemePreview() {
    JellyTheme {
        Surface {
            Text(text = "Jelly", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
