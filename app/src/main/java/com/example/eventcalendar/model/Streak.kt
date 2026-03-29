package com.example.eventcalendar.model

data class Streak(
    val uid: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCheckIn: Long = 0L,
    val totalPoints: Int = 0,
    val weeklyPoints: Int = 0
)