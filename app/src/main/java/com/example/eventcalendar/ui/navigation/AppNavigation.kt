package com.example.eventcalendar.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
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
    object DrinkCounter : Screen("drink_counter/{eventId}/{eventName}") {
        fun createRoute(eventId: String, eventName: String) =
            "drink_counter/$eventId/${eventName.replace("/", " ")}"
    }
    object KideApp : Screen("kide_app")
}