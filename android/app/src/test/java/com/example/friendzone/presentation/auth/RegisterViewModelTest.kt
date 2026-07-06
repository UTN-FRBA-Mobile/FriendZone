package com.example.friendzone.presentation.auth

import com.example.friendzone.domain.model.AuthSession
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.testutil.MainDispatcherRule
import com.example.friendzone.testutil.testUser
import com.example.friendzone.testutil.wheneverSuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mock()

    @Test
    fun register_withMissingField_showsRequiredMessage() = runTest {
        val viewModel = RegisterViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onUsernameChange("validuser")
        viewModel.onDisplayNameChange("")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        viewModel.register()

        assertEquals("All fields are required.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun register_withInvalidEmail_showsEmailMessage() = runTest {
        val viewModel = validFormViewModel(email = "not-an-email")

        viewModel.register()

        assertEquals("Enter a valid email address.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun register_withInvalidUsernameChars_showsUsernameFormatMessage() = runTest {
        val viewModel = validFormViewModel(username = "bad user!")

        viewModel.register()

        assertEquals(
            "Username may only contain letters, numbers, and underscores.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun register_withShortUsername_showsMinLengthMessage() = runTest {
        val viewModel = validFormViewModel(username = "ab")

        viewModel.register()

        assertEquals("Username must be at least 3 characters.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun register_withShortPassword_showsPasswordLengthMessage() = runTest {
        val viewModel = validFormViewModel(password = "short", confirmPassword = "short")

        viewModel.register()

        assertEquals("Password must be at least 8 characters.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun register_withMismatchedPasswords_showsMismatchMessage() = runTest {
        val viewModel = validFormViewModel(confirmPassword = "different123")

        viewModel.register()

        assertEquals("Passwords do not match.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun register_withValidForm_callsRepository() = runTest {
        val session = AuthSession(
            accessToken = "token",
            refreshToken = "refresh",
            user = testUser(),
        )
        wheneverSuspend {
            authRepository.register(
                email = "user@example.com",
                username = "validuser",
                password = "password123",
                displayName = "Test User",
            )
        }.thenReturn(ApiResult.Success(session))
        val viewModel = validFormViewModel()

        viewModel.register()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        runBlocking {
            verify(authRepository).register(
                email = "user@example.com",
                username = "validuser",
                password = "password123",
                displayName = "Test User",
            )
        }
    }

    private fun validFormViewModel(
        email: String = "user@example.com",
        username: String = "validuser",
        displayName: String = "Test User",
        password: String = "password123",
        confirmPassword: String = password,
    ): RegisterViewModel {
        val viewModel = RegisterViewModel(authRepository)
        viewModel.onEmailChange(email)
        viewModel.onUsernameChange(username)
        viewModel.onDisplayNameChange(displayName)
        viewModel.onPasswordChange(password)
        viewModel.onConfirmPasswordChange(confirmPassword)
        return viewModel
    }
}
