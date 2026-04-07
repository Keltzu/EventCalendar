package com.example.eventcalendar.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object AddEvent : Screen("add_event")
}