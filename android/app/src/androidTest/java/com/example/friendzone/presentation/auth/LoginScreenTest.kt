package com.example.friendzone.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.ui.theme.FriendZoneTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loginScreen_displaysWelcomeAndFormFields() {
        val authRepository: AuthRepository = mock()
        composeRule.setContent {
            FriendZoneTheme {
                LoginScreen(
                    onNavigateRegister = {},
                    viewModel = LoginViewModel(authRepository),
                )
            }
        }

        composeRule.onNodeWithText("Welcome back").assertIsDisplayed()
        composeRule.onNodeWithText("Email or username").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule.onNodeWithText("Log In").assertIsDisplayed()
    }
}
