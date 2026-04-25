package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.utils.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GeminiState {
    object Idle : GeminiState()
    object Loading : GeminiState()
    data class Success(val recommendation: String) : GeminiState()
    data class Error(val message: String) : GeminiState()
}

@HiltViewModel
class GeminiViewModel @Inject constructor() : ViewModel() {

    private val _geminiState = MutableStateFlow<GeminiState>(GeminiState.Idle)
    val geminiState: StateFlow<GeminiState> = _geminiState

    fun getEventRecommendations(
        events: List<Event>,
        userAttendedEvents: List<String>,
        userInterests: String = ""
    ) {
        viewModelScope.launch {
            _geminiState.value = GeminiState.Loading
            try {
                val attendedTitles = events
                    .filter { userAttendedEvents.contains(it.id) }
                    .joinToString(", ") { it.title }

                val availableEvents = events
                    .filter { !userAttendedEvents.contains(it.id) }
                    .joinToString("\n") { "- ${it.title} (${it.location})" }

                val prompt = """
                    Olet opiskelijatapahtumien suosittelija.
                    Käyttäjä on käynyt näissä tapahtumissa: $attendedTitles
                    ${if (userInterests.isNotEmpty()) "Käyttäjän kiinnostukset: $userInterests" else ""}
                    Saatavilla olevat tapahtumat:
                    $availableEvents
                    Suosittele 3 parasta tapahtumaa lyhyesti suomeksi.
                """.trimIndent()

                val response = GeminiService.generateContent(prompt)
                _geminiState.value = GeminiState.Success(response)

            } catch (e: Exception) {
                android.util.Log.e("Gemini", "Virhe: ${e.message}")
                _geminiState.value = GeminiState.Error(
                    e.message ?: "Virhe suositusten hakemisessa"
                )
            }
        }
    }
}