package com.example.friendzone.presentation.events

import com.example.friendzone.data.remote.websocket.EventSocketManager
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.repository.LocationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.AppError
import com.example.friendzone.testutil.MainDispatcherRule
import com.example.friendzone.testutil.testInvitation
import com.example.friendzone.testutil.testPendingInvitation
import com.example.friendzone.testutil.testUser
import com.example.friendzone.testutil.wheneverSuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val eventRepository: EventRepository = mock()
    private val invitationRepository: InvitationRepository = mock()
    private val locationRepository: LocationRepository = mock()
    private val eventSocketManager: EventSocketManager = mock()
    private val authRepository: AuthRepository = mock()

    private val pendingInvitation = testPendingInvitation(id = "pending-inv-1")
    private val socketEvents = MutableSharedFlow<com.example.friendzone.data.remote.websocket.SocketEvent>(
        extraBufferCapacity = 1,
    )

    @Before
    fun setUp() {
        whenever(authRepository.currentUser).thenReturn(flowOf(testUser()))
        whenever(eventSocketManager.events).thenReturn(socketEvents)
        wheneverSuspend { eventSocketManager.connect() }.thenReturn(Unit)
        wheneverSuspend { eventRepository.getCachedMine() }.thenReturn(null)
        wheneverSuspend { eventRepository.getMine() }.thenReturn(ApiResult.Success(emptyList()))
        wheneverSuspend { invitationRepository.getCachedMinePending() }.thenReturn(null)
        wheneverSuspend { invitationRepository.getMinePending() }
            .thenReturn(ApiResult.Success(listOf(pendingInvitation)))
        wheneverSuspend { invitationRepository.getCachedByEvent(any()) }.thenReturn(null)
        wheneverSuspend { invitationRepository.getByEvent(any()) }.thenReturn(ApiResult.Success(emptyList()))
        wheneverSuspend { locationRepository.getCachedParticipants(any()) }.thenReturn(null)
        wheneverSuspend { locationRepository.getParticipants(any()) }.thenReturn(ApiResult.Success(emptyList()))
    }

    @Test
    fun selectTab_updatesSelectedTab() = runTest {
        val viewModel = createViewModel()

        viewModel.selectTab(EventsTab.Past)

        assertEquals(EventsTab.Past, viewModel.selectedTab.value)
    }

    @Test
    fun openInvitationById_findsInvitation_selectsInvitationsTab() = runTest {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is EventsUiState.Data)

        viewModel.openInvitationById("pending-inv-1")

        assertEquals(EventsTab.Invitations, viewModel.selectedTab.value)
        assertEquals(pendingInvitation, viewModel.selectedInvitation.value)
    }

    @Test
    fun respondToSelectedInvitation_accept_showsJoinedSnackbar() = runTest {
        wheneverSuspend { invitationRepository.respond("pending-inv-1", InvitationStatus.ACCEPTED) }
            .thenReturn(ApiResult.Success(testInvitation(status = InvitationStatus.ACCEPTED)))
        val viewModel = createViewModel()
        viewModel.openInvitation(pendingInvitation)

        viewModel.respondToSelectedInvitation(accept = true)

        assertEquals("Joined event", viewModel.snackbarMessage.value)
        assertNull(viewModel.selectedInvitation.value)
        assertEquals(false, viewModel.isInvitationActionLoading.value)
    }

    @Test
    fun respondToSelectedInvitation_decline_showsDeclinedSnackbar() = runTest {
        wheneverSuspend { invitationRepository.respond("pending-inv-1", InvitationStatus.REJECTED) }
            .thenReturn(ApiResult.Success(testInvitation(status = InvitationStatus.REJECTED)))
        val viewModel = createViewModel()
        viewModel.openInvitation(pendingInvitation)

        viewModel.respondToSelectedInvitation(accept = false)

        assertEquals("Declined", viewModel.snackbarMessage.value)
    }

    @Test
    fun respondToSelectedInvitation_onError_showsErrorSnackbar() = runTest {
        wheneverSuspend { invitationRepository.respond("pending-inv-1", InvitationStatus.ACCEPTED) }
            .thenReturn(ApiResult.Error(AppError.Network))
        val viewModel = createViewModel()
        viewModel.openInvitation(pendingInvitation)

        viewModel.respondToSelectedInvitation(accept = true)

        assertEquals(
            "Unable to connect. Check your internet connection and try again.",
            viewModel.snackbarMessage.value,
        )
        assertEquals(false, viewModel.isInvitationActionLoading.value)
    }

    private fun createViewModel(): EventsViewModel =
        EventsViewModel(
            eventRepository = eventRepository,
            invitationRepository = invitationRepository,
            locationRepository = locationRepository,
            eventSocketManager = eventSocketManager,
            authRepository = authRepository,
        )
}
