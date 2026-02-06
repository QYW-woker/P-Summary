package com.nickfinance.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nickfinance.dashboard.ui.navigation.AppNavigation
import com.nickfinance.dashboard.ui.settings.SettingsViewModel
import com.nickfinance.dashboard.ui.theme.FinanceDashboardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()

            FinanceDashboardTheme(themeMode = themeMode) {
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}
