package com.example.friendzone.domain.result

import org.junit.Assert.assertEquals
import org.junit.Test

class AppErrorTest {
    @Test
    fun displayMessage_network_returnsConnectivityMessage() {
        assertEquals(
            "Unable to connect. Check your internet connection and try again.",
            AppError.Network.displayMessage(),
        )
    }

    @Test
    fun displayMessage_unauthorized_returnsSessionMessage() {
        assertEquals(
            "Your session has expired. Please sign in again.",
            AppError.Unauthorized.displayMessage(),
        )
    }

    @Test
    fun displayMessage_httpWithKnownApiLine_humanizesCredentialsError() {
        val error = AppError.Http(code = 401, message = "invalid credentials")

        assertEquals(
            "Email/username or password is incorrect.",
            error.displayMessage(),
        )
    }

    @Test
    fun displayMessage_httpWithGenericBadRequest_returnsDefault400Message() {
        val error = AppError.Http(code = 400, message = "bad request")

        assertEquals(
            "Some of the information you entered is invalid. Please review the form and try again.",
            error.displayMessage(),
        )
    }

    @Test
    fun displayMessage_http5xxWithBlankBody_returnsServerErrorMessage() {
        val error = AppError.Http(code = 500, message = "   ")

        assertEquals(
            "Something went wrong on our end. Please try again in a moment.",
            error.displayMessage(),
        )
    }

    @Test
    fun displayMessage_unknownWithBlankMessage_returnsFallback() {
        val error = AppError.Unknown(message = "")

        assertEquals(
            "Something went wrong. Please try again.",
            error.displayMessage(),
        )
    }
}
