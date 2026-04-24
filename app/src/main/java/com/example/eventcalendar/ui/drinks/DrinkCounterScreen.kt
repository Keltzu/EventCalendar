package com.example.eventcalendar.ui.drinks

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

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
    val drinkHistory by viewModel.drinkHistory.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        viewModel.loadDrinkLog(currentUserId, eventId)
    }

    // QR-skanneri launcheri
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.incrementDrinkWithQr(result.contents)
        }
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
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(modifier = Modifier.height(24.dp))

            // juomien määrä
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

            Spacer(modifier = Modifier.height(24.dp))

            // plussa ja miinus napit
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { viewModel.decrementDrink() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "−", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.incrementDrink() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "+", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QR-skannausnappula
            OutlinedButton(
                onClick = {
                    val options = ScanOptions().apply {
                        setPrompt("Skannaa juoman QR-koodi")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    scanLauncher.launch(options)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Skannaa QR-koodi")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // tallennus nappi
            Button(
                onClick = {
                    viewModel.saveDrinkLog(currentUserId, eventId, eventName)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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

            // juomahistoria
            if (drinkHistory.isNotEmpty()) {
                Text(
                    text = "Juomahistoria",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(drinkHistory.reversed()) { drink ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🍺 Juoma #${drinkHistory.indexOf(drink) + 1}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = drink,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

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