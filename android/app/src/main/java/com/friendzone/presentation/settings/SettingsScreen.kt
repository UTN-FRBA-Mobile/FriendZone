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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.friendzone.R
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzPrimaryLight
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FzBackground)
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
                color = FzTextMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LanguageOption(
                label = "English",
                flag = "🇺🇸",
                selected = currentLocale.startsWith("en") || currentLocale.isEmpty(),
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                label = "Español",
                flag = "🇪🇸",
                selected = currentLocale.startsWith("es") && !currentLocale.contains("AR"),
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                label = "Español (Argentina)",
                flag = "🇦🇷",
                selected = currentLocale.contains("AR"),
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
            .background(if (selected) FzPrimaryLight else FzSurface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) FzPrimary else FzBorderGray,
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
            color = if (selected) FzPrimary else FzTextMain,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(FzPrimary)
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
            color = FzTextSecondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "v1.0.0 (Beta)",
            style = MaterialTheme.typography.bodySmall,
            color = FzTextSecondary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Developed with ❤️ by the FriendZone Team\nUTN-FRBA 2025",
            style = MaterialTheme.typography.bodySmall,
            color = FzTextSecondary.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
