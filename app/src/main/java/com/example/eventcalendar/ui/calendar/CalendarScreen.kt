package com.example.eventcalendar.ui.calendar

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eventcalendar.model.Event
import com.example.eventcalendar.viewmodel.EventState
import com.example.eventcalendar.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Event) -> Unit,
    onNavigateToMap: (Event) -> Unit,
    onNavigateToDrinkCounter: (Event) -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val eventState by viewModel.eventState.collectAsState()
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }

    val events = when (val state = eventState) {
        is EventState.Success -> state.events
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalenteri", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Takaisin")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddEvent) {
                        Icon(Icons.Default.Add, contentDescription = "Lisää tapahtuma")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Kuukausi header gradientilla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Edellinen kuukausi",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale("fi")).format(currentMonth.time),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Seuraava kuukausi",
                            tint = Color.White
                        )
                    }
                }
            }

            // Viikonpäivät
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                listOf("Ma", "Ti", "Ke", "To", "Pe", "La", "Su").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Kalenteripäivät
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                events = events,
                onDateSelected = { selectedDate = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Valitun päivän tapahtumat
            val selectedDateEvents = events.filter { event ->
                val eventCal = Calendar.getInstance().apply {
                    timeInMillis = event.startTime
                }
                eventCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                        eventCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = SimpleDateFormat("d. MMMM", Locale("fi")).format(selectedDate.time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${selectedDateEvents.size} tapahtumaa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedDateEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📅", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ei tapahtumia tänä päivänä",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(selectedDateEvents) { event ->
                        CalendarEventCard(
                            event = event,
                            onEditClick = onNavigateToEditEvent,
                            onShowMapClick = onNavigateToMap,
                            onDrinkCounterClick = onNavigateToDrinkCounter
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar,
    events: List<Event>,
    onDateSelected: (Calendar) -> Unit
) {
    val firstDay = (currentMonth.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    var firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 2
    if (firstDayOfWeek < 0) firstDayOfWeek = 6

    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1

                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(44.dp))
                    } else {
                        val dayCalendar = (currentMonth.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, day)
                        }

                        val isSelected = selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                                selectedDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                selectedDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)

                        val isToday = Calendar.getInstance().let {
                            it.get(Calendar.DAY_OF_MONTH) == day &&
                                    it.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                    it.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                        }

                        val hasEvent = events.any { event ->
                            val eventCal = Calendar.getInstance().apply {
                                timeInMillis = event.startTime
                            }
                            eventCal.get(Calendar.DAY_OF_MONTH) == day &&
                                    eventCal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                    eventCal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(dayCalendar) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasEvent) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventCard(
    event: Event,
    onEditClick: (Event) -> Unit,
    onShowMapClick: (Event) -> Unit,
    onDrinkCounterClick: (Event) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (event.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = event.location.take(25),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (event.startTime > 0) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale("fi")).format(java.util.Date(event.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Juomalaskuri nappi
            IconButton(onClick = { onDrinkCounterClick(event) }) {
                Text(text = "🍺", fontSize = 18.sp)
            }

            // Karttanappi
            if (event.location.isNotEmpty()) {
                IconButton(onClick = {
                    if (event.latitude != 0.0 && event.longitude != 0.0) {
                        val uri = Uri.parse("geo:${event.latitude},${event.longitude}?q=${Uri.encode(event.location)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webUri = Uri.parse("https://maps.google.com/?q=${Uri.encode(event.location)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    } else {
                        val uri = Uri.parse("https://maps.google.com/?q=${Uri.encode(event.location)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Näytä kartalla",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Muokkaa nappi
            IconButton(onClick = { onEditClick(event) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Muokkaa",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}