package com.example.eventcalendar.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object AddEvent : Screen("add_event")
    object Leaderboard : Screen("leaderboard")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
    object Map : Screen("map/{locationName}/{latitude}/{longitude}") {
        fun createRoute(locationName: String, latitude: Double, longitude: Double) =
            "map/${locationName.replace("/", " ")}/$latitude/$longitude"
    }
}