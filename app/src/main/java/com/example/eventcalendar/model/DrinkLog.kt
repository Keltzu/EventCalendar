package com.example.eventcalendar.model

data class DrinkLog(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val drinkCount: Int = 0,
    val timestamp: Long = 0L
)