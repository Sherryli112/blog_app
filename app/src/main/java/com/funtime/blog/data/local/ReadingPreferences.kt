package com.funtime.blog.data.local

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object ReadingPreferences {
    val FONT_SIZE = intPreferencesKey("font_size")
    val DARK_MODE = stringPreferencesKey("dark_mode")
    const val DEFAULT_FONT_SIZE = 100
    const val DEFAULT_DARK_MODE = "system"
}
