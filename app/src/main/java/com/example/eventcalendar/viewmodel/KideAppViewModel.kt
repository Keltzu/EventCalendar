package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.repository.EventRepository
import com.example.eventcalendar.repository.KideAppRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.eventcalendar.utils.NominatimService
import com.example.eventcalendar.utils.PreferencesManager

sealed class KideAppState {
    object Idle : KideAppState()
    object Loading : KideAppState()
    data class Success(val events: List<Event>) : KideAppState()
    data class Error(val message: String) : KideAppState()
}

@HiltViewModel
class KideAppViewModel @Inject constructor(
    private val kideAppRepository: KideAppRepository,
    private val eventRepository: EventRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _kideAppState = MutableStateFlow<KideAppState>(KideAppState.Idle)
    val kideAppState: StateFlow<KideAppState> = _kideAppState

    private val _addedEvents = MutableStateFlow<Set<String>>(emptySet())
    val addedEvents: StateFlow<Set<String>> = _addedEvents

    init {
        // hateaan viimeksi valittu kaupunki
        val lastCity = preferencesManager.lastSelectedCity
        if (lastCity != "Kaikki") {
            searchEvents(city = lastCity.lowercase())
        }
    }

    fun searchEvents(query: String = "", city: String = "") {
        viewModelScope.launch {
            // Tallennetaan valittu kaupunki
            if (city.isNotEmpty()) {
                preferencesManager.lastSelectedCity = city.replaceFirstChar { it.uppercase() }
            } else {
                preferencesManager.lastSelectedCity = "Kaikki"
            }

            _kideAppState.value = KideAppState.Loading
            kideAppRepository.getKideEvents(query, city)
                .onSuccess { events ->
                    _kideAppState.value = KideAppState.Success(events)
                }
                .onFailure {
                    _kideAppState.value = KideAppState.Error(
                        it.message ?: "Virhe tapahtumien haussa"
                    )
                }
        }
    }

    fun addEventToCalendar(event: Event) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                _kideAppState.value = KideAppState.Error("Kirjaudu ensin sisään")
                return@launch
            }

            val eventWithCoords = if (event.latitude == 0.0 && event.longitude == 0.0 && event.location.isNotEmpty()) {
                try {
                    val results = NominatimService.api.searchPlace(event.location)
                    if (results.isNotEmpty()) {
                        event.copy(
                            latitude = results.first().lat.toDoubleOrNull() ?: 0.0,
                            longitude = results.first().lon.toDoubleOrNull() ?: 0.0
                        )
                    } else event
                } catch (e: Exception) {
                    event
                }
            } else event

            eventRepository.addPersonalEvent(userId, eventWithCoords)
                .onSuccess {
                    _addedEvents.value = _addedEvents.value + event.id
                    android.util.Log.d("KideApp", "Tapahtuma lisätty: ${event.title}")
                }
                .onFailure {
                    android.util.Log.e("KideApp", "Virhe: ${it.message}")
                    _kideAppState.value = KideAppState.Error(
                        it.message ?: "Virhe tapahtuman lisäämisessä"
                    )
                }
        }
    }
}