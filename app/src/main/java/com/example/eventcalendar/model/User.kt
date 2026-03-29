package com.example.eventcalendar.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val points: Int = 0,
    val streak: Int = 0,
    val lastCheckIn: Long = 0L,
    val attendedEvents: List<String> = emptyList(),
    val totalDrinksLogged: Int = 0
)