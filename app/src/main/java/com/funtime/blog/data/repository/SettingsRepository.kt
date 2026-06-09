package com.funtime.blog.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.funtime.blog.data.local.ReadingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val fontSize: Flow<Int> = dataStore.data.map {
        it[ReadingPreferences.FONT_SIZE] ?: ReadingPreferences.DEFAULT_FONT_SIZE
    }
    val darkMode: Flow<String> = dataStore.data.map {
        it[ReadingPreferences.DARK_MODE] ?: ReadingPreferences.DEFAULT_DARK_MODE
    }

    suspend fun setFontSize(size: Int) {
        dataStore.edit { it[ReadingPreferences.FONT_SIZE] = size }
    }
    suspend fun setDarkMode(mode: String) {
        dataStore.edit { it[ReadingPreferences.DARK_MODE] = mode }
    }
}
