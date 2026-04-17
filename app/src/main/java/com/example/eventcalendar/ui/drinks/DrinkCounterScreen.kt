package com.example.eventcalendar.ui.drinks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eventcalendar.viewmodel.DrinkViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkCounterScreen(
    eventId: String,
    eventName: String,
    onNavigateBack: () -> Unit,
    viewModel: DrinkViewModel = hiltViewModel()
) {
    val drinkCount by viewModel.drinkCount.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        viewModel.loadDrinkLog(currentUserId, eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juomalaskuri") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = eventName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Merkitse juomasi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // juomat
            Text(
                text = drinkCount.toString(),
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    drinkCount == 0 -> MaterialTheme.colorScheme.onSurface
                    drinkCount <= 3 -> MaterialTheme.colorScheme.primary
                    drinkCount <= 6 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Text(
                text = when {
                    drinkCount == 0 -> "Ei juomia vielä"
                    drinkCount <= 3 -> "🟢 Kohtuullinen"
                    drinkCount <= 6 -> "🟡 Kohtalainen"
                    else -> "🔴 Paljon juotu"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // plus ja miinus nappulat
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // miinus nappi
                FilledTonalButton(
                    onClick = { viewModel.decrementDrink() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "−",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // plussa nappi
                Button(
                    onClick = { viewModel.incrementDrink() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Lisää juoma",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // tallennus nappi
            Button(
                onClick = {
                    viewModel.saveDrinkLog(currentUserId, eventId, eventName)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tallenna")
            }

            if (isSaved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✅ Tallennettu!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "🍺 Muista juoda vastuullisesti. Älä aja autoa alkoholin vaikutuksen alaisena.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}