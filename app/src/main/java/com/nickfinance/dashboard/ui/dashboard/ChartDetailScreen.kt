package com.nickfinance.dashboard.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nickfinance.dashboard.data.model.ChartType
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.ChartColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    cardId: Long,
    navController: NavController,
    configViewModel: ChartConfigViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val existingCard by configViewModel.existingCard.collectAsState()
    val sheets by configViewModel.sheets.collectAsState()
    val columnsForSheet by configViewModel.columns.collectAsState()
    val sheetDataMap by dashboardViewModel.sheetDataMap.collectAsState()

    LaunchedEffect(cardId) {
        configViewModel.loadCard(cardId)
    }

    LaunchedEffect(existingCard) {
        existingCard?.let { card ->
            configViewModel.loadColumnsForSheet(card.dataSheetId)
            dashboardViewModel.loadSheetData(card.dataSheetId)
        }
    }

    val card = existingCard
    val chartType = card?.let { ChartType.fromString(it.chartType) } ?: ChartType.LINE
    val sheetName = sheets.find { it.id == card?.dataSheetId }?.let { "${it.icon} ${it.name}" } ?: ""

    // Extract real chart data
    val chartData = remember(card, sheetDataMap) {
        if (card != null) {
            val sheetData = sheetDataMap[card.dataSheetId]
            if (sheetData != null) dashboardViewModel.extractChartData(card, sheetData) else null
        } else null
    }

    // Extract progress data for PROGRESS type
    val progressData = remember(card, sheetDataMap) {
        if (card != null && chartType == ChartType.PROGRESS) {
            val sheetData = sheetDataMap[card.dataSheetId]
            val pair = if (sheetData != null) dashboardViewModel.extractProgressData(card, sheetData) else null
            if (pair != null) ProgressData(pair.first, pair.second) else null
        } else null
    }

    val selectedColIds = card?.selectedColumnIds
        ?.split(",")
        ?.mapNotNull { it.trim().toLongOrNull() }
        ?: emptyList()

    val selectedColNames = selectedColIds.mapNotNull { id ->
        columnsForSheet.find { it.id == id }?.name
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = card?.title ?: "图表详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = colors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Chart type badge + data source
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accentBlueBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${chartType.icon} ${chartType.displayName}",
                        fontSize = 12.sp,
                        color = colors.accentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (sheetName.isNotBlank()) {
                    Text(
                        text = "数据源：$sheetName",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large chart visualization area with REAL DATA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardSurface)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (chartType == ChartType.PROGRESS && progressData != null) {
                    RealProgressChartRenderer(
                        progressData = progressData,
                        isHalf = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else if (chartData != null && chartData.series.isNotEmpty()) {
                    RealChartRenderer(
                        chartType = chartType,
                        chartData = chartData,
                        showLabels = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                } else {
                    // Loading or no data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (card == null) "加载中..." else "暂无数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data series legend
            if (selectedColNames.isNotEmpty()) {
                Text(
                    text = "数据系列",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.cardSurface)
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                ) {
                    selectedColNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(ChartColors[index % ChartColors.size])
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary
                            )
                        }
                        if (index < selectedColNames.lastIndex) {
                            HorizontalDivider(
                                color = colors.border,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card info section
            Text(
                text = "图表信息",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.cardSurface)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoRow(label = "图表类型", value = chartType.displayName)
                InfoRow(label = "卡片大小", value = if (card?.cardSize == "HALF") "半宽" else "整行宽")
                if (chartData != null) {
                    InfoRow(label = "数据行数", value = "${chartData.xLabels.size} 行")
                    InfoRow(label = "数据系列", value = "${chartData.series.size} 个")
                }
                if (card?.targetValue != null) {
                    InfoRow(label = "目标值", value = card.targetValue.toString())
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
