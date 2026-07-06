package com.medrem.app.navigation

/**
 * Sealed class defining all navigation routes in the app.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object Medications : Screen("medications")
    data object AddMedication : Screen("add_medication")
    data object EditMedication : Screen("edit_medication/{medicationId}") {
        fun createRoute(medicationId: String) = "edit_medication/$medicationId"
    }
    data object Reports : Screen("reports")
    data object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    data object AiAssistant : Screen("ai_assistant")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}
