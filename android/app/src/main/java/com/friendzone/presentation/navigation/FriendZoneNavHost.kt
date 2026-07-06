package com.example.friendzone.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.friendzone.presentation.create.CreateEventViewModel
import com.example.friendzone.presentation.auth.LoginScreen
import com.example.friendzone.presentation.auth.RegisterScreen
import com.example.friendzone.presentation.components.BottomNavTab
import com.example.friendzone.presentation.components.FriendZoneBottomBar
import com.example.friendzone.presentation.create.CreateEventStep1Screen
import com.example.friendzone.presentation.create.CreateEventStep2Screen
import com.example.friendzone.presentation.events.EventDetailScreen
import com.example.friendzone.presentation.events.EventsScreen
import com.example.friendzone.presentation.friends.FriendsBadgeViewModel
import com.example.friendzone.presentation.friends.FriendsScreen
import com.example.friendzone.presentation.friends.FriendsTab
import com.example.friendzone.presentation.events.EventsTab
import com.example.friendzone.presentation.invite.IncomingInviteBottomSheet
import com.example.friendzone.presentation.notifications.NotificationsBadgeViewModel
import com.example.friendzone.presentation.notifications.NotificationsScreen
import com.example.friendzone.presentation.notifications.RequestNotificationPermissionWhenLoggedIn
import com.example.friendzone.presentation.profile.ProfileScreen
import com.example.friendzone.presentation.settings.SettingsScreen
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzInk

private const val TransitionDuration = 380

private fun AnimatedContentTransitionScope<*>.slideInFromRight() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TransitionDuration)) + fadeIn(tween(TransitionDuration))

private fun AnimatedContentTransitionScope<*>.slideOutToLeft() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TransitionDuration)) + fadeOut(tween(TransitionDuration / 2))

private fun AnimatedContentTransitionScope<*>.slideInFromLeft() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TransitionDuration)) + fadeIn(tween(TransitionDuration))

private fun AnimatedContentTransitionScope<*>.slideOutToRight() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TransitionDuration)) + fadeOut(tween(TransitionDuration / 2))

private val bottomBarTabIndex = mapOf(
    Screen.Events to 0,
    Screen.Friends to 1,
    Screen.Profile to 2,
)

private fun tabIndex(route: String?): Int? = route?.let { bottomBarTabIndex[it] }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isBottomBarTabSwitch(): Boolean {
    val initialIndex = tabIndex(initialState.destination.route)
    val targetIndex = tabIndex(targetState.destination.route)
    return initialIndex != null && targetIndex != null && initialIndex != targetIndex
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isMovingForwardInTabs(): Boolean {
    val initialIndex = tabIndex(initialState.destination.route) ?: return true
    val targetIndex = tabIndex(targetState.destination.route) ?: return true
    return targetIndex > initialIndex
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomBarEnterTransition(): EnterTransition {
    if (!isBottomBarTabSwitch()) return slideInFromRight()
    return if (isMovingForwardInTabs()) slideInFromRight() else slideInFromLeft()
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomBarExitTransition(): ExitTransition {
    if (!isBottomBarTabSwitch()) return slideOutToLeft()
    return if (isMovingForwardInTabs()) slideOutToLeft() else slideOutToRight()
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomBarPopEnterTransition(): EnterTransition {
    if (!isBottomBarTabSwitch()) return slideInFromLeft()
    return if (isMovingForwardInTabs()) slideInFromRight() else slideInFromLeft()
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomBarPopExitTransition(): ExitTransition {
    if (!isBottomBarTabSwitch()) return slideOutToRight()
    return if (isMovingForwardInTabs()) slideOutToLeft() else slideOutToRight()
}

private fun NavHostController.navigateToBottomBarTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Events) {
            saveState = true
            inclusive = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun isFriendRequestDeepLink(deepLink: DeepLink): Boolean =
    deepLink.type == "friend.request" ||
        (deepLink.requestId != null && deepLink.type != "friend.request.accepted")

private val authGateRoutes = setOf(Screen.Bootstrap, Screen.Login, Screen.Register)

@Composable
fun FriendZoneNavHost(
    appNavViewModel: AppNavViewModel = hiltViewModel(),
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    val authSession by appNavViewModel.authSession.collectAsStateWithLifecycle()
    val isLoggedIn = authSession == AuthSessionState.LoggedIn
    val friendsBadgeViewModel: FriendsBadgeViewModel = hiltViewModel()
    val notificationsBadgeViewModel: NotificationsBadgeViewModel = hiltViewModel()
    val pendingFriendsCount by friendsBadgeViewModel.pendingCount.collectAsStateWithLifecycle()
    val notificationBadgeCount by notificationsBadgeViewModel.badgeCount.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Screen.bottomBarRoutes
    val pendingDeepLink by deepLinkViewModel.pending.collectAsStateWithLifecycle()
    var eventsInitialTab by remember { mutableStateOf<EventsTab?>(null) }
    var eventsOpenInvitationId by remember { mutableStateOf<String?>(null) }
    var friendsInitialTab by remember { mutableStateOf<FriendsTab?>(null) }
    var pendingInviteUsername by remember { mutableStateOf<String?>(null) }

    RequestNotificationPermissionWhenLoggedIn(isLoggedIn)

    LaunchedEffect(authSession, pendingDeepLink, currentRoute) {
        when (authSession) {
            AuthSessionState.Loading -> return@LaunchedEffect
            AuthSessionState.LoggedOut -> {
                if (currentRoute !in setOf(Screen.Login, Screen.Register)) {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Bootstrap) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AuthSessionState.LoggedIn -> {
                val deepLink = pendingDeepLink
                if (deepLink != null) {
                    when {
                        deepLink.type == "invitation.created" || deepLink.invitationId != null -> {
                            eventsInitialTab = EventsTab.Invitations
                            eventsOpenInvitationId = deepLink.invitationId
                            navController.navigate(Screen.Events) {
                                popUpTo(Screen.Bootstrap) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        isFriendRequestDeepLink(deepLink) -> {
                            friendsInitialTab = FriendsTab.Requests
                            navController.navigate(Screen.Friends) {
                                popUpTo(Screen.Bootstrap) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        deepLink.inviteUsername != null -> {
                            // Show the invite modal over the current screen; land on
                            // Friends as the base screen on a cold start via the link.
                            pendingInviteUsername = deepLink.inviteUsername
                            if (currentRoute in authGateRoutes) {
                                navController.navigate(Screen.Friends) {
                                    popUpTo(Screen.Bootstrap) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                    deepLinkViewModel.clear()
                    return@LaunchedEffect
                }

                if (currentRoute in authGateRoutes) {
                    navController.navigate(Screen.Events) {
                        popUpTo(Screen.Bootstrap) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            friendsBadgeViewModel.refresh()
            notificationsBadgeViewModel.refresh()
        }
    }

    LaunchedEffect(currentRoute) {
        if (isLoggedIn && currentRoute in Screen.bottomBarRoutes) {
            notificationsBadgeViewModel.refresh()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FzBackground,
        contentColor = Color.Unspecified,
        bottomBar = {
            if (showBottomBar) {
                FriendZoneBottomBar(
                    selectedTab = when (currentRoute) {
                        Screen.Profile -> BottomNavTab.Profile
                        Screen.Friends -> BottomNavTab.Friends
                        else -> BottomNavTab.Events
                    },
                    pendingFriendsCount = pendingFriendsCount,
                    onEventsClick = {
                        if (currentRoute != Screen.Events) {
                            navController.navigateToBottomBarTab(Screen.Events)
                        }
                    },
                    onFriendsClick = {
                        if (currentRoute != Screen.Friends) {
                            navController.navigateToBottomBarTab(Screen.Friends)
                        }
                    },
                    onProfileClick = {
                        if (currentRoute != Screen.Profile) {
                            navController.navigateToBottomBarTab(Screen.Profile)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Bootstrap,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Screen.Bootstrap) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FzBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = FzInk)
                }
            }
            composable(Screen.Login) {
                LoginScreen(
                    onNavigateRegister = {
                        navController.navigate(Screen.Register)
                    },
                )
            }
            composable(
                route = Screen.Register,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                RegisterScreen(
                    onNavigateLogin = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.Events,
                enterTransition = { bottomBarEnterTransition() },
                exitTransition = { bottomBarExitTransition() },
                popEnterTransition = { bottomBarPopEnterTransition() },
                popExitTransition = { bottomBarPopExitTransition() },
            ) {
                EventsScreen(
                    notificationBadgeCount = notificationBadgeCount,
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications)
                    },
                    onCreateClick = {
                        navController.navigate(Screen.Create)
                    },
                    onEventDetailClick = { eventId, openMap ->
                        navController.navigate(Screen.eventDetail(eventId, openMap))
                    },
                    initialTab = eventsInitialTab,
                    openInvitationId = eventsOpenInvitationId,
                )
            }
            composable(
                route = Screen.EventDetail,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType },
                    navArgument("openMap") { type = NavType.BoolType; defaultValue = false }
                ),
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                EventDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.Friends,
                enterTransition = { bottomBarEnterTransition() },
                exitTransition = { bottomBarExitTransition() },
                popEnterTransition = { bottomBarPopEnterTransition() },
                popExitTransition = { bottomBarPopExitTransition() },
            ) {
                FriendsScreen(
                    notificationBadgeCount = notificationBadgeCount,
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications)
                    },
                    onFriendsChanged = {
                        friendsBadgeViewModel.refresh()
                        notificationsBadgeViewModel.refresh()
                    },
                    initialTab = friendsInitialTab,
                )
            }
            composable(
                route = Screen.Profile,
                enterTransition = { bottomBarEnterTransition() },
                exitTransition = { bottomBarExitTransition() },
                popEnterTransition = { bottomBarPopEnterTransition() },
                popExitTransition = { bottomBarPopExitTransition() },
            ) {
                ProfileScreen(
                    notificationBadgeCount = notificationBadgeCount,
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings)
                    }
                )
            }
            composable(
                route = Screen.Settings,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.Notifications,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onBadgeRefresh = { notificationsBadgeViewModel.refresh() },
                    onActionFinished = {
                        notificationsBadgeViewModel.refresh()
                        friendsBadgeViewModel.refresh()
                        navController.popBackStack()
                    },
                )
            }
            navigation(
                route = Screen.Create,
                startDestination = Screen.CreateStep1,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                composable(Screen.CreateStep1) { backStackEntry ->
                    val createEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.Create)
                    }
                    val viewModel: CreateEventViewModel = hiltViewModel(createEntry)
                    CreateEventStep1Screen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.navigate(Screen.CreateStep2) },
                    )
                }
                composable(Screen.CreateStep2) { backStackEntry ->
                    val createEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.Create)
                    }
                    val viewModel: CreateEventViewModel = hiltViewModel(createEntry)
                    CreateEventStep2Screen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onCreated = {
                            navController.navigate(Screen.Events) {
                                popUpTo(Screen.Create) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }

        if (isLoggedIn) {
            pendingInviteUsername?.let { username ->
                IncomingInviteBottomSheet(
                    username = username,
                    onDismiss = { pendingInviteUsername = null },
                    onAdded = { friendsBadgeViewModel.refresh() },
                )
            }
        }
    }
}
