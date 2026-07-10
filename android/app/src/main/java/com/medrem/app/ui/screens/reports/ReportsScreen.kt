package com.medrem.app.ui.screens.reports

import android.Manifest
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.medrem.app.data.remote.dto.ReportDto
import com.medrem.app.ui.theme.*
import com.medrem.app.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToReportDetail: (String) -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.addImagesToQueue(uris)
    }

    var tempImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showPermissionDeniedMsg by remember { mutableStateOf(false) }
    var reportToRename by remember { mutableStateOf<ReportDto?>(null) }
    var renameText by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.addImagesToQueue(listOf(tempImageUri!!))
        }
    }

    // Request CAMERA permission at runtime (required Android 6.0+)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = FileUtils.createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            showPermissionDeniedMsg = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reports", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { 
                        showPermissionDeniedMsg = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }, 
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) { Icon(Icons.Default.CameraAlt, "Camera") }
                
                Spacer(Modifier.height(8.dp))
                
                FloatingActionButton(
                    onClick = { galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, 
                    containerColor = PrimaryTeal, 
                    contentColor = Color.White, 
                    shape = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Default.AddPhotoAlternate, "Add Photos") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Camera permission denied warning
            if (showPermissionDeniedMsg) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Camera permission denied. Please allow camera access in Settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // New Image Queue Section
            if (uiState.pendingImages.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Selected Pages (${uiState.pendingImages.size}/5)", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.pendingImages.forEach { uri ->
                                Box(Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)) {
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeImageFromQueue(uri) },
                                        modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.uploadQueue(context) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            enabled = !uiState.isUploading
                        ) {
                            if (uiState.isUploading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                            else Text("Proceed to AI Summary")
                        }
                    }
                }
            }

            Text("Reports Library", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Your medical records and prescriptions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryTeal) }
            } else if (uiState.reports.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Description, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("No reports yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                uiState.reports.forEach { report ->
                    ReportCard(
                        report = report,
                        onClick = { onNavigateToReportDetail(report.id) },
                        onDelete = { viewModel.deleteReport(report.id) },
                        onRename = {
                            reportToRename = report
                            renameText = report.title
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (reportToRename != null) {
                AlertDialog(
                    onDismissRequest = { reportToRename = null },
                    title = { Text("Rename Report") },
                    text = {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            label = { Text("Report Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (renameText.isNotBlank()) {
                                    viewModel.renameReport(reportToRename!!.id, renameText)
                                }
                                reportToRename = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { reportToRename = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReportCard(
    report: ReportDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Description, null, tint = PrimaryTeal, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(report.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(report.reportType.replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (report.aiSummary != null) {
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = PrimaryTeal.copy(alpha = 0.1f)) {
                        Text("AI Summary Available", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = PrimaryTeal)
                    }
                }
            }
            IconButton(onClick = onRename) { Icon(Icons.Outlined.Edit, "Rename", tint = PrimaryTeal) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}
