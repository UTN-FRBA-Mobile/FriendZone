package com.example.friendzone.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.friendzone.presentation.notifications.NotificationsBadgeViewModel
import com.example.friendzone.presentation.notifications.NotificationsScreen
import com.example.friendzone.presentation.profile.ProfileScreen
import com.example.friendzone.ui.theme.FzBackground

private const val TransitionDuration = 380

private fun AnimatedContentTransitionScope<*>.slideInFromRight() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TransitionDuration)) + fadeIn(tween(TransitionDuration))

private fun AnimatedContentTransitionScope<*>.slideOutToLeft() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TransitionDuration)) + fadeOut(tween(TransitionDuration / 2))

private fun AnimatedContentTransitionScope<*>.slideInFromLeft() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TransitionDuration)) + fadeIn(tween(TransitionDuration))

private fun AnimatedContentTransitionScope<*>.slideOutToRight() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TransitionDuration)) + fadeOut(tween(TransitionDuration / 2))

@Composable
fun FriendZoneNavHost(
    appNavViewModel: AppNavViewModel = hiltViewModel(),
) {
    val isLoggedIn by appNavViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val friendsBadgeViewModel: FriendsBadgeViewModel = hiltViewModel()
    val notificationsBadgeViewModel: NotificationsBadgeViewModel = hiltViewModel()
    val pendingFriendsCount by friendsBadgeViewModel.pendingCount.collectAsStateWithLifecycle()
    val notificationBadgeCount by notificationsBadgeViewModel.badgeCount.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Screen.bottomBarRoutes

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

    LaunchedEffect(isLoggedIn, currentRoute) {
        if (isLoggedIn) {
            if (currentRoute == Screen.Login || currentRoute == Screen.Register || currentRoute == null) {
                navController.navigate(Screen.Events) {
                    popUpTo(Screen.Login) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (currentRoute !in setOf(Screen.Login, Screen.Register, null)) {
            navController.navigate(Screen.Login) {
                popUpTo(Screen.Login) { inclusive = true }
                launchSingleTop = true
            }
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
                            navController.navigate(Screen.Events) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onFriendsClick = {
                        if (currentRoute != Screen.Friends) {
                            navController.navigate(Screen.Friends) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
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
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                EventsScreen(
                    notificationBadgeCount = notificationBadgeCount,
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications)
                    },
                    onCreateClick = {
                        navController.navigate(Screen.Create)
                    },
                    onEventDetailClick = { eventId ->
                        navController.navigate(Screen.eventDetail(eventId))
                    },
                )
            }
            composable(
                route = Screen.EventDetail,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                EventDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.Friends,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
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
                )
            }
            composable(
                route = Screen.Profile,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                ProfileScreen(
                    notificationBadgeCount = notificationBadgeCount,
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications)
                    },
                )
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
    }
}
