package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StreakState {
    object Idle : StreakState()
    object Loading : StreakState()
    object Success : StreakState()
    data class Error(val message: String) : StreakState()
}

@HiltViewModel
class StreakViewModel @Inject constructor(
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _streakState = MutableStateFlow<StreakState>(StreakState.Idle)
    val streakState: StateFlow<StreakState> = _streakState

    fun checkIn(userId: String, eventId: String) {
        viewModelScope.launch {
            _streakState.value = StreakState.Loading
            streakRepository.checkIn(userId, eventId)
                .onSuccess {
                    _streakState.value = StreakState.Success
                }
                .onFailure {
                    _streakState.value = StreakState.Error(
                        it.message ?: "Kirjautuminen epäonnistui"
                    )
                }
        }
    }

    fun resetState() {
        _streakState.value = StreakState.Idle
    }
}