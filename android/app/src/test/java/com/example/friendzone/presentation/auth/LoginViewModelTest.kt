package com.example.friendzone.presentation.auth

import com.example.friendzone.domain.model.AuthSession
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.AppError
import com.example.friendzone.testutil.MainDispatcherRule
import com.example.friendzone.testutil.testUser
import com.example.friendzone.testutil.wheneverSuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mock()

    @Test
    fun login_withBlankFields_setsValidationError() = runTest {
        val viewModel = LoginViewModel(authRepository)

        viewModel.login()

        assertEquals(
            "Email/username and password are required.",
            viewModel.uiState.value.errorMessage,
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun login_onSuccess_clearsLoading() = runTest {
        val session = AuthSession(
            accessToken = "token",
            refreshToken = "refresh",
            user = testUser(),
        )
        wheneverSuspend { authRepository.login("user@example.com", "password123") }
            .thenReturn(ApiResult.Success(session))
        val viewModel = LoginViewModel(authRepository)
        viewModel.onEmailOrUsernameChange("user@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.login()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun login_onApiError_setsDisplayMessage() = runTest {
        wheneverSuspend { authRepository.login("user@example.com", "wrong") }
            .thenReturn(ApiResult.Error(AppError.Http(401, "invalid credentials")))
        val viewModel = LoginViewModel(authRepository)
        viewModel.onEmailOrUsernameChange("user@example.com")
        viewModel.onPasswordChange("wrong")

        viewModel.login()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Email/username or password is incorrect.",
            viewModel.uiState.value.errorMessage,
        )
    }
}
