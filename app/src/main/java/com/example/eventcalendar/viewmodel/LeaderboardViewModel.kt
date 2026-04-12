package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.model.User
import com.example.eventcalendar.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LeaderboardState {
    object Idle : LeaderboardState()
    object Loading : LeaderboardState()
    data class Success(val users: List<User>) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}

enum class LeaderboardFilter {
    ALL_TIME, WEEKLY
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Idle)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState

    private val _currentFilter = MutableStateFlow(LeaderboardFilter.ALL_TIME)
    val currentFilter: StateFlow<LeaderboardFilter> = _currentFilter

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _leaderboardState.value = LeaderboardState.Loading
            when (_currentFilter.value) {
                LeaderboardFilter.ALL_TIME -> {
                    leaderboardRepository.getTopUsers()
                        .onSuccess { _leaderboardState.value = LeaderboardState.Success(it) }
                        .onFailure { _leaderboardState.value = LeaderboardState.Error(it.message ?: "Virhe") }
                }
                LeaderboardFilter.WEEKLY -> {
                    leaderboardRepository.getWeeklyTopUsers()
                        .onSuccess { _leaderboardState.value = LeaderboardState.Success(it) }
                        .onFailure { _leaderboardState.value = LeaderboardState.Error(it.message ?: "Virhe") }
                }
            }
        }
    }

    fun setFilter(filter: LeaderboardFilter) {
        _currentFilter.value = filter
        loadLeaderboard()
    }
}