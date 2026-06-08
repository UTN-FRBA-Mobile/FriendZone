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
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk2
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzLoginGradientMid
import com.example.friendzone.ui.theme.FzLoginGradientTop
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
                    colors = listOf(FzLoginGradientTop, FzLoginGradientMid, FzBackground),
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
                .padding(top = 48.dp, bottom = 20.dp),
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
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                "FriendZone",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Text(
                "Connect with friends nearby",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LoginCardTopShape)
                .background(FzSurface)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text("Welcome back", style = MaterialTheme.typography.headlineMedium, color = FzInk)
            Spacer(modifier = Modifier.height(18.dp))

            FriendZoneTextField(
                label = "Email or username",
                value = uiState.emailOrUsername,
                onValueChange = viewModel::onEmailOrUsernameChange,
                placeholder = "you@example.com",
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("Remember me", style = MaterialTheme.typography.bodySmall, color = FzInk2)
                }
                Text(
                    "Forgot?",
                    style = MaterialTheme.typography.labelLarge,
                    color = FzInk,
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

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = buildString {
                    append("Don't have an account? ")
                },
                style = MaterialTheme.typography.bodySmall,
                color = FzInk3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Sign up",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = FzInk,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateRegister)
                    .padding(top = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                listOf("Privacy", "Terms", "Help").forEachIndexed { index, label ->
                    if (index > 0) {
                        Text(" · ", color = FzInk3, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(label, color = FzInk3, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
