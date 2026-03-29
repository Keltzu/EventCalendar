package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.repository.EventRepository
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

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            eventRepository.getEvents()
                .onSuccess { _eventState.value = EventState.Success(it) }
                .onFailure { _eventState.value = EventState.Error(it.message ?: "Virhe tapahtumien lataamisessa") }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.addEvent(event)
                .onSuccess { loadEvents() }
                .onFailure { _eventState.value = EventState.Error(it.message ?: "Virhe tapahtuman lisäämisessä") }
        }
    }
}