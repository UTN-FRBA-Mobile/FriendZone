package com.example.friendzone.presentation.friends

import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.AppError
import com.example.friendzone.testutil.MainDispatcherRule
import com.example.friendzone.testutil.testFriendRequest
import com.example.friendzone.testutil.testUser
import com.example.friendzone.testutil.wheneverSuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val friendRepository: FriendRepository = mock()
    private val userRepository: UserRepository = mock()

    private val existingFriend = testUser(id = "friend-1", username = "existing")
    private val requester = testUser(id = "requester-1", username = "pendinguser")
    private val newUser = testUser(id = "new-1", username = "newuser", displayName = "New User")

    @Before
    fun setUp() {
        wheneverSuspend { friendRepository.getCachedFriends() }.thenReturn(null)
        wheneverSuspend { friendRepository.getCachedIncomingRequests() }.thenReturn(null)
        wheneverSuspend { friendRepository.getFriends() }.thenReturn(ApiResult.Success(listOf(existingFriend)))
        wheneverSuspend { friendRepository.getIncomingRequests() }.thenReturn(
            ApiResult.Success(listOf(testFriendRequest(requester = requester))),
        )
    }

    @Test
    fun lookupUser_alreadyFriend_returnsErrorResult() = runTest {
        val viewModel = FriendsViewModel(friendRepository, userRepository)
        viewModel.updateSearchQuery("existing")
        wheneverSuspend { userRepository.lookup("existing") }.thenReturn(ApiResult.Success(existingFriend))

        viewModel.lookupUser()

        val result = viewModel.uiState.value.lookupResult
        assertTrue(result is LookupResult.Error)
        assertEquals("Already friends", (result as LookupResult.Error).message)
    }

    @Test
    fun lookupUser_pendingRequestFromUser_returnsErrorResult() = runTest {
        val viewModel = FriendsViewModel(friendRepository, userRepository)
        viewModel.updateSearchQuery("pendinguser")
        wheneverSuspend { userRepository.lookup("pendinguser") }.thenReturn(ApiResult.Success(requester))

        viewModel.lookupUser()

        val result = viewModel.uiState.value.lookupResult
        assertTrue(result is LookupResult.Error)
        assertEquals(
            "This user already sent you a request",
            (result as LookupResult.Error).message,
        )
    }

    @Test
    fun lookupUser_newUser_returnsFoundResult() = runTest {
        val viewModel = FriendsViewModel(friendRepository, userRepository)
        viewModel.updateSearchQuery("newuser")
        wheneverSuspend { userRepository.lookup("newuser") }.thenReturn(ApiResult.Success(newUser))

        viewModel.lookupUser()

        val result = viewModel.uiState.value.lookupResult
        assertTrue(result is LookupResult.Found)
        assertEquals(newUser, (result as LookupResult.Found).user)
    }

    @Test
    fun lookupUser_apiError_returnsNotFound() = runTest {
        val viewModel = FriendsViewModel(friendRepository, userRepository)
        viewModel.updateSearchQuery("ghost")
        wheneverSuspend { userRepository.lookup("ghost") }.thenReturn(ApiResult.Error(AppError.Network))

        viewModel.lookupUser()

        assertEquals(LookupResult.NotFound, viewModel.uiState.value.lookupResult)
    }
}
