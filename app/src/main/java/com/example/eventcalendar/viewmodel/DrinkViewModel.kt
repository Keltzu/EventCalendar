package com.example.eventcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventcalendar.model.DrinkLog
import com.example.eventcalendar.repository.DrinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DrinkViewModel @Inject constructor(
    private val drinkRepository: DrinkRepository
) : ViewModel() {

    private val _drinkCount = MutableStateFlow(0)
    val drinkCount: StateFlow<Int> = _drinkCount

    private val _currentLog = MutableStateFlow<DrinkLog?>(null)
    val currentLog: StateFlow<DrinkLog?> = _currentLog

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _drinkHistory = MutableStateFlow<List<String>>(emptyList())
    val drinkHistory: StateFlow<List<String>> = _drinkHistory

    fun loadDrinkLog(userId: String, eventId: String) {
        viewModelScope.launch {
            drinkRepository.getDrinkLogForEvent(userId, eventId)
                .onSuccess { log ->
                    if (log != null) {
                        _currentLog.value = log
                        _drinkCount.value = log.drinkCount
                        _drinkHistory.value = log.drinkHistory
                    }
                }
        }
    }

    fun incrementDrink() {
        _drinkCount.value++
        val time = SimpleDateFormat("HH:mm", Locale("fi")).format(Date())
        _drinkHistory.value = _drinkHistory.value + "Manuaalinen — $time"
        _isSaved.value = false
    }

    fun incrementDrinkWithQr(qrContent: String) {
        _drinkCount.value++
        val time = SimpleDateFormat("HH:mm", Locale("fi")).format(Date())
        _drinkHistory.value = _drinkHistory.value + "QR-koodi — $time"
        _isSaved.value = false
    }

    fun decrementDrink() {
        if (_drinkCount.value > 0) {
            _drinkCount.value--
            if (_drinkHistory.value.isNotEmpty()) {
                _drinkHistory.value = _drinkHistory.value.dropLast(1)
            }
            _isSaved.value = false
        }
    }

    fun saveDrinkLog(userId: String, eventId: String, eventName: String) {
        viewModelScope.launch {
            val log = DrinkLog(
                id = _currentLog.value?.id ?: "",
                userId = userId,
                eventId = eventId,
                eventName = eventName,
                drinkCount = _drinkCount.value,
                drinkHistory = _drinkHistory.value,
                timestamp = System.currentTimeMillis()
            )
            drinkRepository.saveDrinkLog(log)
                .onSuccess {
                    _currentLog.value = log
                    _isSaved.value = true
                }
        }
    }

    fun resetState() {
        _drinkCount.value = 0
        _currentLog.value = null
        _isSaved.value = false
        _drinkHistory.value = emptyList()
    }
}