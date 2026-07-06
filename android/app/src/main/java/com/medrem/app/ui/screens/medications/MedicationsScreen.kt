package com.medrem.app.ui.screens.medications

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medrem.app.data.remote.dto.MedicationDto
import com.medrem.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    onNavigateToAddMedication: () -> Unit,
    onNavigateToEditMedication: (String) -> Unit,
    viewModel: MedicationsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val filtered = viewModel.filteredMedications
    val activeCount = filtered.count { it.isActive }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedRem", fontWeight = FontWeight.Bold, color = PrimaryDark) },
                actions = {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(SecondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, "Profile", tint = PrimaryDark, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddMedication, containerColor = PrimaryTeal, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, "Add Medication")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Search medications...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )

            Spacer(Modifier.height(20.dp))

            // Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Your Cabinet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Text("$activeCount Active", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.MedicalServices, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("No medications yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to add your first medication", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                filtered.forEach { med ->
                    MedicationCard(
                        medication = med,
                        onEdit = { onNavigateToEditMedication(med.id) },
                        onDelete = { viewModel.deleteMedication(med.id) },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun MedicationCard(medication: MedicationDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    val bgColor = if (medication.isPrn) SecondaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerLowest

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Left teal stripe
            Box(Modifier.width(4.dp).fillMaxHeight().background(PrimaryTeal))
            Column(Modifier.weight(1f).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(medication.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = PrimaryDark)
                        Text("${medication.dosage} • ${medication.frequency}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Edit, "Edit", Modifier.size(18.dp)) }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                if (medication.reminderTimes.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("Next Dose", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(medication.reminderTimes.first(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                if (medication.isPrn) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {}, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal.copy(alpha = 0.8f))) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Log dose now")
                    }
                }
            }
        }
    }
}
