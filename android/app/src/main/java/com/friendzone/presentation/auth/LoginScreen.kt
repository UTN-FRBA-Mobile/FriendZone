package com.example.friendzone.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.PrimaryDark
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary
import com.example.friendzone.ui.theme.Surface
import com.example.friendzone.ui.theme.LoginCardTopShape

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryDark, Primary, Background),
                    startY = 0f,
                    endY = 1200f,
                ),
            )
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthLogo(size = 80.dp, cornerRadius = 24.dp)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Text(
                stringResource(R.string.login_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LoginCardTopShape)
                .background(Surface)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(stringResource(R.string.title_welcome_back), style = MaterialTheme.typography.headlineMedium, color = TextMain)
            Spacer(modifier = Modifier.height(24.dp))

            FriendZoneTextField(
                label = stringResource(R.string.label_email_username),
                value = uiState.emailOrUsername,
                onValueChange = viewModel::onEmailOrUsernameChange,
                placeholder = stringResource(R.string.placeholder_email),
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_password),
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "········",
                isPassword = true,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text(stringResource(R.string.label_remember_me), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Text(
                    stringResource(R.string.action_forgot_password),
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary,
                    modifier = Modifier.clickable { },
                )
            }

            uiState.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            FriendZonePrimaryButton(
                text = stringResource(R.string.btn_login),
                onClick = viewModel::login,
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.prompt_no_account),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.btn_signup),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateRegister)
                    .padding(top = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                listOf(
                    stringResource(R.string.link_privacy),
                    stringResource(R.string.link_terms),
                    stringResource(R.string.link_help)
                ).forEachIndexed { index, label ->
                    if (index > 0) {
                        Text(" · ", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
