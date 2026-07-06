package com.medrem.app.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medrem.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryTeal) }
            } else if (uiState.report != null) {
                val report = uiState.report
                Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(report.reportType.replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                OutlinedButton(onClick = { uriHandler.openUri(report.fileUrl) }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.OpenInNew, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open Original Document")
                }

                Spacer(Modifier.height(20.dp))

                // AI Summary Section
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = PrimaryTeal)
                            Spacer(Modifier.width(8.dp))
                            Text("AI Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        if (report.aiSummary != null) {
                            Text(report.aiSummary, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Button(onClick = viewModel::summarize, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal), enabled = !uiState.isSummarizing) {
                                if (uiState.isSummarizing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                else { Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Generate Summary") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Extract Medicines Button
                Button(onClick = viewModel::extractMedicines, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal), enabled = !uiState.isExtracting) {
                    if (uiState.isExtracting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else { Icon(Icons.Default.MedicalServices, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Extract Medicines from Report") }
                }
            }
        }
    }
}
