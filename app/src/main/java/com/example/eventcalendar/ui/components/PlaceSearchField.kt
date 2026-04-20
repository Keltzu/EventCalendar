package com.example.eventcalendar.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventcalendar.utils.NominatimResult
import com.example.eventcalendar.utils.NominatimService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlaceSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (NominatimResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                if (newValue.length >= 4) {
                    scope.launch {
                        isSearching = true
                        delay(1000)
                        try {
                            val results = NominatimService.api.searchPlace(newValue)
                            suggestions = results
                            android.util.Log.d("PlaceSearch", "Tuloksia: ${results.size}")
                        } catch (e: Exception) {
                            android.util.Log.e("PlaceSearch", "Virhe: ${e.message}")
                            suggestions = emptyList()
                        }
                        isSearching = false
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            label = { Text("Paikka") },
            singleLine = true,
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Text(
                            text = suggestion.display_name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(suggestion.display_name)
                                    onPlaceSelected(suggestion)
                                    suggestions = emptyList()
                                }
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}