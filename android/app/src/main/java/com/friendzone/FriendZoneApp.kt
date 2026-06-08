package com.example.friendzone

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class FriendZoneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // osmdroid requiere un user agent (idealmente el package) para poder
        // descargar los tiles de OpenStreetMap sin que el servidor los rechace.
        Configuration.getInstance().apply {
            load(this@FriendZoneApp, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}
