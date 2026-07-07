package com.medrem.app.ui.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medrem.app.data.local.TokenManager
import com.medrem.app.data.remote.dto.*
import com.medrem.app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val stats: DashboardStatsDto? = null,
    val todayProgress: TodayProgressDto? = null,
    val recentReports: List<RecentReportDto> = emptyList(),
    val healthInsight: String = "Monitoring your health data...",
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    var uiState by mutableStateOf(DashboardUiState())
        private set

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                userName = tokenManager.getUserName() ?: "User"
            )

            // Load all dashboard data in parallel
            val statsResult = dashboardRepository.getStats()
            val consistencyResult = dashboardRepository.getConsistency()
            val reportsResult = dashboardRepository.getRecentReports()

            val stats = statsResult.getOrNull()
            val insight = generatePositiveInsight(stats?.consistencyScore ?: 0.0)

            uiState = uiState.copy(
                isLoading = false,
                stats = stats,
                todayProgress = consistencyResult.getOrNull(),
                recentReports = reportsResult.getOrDefault(emptyList()),
                healthInsight = insight
            )
        }
    }

    private fun generatePositiveInsight(score: Double): String {
        return when {
            score >= 90 -> "Incredible job! Your consistency is perfect. Keeping this rhythm is the best way to maintain your long-term health. Keep it up!"
            score >= 70 -> "Doing great! You're staying on track with almost all your doses. Every pill taken is a solid step towards better recovery."
            score >= 40 -> "Good progress! You're building a healthy habit. Even small steps count—let's try to hit a new personal best today!"
            else -> "You're starting a journey! Every medication logged is a win for your future self. We're here to help you get back on track—you've got this!"
        }
    }
}
