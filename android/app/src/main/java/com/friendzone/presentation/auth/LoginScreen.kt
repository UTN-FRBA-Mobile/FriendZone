package com.example.friendzone.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzPrimaryDark
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzSurface
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
                    colors = listOf(FzPrimaryDark, FzPrimary, FzBackground),
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text("👥", style = MaterialTheme.typography.displayLarge)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                "FriendZone",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Text(
                "Connect with friends nearby",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LoginCardTopShape)
                .background(FzSurface)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text("Welcome back", style = MaterialTheme.typography.headlineMedium, color = FzTextMain)
            Spacer(modifier = Modifier.height(24.dp))

            FriendZoneTextField(
                label = "Email or username",
                value = uiState.emailOrUsername,
                onValueChange = viewModel::onEmailOrUsernameChange,
                placeholder = "you@example.com",
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = "Password",
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
                    Text("Remember me", style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
                }
                Text(
                    "Forgot?",
                    style = MaterialTheme.typography.labelLarge,
                    color = FzPrimary,
                    modifier = Modifier.clickable { },
                )
            }

            uiState.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            FriendZonePrimaryButton(
                text = "Log In",
                onClick = viewModel::login,
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodySmall,
                color = FzTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Sign up",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = FzPrimary,
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
                listOf("Privacy", "Terms", "Help").forEachIndexed { index, label ->
                    if (index > 0) {
                        Text(" · ", color = FzTextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(label, color = FzTextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
