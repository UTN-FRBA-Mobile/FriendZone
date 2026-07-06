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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.ErrorColor
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.PrimaryLight
import com.example.friendzone.ui.theme.Surface
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val isSystemDefault = currentLocales.isEmpty
    val currentLanguage = if (isSystemDefault) "" else currentLocales.toLanguageTags()
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            // Navigation to Login is handled by FriendZoneNavHost based on authSession
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LanguageSelector(
                    isSystemDefault = isSystemDefault,
                    currentLanguage = currentLanguage
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                HorizontalDivider(color = BorderGray, thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor.copy(alpha = 0.1f),
                        contentColor = ErrorColor,
                        disabledContainerColor = ErrorColor.copy(alpha = 0.05f),
                        disabledContentColor = ErrorColor.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ErrorColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            stringResource(R.string.btn_delete_account),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                AppInfoFooter()
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.title_delete_account)) },
            text = { Text(stringResource(R.string.msg_delete_account_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun LanguageSelector(
    isSystemDefault: Boolean,
    currentLanguage: String
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedLabel = when {
        isSystemDefault -> stringResource(R.string.label_system_default)
        currentLanguage.startsWith("en") -> "English 🇺🇸"
        currentLanguage.contains("AR") -> "Español (Argentina) 🇦🇷"
        currentLanguage.startsWith("es") -> "Español 🇪🇸"
        else -> stringResource(R.string.label_system_default)
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                .clickable { expanded = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMain,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Surface)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.label_system_default) + " ⚙️") },
                onClick = {
                    expanded = false
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            )
            DropdownMenuItem(
                text = { Text("English 🇺🇸") },
                onClick = {
                    expanded = false
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                }
            )
            DropdownMenuItem(
                text = { Text("Español 🇪🇸") },
                onClick = {
                    expanded = false
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                }
            )
            DropdownMenuItem(
                text = { Text("Español (Argentina) 🇦🇷") },
                onClick = {
                    expanded = false
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es-AR"))
                }
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
            stringResource(R.string.msg_footer_info),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
