package com.medrem.app.ui.screens.medications

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    onMedicationAdded: () -> Unit,
    medicationId: String? = null,
    viewModel: AddMedicationViewModel = hiltViewModel(),
) {
    val isEdit = medicationId != null

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) onMedicationAdded()
    }

    LaunchedEffect(medicationId) {
        medicationId?.let { viewModel.loadMedicationForEdit(it) }
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
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(if (isEdit) "Edit Medication" else "Add Medications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("Review and edit the medicines before saving.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))

                // AI Prescription Scan Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                ) {
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(PrimaryTeal, PrimaryDark))).padding(20.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Outlined.DocumentScanner, contentDescription = null, tint = Color.White.copy(0.8f))
                                Spacer(Modifier.width(8.dp))
                                Text("AI Scan (Multiple Meds)", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { launcher.launch("*/*") }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.White.copy(0.5f))) {
                                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
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
                                    enabled = selectedFileUri != null && !viewModel.isLoading,
                                    shape = RoundedCornerShape(12.dp), 
                                     colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.2f))
                                ) {
                                    if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    else { Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Scan", color = Color.White) }
                                }
                            }
                        }
                    }
                }
            }

            items(viewModel.drafts, key = { it.id }) { draft ->
                MedicationDraftCard(
                    draft = draft,
                    onUpdate = { update -> viewModel.updateDraft(draft.id, update) },
                    onRemove = { viewModel.removeDraft(draft.id) }
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedButton(onClick = viewModel::addEmptyDraft, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Another Medicine")
                }
                
                Spacer(Modifier.height(24.dp))

                if (viewModel.error != null) {
                    Text(viewModel.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                }

                Button(
                    onClick = viewModel::addMedication,
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                ) {
                    if (viewModel.isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    else Text("Save All Medications", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val timePickerState = rememberTimePickerState(is24Hour = true)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Select Time (24h)",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(state = timePickerState)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationDraftCard(
    draft: MedicationDraft,
    onUpdate: ((MedicationDraft) -> MedicationDraft) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Medicine Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            }
            
            OutlinedTextField(value = draft.name, onValueChange = { n -> onUpdate { it.copy(name = n) } }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = draft.dosage, onValueChange = { d -> onUpdate { it.copy(dosage = d) } }, label = { Text("Dosage (e.g. 1 tab)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            
            Spacer(Modifier.height(16.dp))
            Text("Frequency", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Once Daily", "Twice Daily", "Thrice Daily").forEach { freq ->
                    FilterChip(
                        selected = draft.frequency == freq,
                        onClick = { 
                            onUpdate { d ->
                                val newTimes = when(freq) {
                                    "Once Daily" -> listOf("08:00")
                                    "Twice Daily" -> listOf("08:00", "20:00")
                                    "Thrice Daily" -> listOf("08:00", "14:00", "20:00")
                                    else -> d.reminderTimes
                                }
                                d.copy(frequency = freq, reminderTimes = newTimes) 
                            } 
                        },
                        label = { Text(freq) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Specific Timings", style = MaterialTheme.typography.labelLarge)
            
            var showTimePicker by remember { mutableStateOf(false) }
            var editingTimeIndex by remember { mutableStateOf<Int?>(null) }

            if (showTimePicker) {
                TimePickerDialog(
                    onDismissRequest = { 
                        showTimePicker = false
                        editingTimeIndex = null
                    },
                    onConfirm = { hour, minute ->
                        val timeStr = String.format("%02d:%01d", hour, minute).replaceFirst(":", ":").let {
                             // Ensure format HH:mm
                             "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                        }
                        onUpdate { d ->
                            val newList = d.reminderTimes.toMutableList()
                            if (editingTimeIndex != null) {
                                newList[editingTimeIndex!!] = timeStr
                            } else {
                                newList.add(timeStr)
                            }
                            d.copy(reminderTimes = newList.sorted())
                        }
                        showTimePicker = false
                        editingTimeIndex = null
                    }
                )
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                draft.reminderTimes.forEachIndexed { index, time ->
                    AssistChip(
                        onClick = { 
                            editingTimeIndex = index
                            showTimePicker = true 
                        },
                        label = { Text(time) },
                        trailingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = "Remove", 
                                modifier = Modifier.size(14.dp).clickable {
                                    onUpdate { d -> d.copy(reminderTimes = d.reminderTimes.filterIndexed { i, _ -> i != index }) }
                                }
                            ) 
                        }
                    )
                }
                AssistChip(
                    onClick = { 
                        editingTimeIndex = null
                        showTimePicker = true 
                    }, 
                    label = { Text("+ Add Time") }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Duration", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = draft.durationType == "custom", onClick = { onUpdate { it.copy(durationType = "custom") } })
                    Text("Custom Days")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = draft.durationType == "permanent", onClick = { onUpdate { it.copy(durationType = "permanent") } })
                    Text("Permanent")
                }
            }
            if (draft.durationType == "custom") {
                OutlinedTextField(value = draft.durationDays, onValueChange = { d -> onUpdate { it.copy(durationDays = d) } }, label = { Text("Number of Days") }, modifier = Modifier.width(150.dp), shape = RoundedCornerShape(12.dp), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            }

            if (draft.interactionWarning != null) {
                Spacer(Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(draft.interactionWarning!!, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
