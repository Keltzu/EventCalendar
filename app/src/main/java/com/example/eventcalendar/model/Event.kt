package com.example.eventcalendar.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val category: String = "",
    val attendeeCount: Int = 0,
    val imageUrl: String = ""
)