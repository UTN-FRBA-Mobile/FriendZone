package com.example.friendzone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.friendzone.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "friendzone_tokens")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val displayNameKey = stringPreferencesKey("display_name")
    private val emailKey = stringPreferencesKey("email")
    private val usernameKey = stringPreferencesKey("username")
    private val locationSharingKey = booleanPreferencesKey("location_sharing_enabled")
    private val loggedInKey = booleanPreferencesKey("logged_in")

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[loggedInKey] == true && !prefs[accessTokenKey].isNullOrBlank()
    }

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        if (prefs[loggedInKey] != true || prefs[accessTokenKey].isNullOrBlank()) {
            null
        } else {
            val id = prefs[userIdKey] ?: return@map null
            User(
                id = id,
                email = prefs[emailKey].orEmpty(),
                username = prefs[usernameKey].orEmpty(),
                displayName = prefs[displayNameKey].orEmpty(),
                fcmToken = null,
                locationSharingEnabled = prefs[locationSharingKey] ?: false,
                createdAt = "",
            )
        }
    }

    suspend fun getAccessToken(): String? = context.dataStore.data.first()[accessTokenKey]

    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[refreshTokenKey]

    suspend fun saveSession(accessToken: String, refreshToken: String, user: User) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
            writeUserProfile(prefs, user)
            prefs[loggedInKey] = true
        }
    }

    suspend fun saveUserProfile(user: User) {
        context.dataStore.edit { prefs ->
            writeUserProfile(prefs, user)
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
            prefs.remove(userIdKey)
            prefs.remove(displayNameKey)
            prefs.remove(emailKey)
            prefs.remove(usernameKey)
            prefs.remove(locationSharingKey)
            prefs[loggedInKey] = false
        }
    }

    private fun writeUserProfile(prefs: MutablePreferences, user: User) {
        prefs[userIdKey] = user.id
        prefs[displayNameKey] = user.displayName
        prefs[emailKey] = user.email
        prefs[usernameKey] = user.username
        prefs[locationSharingKey] = user.locationSharingEnabled
    }
}
