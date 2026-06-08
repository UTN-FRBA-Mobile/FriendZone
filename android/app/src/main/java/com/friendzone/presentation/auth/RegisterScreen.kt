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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzLoginGradientMid
import com.example.friendzone.ui.theme.FzLoginGradientTop
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.LoginCardTopShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

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
                    colors = listOf(FzLoginGradientTop, FzLoginGradientMid, FzBackground),
                ),
            )
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text("👥", style = MaterialTheme.typography.displayLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("FriendZone", style = MaterialTheme.typography.displayLarge, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LoginCardTopShape)
                .background(FzSurface)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text("Create account", style = MaterialTheme.typography.headlineMedium, color = FzInk)
            Spacer(modifier = Modifier.height(18.dp))

            FriendZoneTextField(
                label = "Email",
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "you@example.com",
                required = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Username",
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                placeholder = "johndoe",
                required = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Display name",
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                placeholder = "John Doe",
                required = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Password",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "········",
                isPassword = true,
                required = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Confirm password",
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

            Spacer(modifier = Modifier.height(16.dp))
            FriendZonePrimaryButton(
                text = "Sign up",
                onClick = viewModel::register,
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodySmall,
                color = FzInk3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Log in",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = FzInk,
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
