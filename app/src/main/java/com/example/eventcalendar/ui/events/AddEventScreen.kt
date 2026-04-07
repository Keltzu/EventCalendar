package com.example.eventcalendar.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun AddEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("fi"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("fi"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lisää tapahtuma") },
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
                label = { Text("Päivämäärä (pp.kk.vvvv) *") },
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
                label = { Text("Kategoria (esim. Urheilu, Musiikki)") },
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
                    if (title.isEmpty() || date.isEmpty()) {
                        errorMessage = "Nimi ja päivämäärä ovat pakollisia"
                        return@Button
                    }
                    try {
                        val parsedDate = dateFormat.parse(date) ?: throw Exception("Virheellinen päivämäärä")
                        val startTimestamp = if (startTime.isNotEmpty()) {
                            val parsedTime = timeFormat.parse(startTime)!!
                            val cal = Calendar.getInstance().apply {
                                time = parsedDate
                                set(Calendar.HOUR_OF_DAY, parsedTime.hours)
                                set(Calendar.MINUTE, parsedTime.minutes)
                            }
                            cal.timeInMillis
                        } else parsedDate.time

                        val endTimestamp = if (endTime.isNotEmpty()) {
                            val parsedTime = timeFormat.parse(endTime)!!
                            val cal = Calendar.getInstance().apply {
                                time = parsedDate
                                set(Calendar.HOUR_OF_DAY, parsedTime.hours)
                                set(Calendar.MINUTE, parsedTime.minutes)
                            }
                            cal.timeInMillis
                        } else startTimestamp + 3600000

                        val event = Event(
                            title = title,
                            description = description,
                            location = location,
                            startTime = startTimestamp,
                            endTime = endTimestamp,
                            category = category
                        )
                        viewModel.addEvent(event)
                        onNavigateBack()
                    } catch (e: Exception) {
                        errorMessage = "Tarkista päivämäärä ja aika formaatti"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tallenna tapahtuma")
            }
        }
    }
}