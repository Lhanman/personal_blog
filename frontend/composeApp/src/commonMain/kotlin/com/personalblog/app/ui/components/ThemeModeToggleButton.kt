package com.personalblog.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalblog.app.ui.theme.ThemeMode

@Composable
fun ThemeModeToggleButton(
    currentMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        listOf(ThemeMode.LIGHT to "☀", ThemeMode.DARK to "☾", ThemeMode.SYSTEM to "⊙").forEach { (mode, icon) ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (currentMode == mode)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable { onThemeModeSelected(mode) }
                    .padding(2.dp)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (currentMode == mode)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
