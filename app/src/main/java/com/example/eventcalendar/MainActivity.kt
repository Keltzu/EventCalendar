package com.example.eventcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventcalendar.ui.auth.LoginScreen
import com.example.eventcalendar.ui.auth.RegisterScreen
import com.example.eventcalendar.ui.calendar.CalendarScreen
import com.example.eventcalendar.ui.events.AddEventScreen
import com.example.eventcalendar.ui.home.HomeScreen
import com.example.eventcalendar.ui.navigation.Screen
import com.example.eventcalendar.ui.theme.EventCalendarTheme
import com.example.eventcalendar.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.eventcalendar.ui.leaderboard.LeaderboardScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.eventcalendar.ui.events.EditEventScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.eventcalendar.viewmodel.EventState
import com.example.eventcalendar.viewmodel.EventViewModel
import androidx.compose.runtime.collectAsState
import com.example.eventcalendar.ui.map.MapScreen
import com.example.eventcalendar.ui.profile.ProfileScreen
import com.example.eventcalendar.ui.drinks.DrinkCounterScreen
import com.example.eventcalendar.ui.kide.KideAppScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventCalendarTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val startDestination = if (authViewModel.isLoggedIn) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            }
                        )
                    }
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            },
                            onNavigateToCalendar = {
                                navController.navigate(Screen.Calendar.route)
                            },
                            onNavigateToLeaderboard = {
                                navController.navigate(Screen.Leaderboard.route)
                            },
                            onNavigateToProfile = {
                                navController.navigate(Screen.Profile.route)
                            },
                            onNavigateToKideApp = {
                                navController.navigate(Screen.KideApp.route)
                            },
                        )
                    }
                    composable(Screen.Calendar.route) {
                        CalendarScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToAddEvent = {
                                navController.navigate(Screen.AddEvent.route)
                            },
                            onNavigateToEditEvent = { event ->
                                navController.navigate(Screen.EditEvent.createRoute(event.id))
                            },
                            onNavigateToMap = { event ->
                                navController.navigate(
                                    Screen.Map.createRoute(
                                        locationName = event.location,
                                        latitude = event.latitude,
                                        longitude = event.longitude
                                    )
                                )
                            },
                            onNavigateToDrinkCounter = { event ->
                                navController.navigate(
                                    Screen.DrinkCounter.createRoute(
                                        eventId = event.id,
                                        eventName = event.title
                                    )
                                )
                            }
                        )
                    }
                    composable(Screen.AddEvent.route) {
                        AddEventScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(Screen.Leaderboard.route) {
                        LeaderboardScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        )
                    }
                    composable(
                        route = Screen.EditEvent.route,
                        arguments = listOf(
                            navArgument("eventId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        val eventViewModel: EventViewModel = hiltViewModel()
                        val eventState = eventViewModel.eventState.collectAsState()
                        val event = (eventState.value as? EventState.Success)?.events?.find { it.id == eventId }

                        event?.let {
                            EditEventScreen(
                                event = it,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(
                        route = Screen.Map.route,
                        arguments = listOf(
                            navArgument("locationName") { type = NavType.StringType },
                            navArgument("latitude") { type = NavType.FloatType },
                            navArgument("longitude") { type = NavType.FloatType }
                        )
                    ) { backStackEntry ->
                        val locationName = backStackEntry.arguments?.getString("locationName") ?: ""
                        val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
                        val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0

                        MapScreen(
                            locationName = locationName,
                            latitude = latitude,
                            longitude = longitude,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = Screen.DrinkCounter.route,
                        arguments = listOf(
                            navArgument("eventId") { type = NavType.StringType },
                            navArgument("eventName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
                        DrinkCounterScreen(
                            eventId = eventId,
                            eventName = eventName,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.KideApp.route) {
                        KideAppScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}