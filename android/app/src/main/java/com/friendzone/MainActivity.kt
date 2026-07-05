package com.example.friendzone

import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
class MainActivity : AppCompatActivity() {
    var intentVersion by mutableIntStateOf(0)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val activity = remember(context) { 
                var ctx = context
                while (ctx is ContextWrapper) {
                    if (ctx is MainActivity) break
                    ctx = ctx.baseContext
                }
                ctx as? MainActivity
            }
            
            val deepLinkViewModel: DeepLinkViewModel = hiltViewModel()
            
            LaunchedEffect(activity?.intentVersion) {
                activity?.let {
                    deepLinkViewModel.consumeFromIntent(it.intent)
                }
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
