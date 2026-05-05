package com.example.eventcalendar.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "eventcalendar_prefs",
        Context.MODE_PRIVATE
    )

    // Viimeisin valittu kaupunki Kide.appissa
    var lastSelectedCity: String
        get() = prefs.getString("last_city", "Kaikki") ?: "Kaikki"
        set(value) = prefs.edit().putString("last_city", value).apply()

    // AI-suositusten kiinnostukset
    var userInterests: String
        get() = prefs.getString("user_interests", "") ?: ""
        set(value) = prefs.edit().putString("user_interests", value).apply()

    // Onko käyttäjä kirjautunut aiemmin
    var hasLoggedInBefore: Boolean
        get() = prefs.getBoolean("has_logged_in", false)
        set(value) = prefs.edit().putBoolean("has_logged_in", value).apply()
}