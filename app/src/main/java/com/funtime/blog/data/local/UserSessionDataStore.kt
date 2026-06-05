package com.funtime.blog.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.funtime.blog.data.api.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_session")

data class UserSession(
    val jwt: String,
    val userId: Int,
    val username: String,
    val email: String
)

@Singleton
class UserSessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val JWT_KEY = stringPreferencesKey("jwt")
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val EMAIL_KEY = stringPreferencesKey("email")

    val sessionFlow: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        val jwt = prefs[JWT_KEY] ?: return@map null
        UserSession(
            jwt = jwt,
            userId = prefs[USER_ID_KEY] ?: 0,
            username = prefs[USERNAME_KEY] ?: "",
            email = prefs[EMAIL_KEY] ?: ""
        )
    }

    suspend fun save(jwt: String, user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[JWT_KEY] = jwt
            prefs[USER_ID_KEY] = user.id
            prefs[USERNAME_KEY] = user.username
            prefs[EMAIL_KEY] = user.email
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
