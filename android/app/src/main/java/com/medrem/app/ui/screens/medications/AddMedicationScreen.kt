package com.medrem.app.ui.screens.medications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.medrem.app.ui.theme.*
import com.medrem.app.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    onMedicationAdded: () -> Unit,
    medicationId: String? = null,
    viewModel: AddMedicationViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val isEdit = medicationId != null

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onMedicationAdded()
    }

    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = FileUtils.getFilePathFromUri(context, it)?.substringAfterLast("/")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedRem", fontWeight = FontWeight.Bold, color = PrimaryDark) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(if (isEdit) "Edit Medication" else "Add New Medication", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Enter details manually or use AI to extract from a prescription.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(20.dp))

            // AI Prescription Scan Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(PrimaryTeal, PrimaryDark))).padding(20.dp)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DocumentScanner, null, tint = Color.White.copy(0.8f))
                            Spacer(Modifier.width(8.dp))
                            Text("AI Prescription Scan", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { launcher.launch("*/*") }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.White.copy(0.5f))) {
                                Icon(Icons.Default.Upload, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(selectedFileName ?: "Upload")
                            }
                            Button(
                                onClick = { 
                                    selectedFileUri?.let { uri ->
                                        val path = FileUtils.getFilePathFromUri(context, uri)
                                        if (path != null) viewModel.uploadAndExtract(path) 
                                    }
                                }, 
                                enabled = selectedFileUri != null,
                                shape = RoundedCornerShape(12.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.2f))
                            ) {
                                Icon(Icons.Outlined.MedicalServices, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Extract", color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Divider with text
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text(" Manual Entry ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            // Form Fields
            Text("Medicine Name", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = uiState.name, onValueChange = { viewModel.onFieldChange("name", it) }, placeholder = { Text("e.g. Lisinopril") }, trailingIcon = { Icon(Icons.Outlined.MedicalServices, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)

            Spacer(Modifier.height(16.dp))
            Text("Dosage", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = uiState.dosage, onValueChange = { viewModel.onFieldChange("dosage", it) }, placeholder = { Text("e.g. 10mg / 1 tablet") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Frequency", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = uiState.frequency, onValueChange = { viewModel.onFieldChange("frequency", it) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
                }
                Column(Modifier.weight(1f)) {
                    Text("Reminder Time", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = uiState.reminderTime, onValueChange = { viewModel.onFieldChange("reminderTime", it) }, trailingIcon = { Icon(Icons.Outlined.Schedule, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Duration", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = uiState.durationType == "permanent", onClick = { viewModel.onFieldChange("durationType", "permanent") }, label = { Text("Permanent") }, shape = RoundedCornerShape(12.dp))
                FilterChip(selected = uiState.durationType == "custom", onClick = { viewModel.onFieldChange("durationType", "custom") }, label = { Text("Custom") }, shape = RoundedCornerShape(12.dp))
            }

            // Error
            AnimatedVisibility(visible = uiState.error != null) {
                Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = viewModel::addMedication,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEdit) "Update Medication" else "Add Medication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
