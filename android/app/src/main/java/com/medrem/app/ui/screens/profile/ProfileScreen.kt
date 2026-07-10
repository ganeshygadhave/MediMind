package com.medrem.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medrem.app.ui.theme.*
import com.medrem.app.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAllergyDialog by remember { mutableStateOf(false) }
    var historyInput by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.historySaved) {
        if (uiState.historySaved) {
            snackbarHostState.showSnackbar("Medical history summarized and saved!")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
                TopAppBar(
                    title = { Text("MediMind", fontWeight = FontWeight.Bold, color = PrimaryDark) },
                    actions = { IconButton(onClick = onNavigateToSettings) { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}, containerColor = PrimaryTeal, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI")
                }
            }
        ) { padding ->
            if (uiState.isLoading || uiState.isSavingHistory) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryTeal)
                        if (uiState.isSavingHistory) {
                            Spacer(Modifier.height(8.dp))
                            Text("Summarizing medical history...")
                        }
                    }
                }
            } else {
            val user = uiState.user
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Photo
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(Modifier.size(100.dp).clip(CircleShape).background(SecondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = PrimaryDark)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(user?.fullName ?: "User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("Daily Adherence: 85%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showEditDialog = true }, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Edit Profile")
                }

                Spacer(Modifier.height(20.dp))

                // Contact Information
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { 
                            Icon(imageVector = Icons.Default.ContactPage, contentDescription = null, tint = PrimaryTeal)
                            Spacer(Modifier.width(8.dp))
                            Text("Contact Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) 
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { 
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp), tint = PrimaryTeal)
                            Spacer(Modifier.width(8.dp))
                            Column { 
                                Text("Phone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(user?.phone ?: "Not set", style = MaterialTheme.typography.bodyMedium) 
                            } 
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { 
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp), tint = PrimaryTeal)
                            Spacer(Modifier.width(8.dp))
                            Column { 
                                Text("Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium) 
                            } 
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Blood Type & Allergies Row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { 
                                Icon(imageVector = Icons.Default.Bloodtype, contentDescription = null, modifier = Modifier.size(18.dp), tint = StatusMissed)
                                Spacer(Modifier.width(4.dp))
                                Text("Blood", style = MaterialTheme.typography.labelMedium, color = StatusMissed) 
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(user?.bloodType ?: "N/A", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp), tint = StatusSkipped)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Allergies", style = MaterialTheme.typography.labelMedium, color = StatusSkipped) 
                                }
                                IconButton(onClick = { showAllergyDialog = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = StatusSkipped)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            val allergies = user?.allergies ?: emptyList()
                            if (allergies.isEmpty()) Text("None", style = MaterialTheme.typography.bodyMedium)
                            else allergies.take(2).forEach { allergy ->
                                Surface(Modifier.padding(bottom = 4.dp), shape = RoundedCornerShape(8.dp), color = PrimaryTeal) {
                                    Text(allergy, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Medical History
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { 
                            Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = PrimaryTeal)
                            Spacer(Modifier.width(8.dp))
                            Text("Medical History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) 
                        }
                        Spacer(Modifier.height(12.dp))
                        val conditions = user?.medicalConditions ?: emptyList()
                        if (conditions.isEmpty()) Text("No conditions recorded", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else conditions.forEach { condition ->
                            Row(Modifier.padding(bottom = 8.dp)) {
                                Box(Modifier.padding(top = 6.dp).size(8.dp).background(PrimaryTeal, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Column { Text(condition["name"] ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium); Text(condition["description"] ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                        
                        OutlinedTextField(
                            value = historyInput,
                            onValueChange = { historyInput = it },
                            label = { Text("Describe your medical/surgical history") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("e.g. Diagnosed with hypertension in 2021. Had appendix removed in 2023.") }
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (historyInput.isNotBlank()) {
                                    viewModel.saveMedicalHistory(historyInput)
                                    historyInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("AI Summarize & Save")
                        }
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
            
            // Edit Profile Dialog
            if (showEditDialog && user != null) {
                var editName by remember { mutableStateOf(user.fullName) }
                var editPhone by remember { mutableStateOf(user.phone ?: "") }
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Edit Profile") },
                    text = {
                        Column {
                            OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                    },
                    confirmButton = { Button(onClick = { showEditDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)) { Text("Save") } }
                )
            }

            // Allergy Edit Dialog
            if (showAllergyDialog && user != null) {
                var newAllergy by remember { mutableStateOf("") }
                val allergyList = remember { mutableStateListOf(*user.allergies.toTypedArray()) }
                AlertDialog(
                    onDismissRequest = { showAllergyDialog = false },
                    title = { Text("Manage Allergies") },
                    text = {
                        Column {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(value = newAllergy, onValueChange = { newAllergy = it }, placeholder = { Text("Add allergy...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                IconButton(onClick = { if (newAllergy.isNotBlank()) { allergyList.add(newAllergy); newAllergy = "" } }) { Icon(Icons.Default.Add, null, tint = PrimaryTeal) }
                            }
                            Spacer(Modifier.height(12.dp))
                            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                                allergyList.forEach { allergy ->
                                    InputChip(
                                        selected = true,
                                        onClick = { allergyList.remove(allergy) },
                                        label = { Text(allergy) },
                                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = { Button(onClick = { showAllergyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)) { Text("Done") } }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}
