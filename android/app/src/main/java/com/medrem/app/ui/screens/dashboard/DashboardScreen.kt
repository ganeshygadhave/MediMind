package com.medrem.app.ui.screens.dashboard

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medrem.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToMedications: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    // Refresh every time screen is visited
    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MediMind", fontWeight = FontWeight.Bold, color = PrimaryDark)
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Person, "Profile", tint = PrimaryDark, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAiAssistant,
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.AutoAwesome, "AI Assistant")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                // Greeting
                Text("Hello, ${uiState.userName}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("Your health status is stable today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(24.dp))

                // Consistency Score Ring
                val score = uiState.stats?.consistencyScore?.toFloat() ?: 0f
                ConsistencyRingCard(score = score, label = "Consistency Score")

                Spacer(modifier = Modifier.height(16.dp))

                // Today's Progress Ring
                val progress = uiState.todayProgress
                val taken = progress?.taken ?: 0
                val total = progress?.totalDoses ?: 1
                ProgressRingCard(taken = taken, total = total)

                Spacer(modifier = Modifier.height(16.dp))

                // Status Breakdown
                if (progress != null) {
                    StatusBreakdownCard(taken = progress.taken, missed = progress.missed, skipped = progress.skipped)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upcoming Medications
                if (progress != null && progress.doses.isNotEmpty()) {
                    val upcomingCount = progress.doses.count { it.status == "upcoming" }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Upcoming Medications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    progress.doses.filter { it.status == "upcoming" }.take(3).forEach { dose ->
                        UpcomingMedicationChip(name = dose.name, time = dose.time, dosage = dose.dosage)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (upcomingCount == 0) {
                        Text("No upcoming medications for today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Health Insight Card
                HealthInsightCard(message = uiState.healthInsight)

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Reports
                if (uiState.recentReports.isNotEmpty()) {
                    Text("Recent Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.recentReports.forEach { report ->
                        ReportListItem(title = report.title, subtitle = report.subtitle)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Stats Grid
                Text("Quick Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStatCard(Modifier.weight(1f), icon = Icons.Outlined.Schedule, label = "Avg. Taken Time", value = uiState.stats?.avgTakenTime ?: "N/A", tint = PrimaryTeal)
                    QuickStatCard(Modifier.weight(1f), icon = Icons.Outlined.Star, label = "Perfect Streak", value = "${uiState.stats?.perfectStreakDays ?: 0} Days", tint = PrimaryTeal)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStatCard(Modifier.weight(1f), icon = Icons.Outlined.MedicalServices, label = "Meds Active", value = "${uiState.stats?.activeMedications ?: 0} Total", tint = PrimaryTeal)
                    QuickStatCard(Modifier.weight(1f), icon = Icons.Outlined.Notifications, label = "Alerts Sent", value = "${uiState.stats?.alertsSentToday ?: 0} Today", tint = PrimaryTeal)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ConsistencyRingCard(score: Float, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val animatedScore by animateFloatAsState(targetValue = score / 100f, animationSpec = tween(1000), label = "ring")
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Canvas(Modifier.size(120.dp)) {
                    drawArc(color = Color(0xFFE0E0E0), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color = PrimaryTeal, startAngle = -90f, sweepAngle = 360f * animatedScore, useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("${score.toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProgressRingCard(taken: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val fraction = if (total > 0) taken.toFloat() / total else 0f
            val animatedFraction by animateFloatAsState(targetValue = fraction, animationSpec = tween(1000), label = "progress")
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(Modifier.size(100.dp)) {
                    drawArc(color = Color(0xFFE0E0E0), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color = PrimaryTeal, startAngle = -90f, sweepAngle = 360f * animatedFraction, useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("$taken/$total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Today's Progress", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatusBreakdownCard(taken: Int, missed: Int, skipped: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Status Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(StatusTaken, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Taken ($taken)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(12.dp).background(StatusMissed, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Missed ($missed)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(12.dp).background(StatusSkipped, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Skipped ($skipped)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun UpcomingMedicationChip(name: String, time: String, dosage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(SecondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.MedicalServices, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text("$time • $dosage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HealthInsightCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(PrimaryTeal, PrimaryDark)))
                .padding(20.dp),
        ) {
            Column {
                Icon(Icons.Default.Insights, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text("Health Insight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
fun ReportListItem(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Description, null, tint = PrimaryTeal)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickStatCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(36.dp).background(tint.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
        }
    }
}
