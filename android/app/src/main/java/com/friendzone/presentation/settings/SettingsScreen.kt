package com.example.friendzone.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.friendzone.R
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.PrimaryLight
import com.example.friendzone.ui.theme.Surface
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val isSystemDefault = currentLocales.isEmpty
    val currentLanguage = if (isSystemDefault) "" else currentLocales.toLanguageTags()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        CreateEventHeader(
            title = stringResource(R.string.header_settings),
            onBackClick = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.label_language),
                style = MaterialTheme.typography.titleMedium,
                color = TextMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LanguageOption(
                label = "System Default",
                flag = "⚙️",
                selected = isSystemDefault,
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                label = "English",
                flag = "🇺🇸",
                selected = !isSystemDefault && currentLanguage.startsWith("en"),
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                label = "Español",
                flag = "🇪🇸",
                selected = !isSystemDefault && currentLanguage.startsWith("es") && !currentLanguage.contains("AR"),
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                label = "Español (Argentina)",
                flag = "🇦🇷",
                selected = !isSystemDefault && currentLanguage.contains("AR"),
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es-AR"))
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            AppInfoFooter()
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    flag: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PrimaryLight else Surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Primary else BorderGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) Primary else TextMain,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Primary)
            )
        }
    }
}

@Composable
private fun AppInfoFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "FriendZone",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "v1.0.0 (Beta)",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Developed with ❤️ by the FriendZone Team\nUTN-FRBA 2025",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
