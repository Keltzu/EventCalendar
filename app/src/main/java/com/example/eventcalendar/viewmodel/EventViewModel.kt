package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EventState {
    object Idle : EventState()
    object Loading : EventState()
    data class Success(val events: List<Event>) : EventState()
    data class Error(val message: String) : EventState()
}

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _eventState = MutableStateFlow<EventState>(EventState.Idle)
    val eventState: StateFlow<EventState> = _eventState

    private val currentUserId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            eventRepository.getEventsFlow(currentUserId)
                .collect { events ->
                    _eventState.value = EventState.Success(events)
                }
        }
    }

    fun addPersonalEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.addPersonalEvent(currentUserId, event)
                .onFailure {
                    _eventState.value = EventState.Error(
                        it.message ?: "Virhe tapahtuman lisäämisessä"
                    )
                }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.addEvent(event)
                .onFailure {
                    _eventState.value = EventState.Error(
                        it.message ?: "Virhe tapahtuman lisäämisessä"
                    )
                }
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.updateEvent(event)
                .onFailure {
                    _eventState.value = EventState.Error(
                        it.message ?: "Virhe tapahtuman päivityksessä"
                    )
                }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
                .onFailure {
                    _eventState.value = EventState.Error(
                        it.message ?: "Virhe tapahtuman poistamisessa"
                    )
                }
        }
    }
}