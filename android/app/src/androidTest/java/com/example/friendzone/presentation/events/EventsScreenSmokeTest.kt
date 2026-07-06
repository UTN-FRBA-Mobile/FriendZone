package com.example.friendzone.presentation.events

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.friendzone.data.local.TokenManager
import com.example.friendzone.data.local.TokenStore
import com.example.friendzone.data.remote.websocket.EventSocketManager
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.repository.LocationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.ui.theme.FriendZoneTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class EventsScreenSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun eventsScreen_withEmptyData_displaysHeaderAndTabs() {
        val eventRepository: EventRepository = mock()
        val invitationRepository: InvitationRepository = mock()
        val locationRepository: LocationRepository = mock()
        val authRepository: AuthRepository = mock()
        val eventSocketManager = EventSocketManager(
            TokenManager(TokenStore(composeRule.activity.applicationContext)),
        )
        val testUser = User(
            id = "user-1",
            email = "user@example.com",
            username = "testuser",
            displayName = "Test User",
            fcmToken = null,
            locationSharingEnabled = true,
            createdAt = "2026-07-05T18:00:00Z",
        )

        whenever(authRepository.currentUser).thenReturn(flowOf(testUser))
        runBlocking {
            whenever(eventRepository.getCachedMine()).thenReturn(null)
            whenever(eventRepository.getMine()).thenReturn(ApiResult.Success(emptyList()))
            whenever(invitationRepository.getCachedMinePending()).thenReturn(null)
            whenever(invitationRepository.getMinePending()).thenReturn(ApiResult.Success(emptyList()))
            whenever(invitationRepository.getCachedByEvent(any())).thenReturn(null)
            whenever(invitationRepository.getByEvent(any())).thenReturn(ApiResult.Success(emptyList()))
            whenever(locationRepository.getCachedParticipants(any())).thenReturn(null)
            whenever(locationRepository.getParticipants(any())).thenReturn(ApiResult.Success(emptyList()))
        }

        val viewModel = EventsViewModel(
            eventRepository = eventRepository,
            invitationRepository = invitationRepository,
            locationRepository = locationRepository,
            eventSocketManager = eventSocketManager,
            authRepository = authRepository,
        )

        composeRule.setContent {
            FriendZoneTheme {
                EventsScreen(
                    onCreateClick = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("My Events").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("My Events").assertIsDisplayed()
        composeRule.onNodeWithText("Upcoming").assertIsDisplayed()
        composeRule.onNodeWithText("Past").assertIsDisplayed()
        composeRule.onNodeWithText("Invites").assertIsDisplayed()
    }
}
