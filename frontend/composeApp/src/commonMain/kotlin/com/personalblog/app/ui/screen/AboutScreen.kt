package com.personalblog.app.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalblog.app.ui.components.BrandDivider
import com.personalblog.app.ui.components.ContentContainer
import com.personalblog.app.ui.components.ContentWidthStyle
import com.personalblog.app.ui.components.HeroSection

@Composable
fun AboutScreen() {
    ContentContainer(widthStyle = ContentWidthStyle.Page) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HeroSection(modifier = Modifier.padding(top = 16.dp))
            BrandDivider()
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Hi, I'm the author of this blog.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
