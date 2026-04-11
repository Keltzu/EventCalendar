package com.example.eventcalendar.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: Event,
    onNavigateBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var location by remember { mutableStateOf(event.location) }
    var category by remember { mutableStateOf(event.category) }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("fi"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("fi"))

    var date by remember {
        mutableStateOf(dateFormat.format(Date(event.startTime)))
    }
    var startTime by remember {
        mutableStateOf(timeFormat.format(Date(event.startTime)))
    }
    var endTime by remember {
        mutableStateOf(timeFormat.format(Date(event.endTime)))
    }

    // Poistodialoogi
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Poista tapahtuma") },
            text = { Text("Haluatko varmasti poistaa tapahtuman '${event.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent(event.id)
                        onNavigateBack()
                    }
                ) {
                    Text("Poista", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Peruuta")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muokkaa tapahtumaa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Takaisin")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Poista tapahtuma",
                            tint = MaterialTheme.colorScheme.error
                        )
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tapahtuman nimi *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Kuvaus") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Paikka") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Päivämäärä (pp.kk.vvvv)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Alkaa (tt:mm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Loppuu (tt:mm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategoria") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (title.isEmpty()) {
                        errorMessage = "Nimi on pakollinen"
                        return@Button
                    }
                    try {
                        val parsedDate = dateFormat.parse(date) ?: throw Exception("Virheellinen päivämäärä")
                        val parsedStart = timeFormat.parse(startTime)!!
                        val parsedEnd = timeFormat.parse(endTime)!!

                        val startCal = Calendar.getInstance().apply {
                            time = parsedDate
                            set(Calendar.HOUR_OF_DAY, parsedStart.hours)
                            set(Calendar.MINUTE, parsedStart.minutes)
                        }
                        val endCal = Calendar.getInstance().apply {
                            time = parsedDate
                            set(Calendar.HOUR_OF_DAY, parsedEnd.hours)
                            set(Calendar.MINUTE, parsedEnd.minutes)
                        }

                        val updatedEvent = event.copy(
                            title = title,
                            description = description,
                            location = location,
                            category = category,
                            startTime = startCal.timeInMillis,
                            endTime = endCal.timeInMillis
                        )
                        viewModel.updateEvent(updatedEvent)
                        onNavigateBack()
                    } catch (e: Exception) {
                        errorMessage = "Tarkista päivämäärä ja aika formaatti"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tallenna muutokset")
            }
        }
    }
}