package com.example.friendzone.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun RegisterScreen(
    onNavigateLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryDark, Primary, Background),
                ),
            )
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthLogo(size = 72.dp, cornerRadius = 22.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displayLarge, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LoginCardTopShape)
                .background(Surface)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(stringResource(R.string.title_create_account), style = MaterialTheme.typography.headlineMedium, color = TextMain)
            Spacer(modifier = Modifier.height(24.dp))

            FriendZoneTextField(
                label = stringResource(R.string.label_email),
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = stringResource(R.string.placeholder_email),
                required = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_username),
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                placeholder = stringResource(R.string.placeholder_username),
                required = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_display_name),
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                placeholder = stringResource(R.string.placeholder_display_name),
                required = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_password),
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "········",
                isPassword = true,
                required = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_confirm_password),
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = "········",
                isPassword = true,
                required = true,
            )

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            FriendZonePrimaryButton(
                text = stringResource(R.string.btn_signup),
                onClick = viewModel::register,
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.prompt_have_account),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.btn_login),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateLogin)
                    .padding(top = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
