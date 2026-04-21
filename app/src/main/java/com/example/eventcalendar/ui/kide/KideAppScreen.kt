package com.example.eventcalendar.ui.kide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.viewmodel.KideAppState
import com.example.eventcalendar.viewmodel.KideAppViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.FilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KideAppScreen(
    onNavigateBack: () -> Unit,
    viewModel: KideAppViewModel = hiltViewModel()
) {
    val state by viewModel.kideAppState.collectAsState()
    val addedEvents by viewModel.addedEvents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Kaikki") }

    val cities = listOf(
        "Kaikki", "Oulu", "Helsinki", "Tampere",
        "Turku", "Jyväskylä", "Kuopio", "Vaasa"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kide.app tapahtumat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Takaisin")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Hakukenttä
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Hae tapahtumia") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { },
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hae")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kaupunki suodattimet
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cities) { city ->
                    FilterChip(
                        selected = selectedCity == city,
                        onClick = {
                            selectedCity = city
                            val cityParam = if (city == "Kaikki") "" else city.lowercase()
                            viewModel.searchEvents(searchQuery, cityParam)
                        },
                        label = { Text(city) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hae kaikki nappi
            OutlinedButton(
                onClick = {
                    selectedCity = "Kaikki"
                    viewModel.searchEvents()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Näytä kaikki tapahtumat")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val currentState = state) {
                is KideAppState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎉",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hae Kide.app tapahtumia",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Valitse kaupunki tai hae nimellä",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is KideAppState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Haetaan tapahtumia...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is KideAppState.Success -> {
                    // tapahtumahaku
                    val filteredEvents = if (searchQuery.isEmpty()) {
                        currentState.events
                    } else {
                        currentState.events.filter { event ->
                            event.title.contains(searchQuery, ignoreCase = true) ||
                                    event.location.contains(searchQuery, ignoreCase = true) ||
                                    event.description.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredEvents.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ei tapahtumia löydetty",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "${filteredEvents.size} tapahtumaa löydetty",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredEvents) { event ->
                                KideEventCard(
                                    event = event,
                                    isAdded = addedEvents.contains(event.id),
                                    onAddClick = { viewModel.addEventToCalendar(event) }
                                )
                            }
                        }
                    }
                }
                is KideAppState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "❌",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val cityParam = if (selectedCity == "Kaikki") "" else selectedCity.lowercase()
                                    viewModel.searchEvents(searchQuery, cityParam)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Yritä uudelleen")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KideEventCard(
    event: Event,
    isAdded: Boolean,
    onAddClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("fi"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.location.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 ${event.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (event.startTime > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🕐 ${dateFormat.format(Date(event.startTime))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isAdded) {
                    FilledTonalButton(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lisätty")
                    }
                } else {
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lisää")
                    }
                }
            }

            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = { },
                label = { Text("Kide.app", style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}