package com.example.friendzone.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.friendzone.ui.theme.FriendZoneTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class FriendZoneBottomBarTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomBar_displaysMainNavigationLabels() {
        composeRule.setContent {
            FriendZoneTheme {
                FriendZoneBottomBar(
                    selectedTab = BottomNavTab.Events,
                    onEventsClick = {},
                    onFriendsClick = {},
                    onProfileClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Events").assertIsDisplayed()
        composeRule.onNodeWithText("Friends").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }
}
