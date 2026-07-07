package com.medrem.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medrem.app.data.local.TokenManager
import com.medrem.app.ui.screens.login.LoginScreen
import com.medrem.app.ui.screens.register.RegisterScreen
import com.medrem.app.ui.screens.dashboard.DashboardScreen
import com.medrem.app.ui.screens.medications.MedicationsScreen
import com.medrem.app.ui.screens.medications.AddMedicationScreen
import com.medrem.app.ui.screens.reports.ReportsScreen
import com.medrem.app.ui.screens.reports.ReportDetailScreen
import com.medrem.app.ui.screens.ai.AiAssistantScreen
import com.medrem.app.ui.screens.profile.ProfileScreen
import com.medrem.app.ui.screens.settings.SettingsScreen
import javax.inject.Inject

/**
 * Bottom navigation items.
 */
data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Dashboard.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Medications", Screen.Medications.route, Icons.Filled.MedicalServices, Icons.Outlined.MedicalServices),
    BottomNavItem("Reports", Screen.Reports.route, Icons.Filled.Description, Icons.Outlined.Description),
    BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person),
)

/**
 * Main navigation host composable.
 */
@Composable
fun MedRemNavHost(tokenManager: TokenManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check persisted token to decide the start destination
    val startDestination = remember {
        if (tokenManager.isLoggedIn()) Screen.Dashboard.route else Screen.Login.route
    }

    // Determine if bottom bar should be shown
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAiAssistant = {
                        navController.navigate(Screen.AiAssistant.route)
                    },
                    onNavigateToMedications = {
                        navController.navigate(Screen.Medications.route)
                    }
                )
            }

            composable(Screen.Medications.route) {
                MedicationsScreen(
                    onNavigateToAddMedication = {
                        navController.navigate(Screen.AddMedication.route)
                    },
                    onNavigateToEditMedication = { medicationId ->
                        navController.navigate(Screen.EditMedication.createRoute(medicationId))
                    }
                )
            }

            composable(Screen.AddMedication.route) {
                AddMedicationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onMedicationAdded = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditMedication.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.StringType })
            ) {
                AddMedicationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onMedicationAdded = { navController.popBackStack() },
                    medicationId = it.arguments?.getString("medicationId")
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    onNavigateToReportDetail = { reportId ->
                        navController.navigate(Screen.ReportDetail.createRoute(reportId))
                    }
                )
            }

            composable(
                route = Screen.ReportDetail.route,
                arguments = listOf(navArgument("reportId") { type = NavType.StringType })
            ) {
                ReportDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AiAssistant.route) {
                AiAssistantScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
