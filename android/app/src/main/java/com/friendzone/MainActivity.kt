package com.example.friendzone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.friendzone.presentation.navigation.DeepLinkViewModel
import com.example.friendzone.presentation.navigation.FriendZoneNavHost
import com.example.friendzone.ui.theme.FriendZoneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var intentVersion by mutableIntStateOf(0)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activity = LocalContext.current as MainActivity
            val deepLinkViewModel: DeepLinkViewModel = hiltViewModel()
            LaunchedEffect(activity.intentVersion) {
                deepLinkViewModel.consumeFromIntent(activity.intent)
            }
            FriendZoneTheme {
                FriendZoneNavHost(deepLinkViewModel = deepLinkViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentVersion++
    }
}
