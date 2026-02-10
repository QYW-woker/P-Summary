package com.nickfinance.dashboard.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nickfinance.dashboard.ui.dashboard.ChartDetailScreen
import com.nickfinance.dashboard.ui.dashboard.DashboardScreen
import com.nickfinance.dashboard.ui.dashboard.ChartConfigScreen
import com.nickfinance.dashboard.ui.settings.SettingsScreen
import com.nickfinance.dashboard.ui.settings.SettingsViewModel
import com.nickfinance.dashboard.ui.sheets.SheetDetailScreen
import com.nickfinance.dashboard.ui.sheets.SheetListScreen
import com.nickfinance.dashboard.ui.theme.AppTheme

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Sheets : Screen("sheets")
    data object Settings : Screen("settings")
    data object SheetDetail : Screen("sheet_detail/{sheetId}") {
        fun createRoute(sheetId: Long) = "sheet_detail/$sheetId"
    }
    data object ChartConfig : Screen("chart_config?cardId={cardId}&chartType={chartType}") {
        fun createRoute(cardId: Long = -1L, chartType: String = "LINE") =
            "chart_config?cardId=$cardId&chartType=$chartType"
    }
    data object ChartDetail : Screen("chart_detail/{cardId}") {
        fun createRoute(cardId: Long) = "chart_detail/$cardId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "仪表盘", Icons.Default.Dashboard),
    BottomNavItem(Screen.Sheets, "数据表", Icons.Default.TableChart),
    BottomNavItem(Screen.Settings, "设置", Icons.Default.Settings)
)

@Composable
fun AppNavigation(
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Screen.Dashboard.route,
        Screen.Sheets.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) },
            exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
            composable(Screen.Sheets.route) {
                SheetListScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(settingsViewModel = settingsViewModel)
            }
            composable(
                route = Screen.SheetDetail.route,
                arguments = listOf(navArgument("sheetId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sheetId = backStackEntry.arguments?.getLong("sheetId") ?: 0L
                SheetDetailScreen(
                    sheetId = sheetId,
                    navController = navController
                )
            }
            composable(
                route = Screen.ChartConfig.route,
                arguments = listOf(
                    navArgument("cardId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("chartType") { type = NavType.StringType; defaultValue = "LINE" }
                )
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getLong("cardId") ?: -1L
                val chartType = backStackEntry.arguments?.getString("chartType") ?: "LINE"
                ChartConfigScreen(
                    cardId = cardId,
                    initialChartType = chartType,
                    navController = navController
                )
            }
            composable(
                route = Screen.ChartDetail.route,
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
                ChartDetailScreen(
                    cardId = cardId,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val colors = AppTheme.colors

    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.border
        )
        NavigationBar(
            containerColor = colors.cardSurface,
            modifier = Modifier.height(64.dp),
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == item.screen.route
                } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Box(
                            modifier = if (selected) {
                                Modifier
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(colors.accentBlueBg)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            } else {
                                Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.accentBlue,
                        selectedTextColor = colors.accentBlue,
                        unselectedIconColor = colors.textTertiary,
                        unselectedTextColor = colors.textTertiary,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
