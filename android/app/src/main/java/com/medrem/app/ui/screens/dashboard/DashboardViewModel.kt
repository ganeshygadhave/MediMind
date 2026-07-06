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

            uiState = uiState.copy(
                isLoading = false,
                stats = statsResult.getOrNull(),
                todayProgress = consistencyResult.getOrNull(),
                recentReports = reportsResult.getOrDefault(emptyList()),
            )
        }
    }
}
