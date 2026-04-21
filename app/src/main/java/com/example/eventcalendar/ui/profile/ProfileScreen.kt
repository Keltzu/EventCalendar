package com.example.eventcalendar.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.model.User
import com.example.eventcalendar.viewmodel.AuthViewModel
import com.example.eventcalendar.viewmodel.EventState
import com.example.eventcalendar.viewmodel.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val eventState by eventViewModel.eventState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val uid = currentUser?.uid ?: return@LaunchedEffect
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()
            userData = doc.toObject(User::class.java)
        } catch (e: Exception) { }
        isLoading = false
    }

    val allEvents = when (val state = eventState) {
        is EventState.Success -> state.events
        else -> emptyList()
    }

    val now = System.currentTimeMillis()
    val attendedEventIds = userData?.attendedEvents ?: emptyList()

    // Tulevat tapahtumat joihin ilmoittautunut
    val upcomingAttended = allEvents.filter { event ->
        attendedEventIds.contains(event.id) && event.startTime > now
    }.sortedBy { it.startTime }

    // Menneet tapahtumat joissa käynyt
    val pastAttended = allEvents.filter { event ->
        attendedEventIds.contains(event.id) && event.startTime <= now
    }.sortedByDescending { it.startTime }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profiili") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Takaisin")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Kirjaudu ulos")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Avatar ja nimi
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userData?.displayName?.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = userData?.displayName ?: "Tuntematon",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tilastokortit
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Pisteet",
                            value = "${userData?.points ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Streak",
                            value = "🔥 ${userData?.streak ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Tapahtumat",
                            value = "${attendedEventIds.size}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Viikkopisteet",
                            value = "${userData?.weekly_points ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pisin streak",
                            value = "🏆 ${userData?.longestStreak ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Välilehdet
                item {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Tulevat (${upcomingAttended.size})") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Menneet (${pastAttended.size})") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tapahtumalista
                val eventsToShow = if (selectedTab == 0) upcomingAttended else pastAttended

                if (eventsToShow.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0) "Ei tulevia tapahtumia" else "Ei menneitä tapahtumia",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(eventsToShow) { event ->
                        ProfileEventCard(event = event)
                    }
                }

                // Kirjaudu ulos nappi
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirjaudu ulos")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEventCard(event: Event) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("fi"))
    val now = System.currentTimeMillis()
    val isPast = event.startTime <= now

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (event.startTime > 0)
                        dateFormat.format(Date(event.startTime))
                    else "Aika ei tiedossa",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPast)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
            if (isPast) {
                Text(
                    text = "✅",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}