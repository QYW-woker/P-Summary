package com.nickfinance.dashboard.ui.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nickfinance.dashboard.data.model.ChartType
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.ChartColors
import com.nickfinance.dashboard.ui.theme.MonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    cardId: Long,
    navController: NavController,
    viewModel: ChartConfigViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val existingCard by viewModel.existingCard.collectAsState()
    val sheets by viewModel.sheets.collectAsState()
    val columnsForSheet by viewModel.columns.collectAsState()

    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    LaunchedEffect(existingCard) {
        existingCard?.let { card ->
            viewModel.loadColumnsForSheet(card.dataSheetId)
        }
    }

    val card = existingCard
    val chartType = card?.let { ChartType.fromString(it.chartType) } ?: ChartType.LINE
    val sheetName = sheets.find { it.id == card?.dataSheetId }?.let { "${it.icon} ${it.name}" } ?: ""

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
            // Chart type badge
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

            // Large chart visualization area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardSurface)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                DetailChartPlaceholder(
                    chartType = chartType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
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

@Composable
private fun DetailChartPlaceholder(
    chartType: ChartType,
    modifier: Modifier = Modifier
) {
    when (chartType) {
        ChartType.LINE -> DetailLinePlaceholder(modifier)
        ChartType.AREA -> DetailAreaPlaceholder(modifier)
        ChartType.BAR, ChartType.HORIZONTAL_BAR, ChartType.COMBINED -> DetailBarPlaceholder(modifier)
        ChartType.PIE -> DetailPiePlaceholder(modifier)
        ChartType.PROGRESS -> DetailProgressPlaceholder(modifier)
        ChartType.STAT_NUMBER -> DetailStatPlaceholder(modifier)
    }
}

@Composable
private fun DetailLinePlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val lineColor = ChartColors[0]
    val lineColor2 = ChartColors[1]
    val gridColor = colors.border

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (i in 0..5) {
            val y = h * i / 5
            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        }

        val points1 = listOf(0.6f, 0.4f, 0.7f, 0.3f, 0.5f, 0.2f, 0.45f, 0.55f, 0.35f, 0.65f)
        val points2 = listOf(0.3f, 0.5f, 0.35f, 0.6f, 0.4f, 0.55f, 0.3f, 0.45f, 0.5f, 0.4f)
        val step = w / (points1.size - 1).coerceAtLeast(1)

        // Line 1
        val path1 = Path()
        points1.forEachIndexed { index, value ->
            val x = step * index
            val y = h * (1f - value) * 0.8f + h * 0.1f
            if (index == 0) path1.moveTo(x, y) else path1.lineTo(x, y)
        }
        drawPath(path1, lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        points1.forEachIndexed { index, value ->
            val x = step * index
            val y = h * (1f - value) * 0.8f + h * 0.1f
            drawCircle(color = lineColor, radius = 4f, center = Offset(x, y))
        }

        // Line 2
        val path2 = Path()
        points2.forEachIndexed { index, value ->
            val x = step * index
            val y = h * (1f - value) * 0.8f + h * 0.1f
            if (index == 0) path2.moveTo(x, y) else path2.lineTo(x, y)
        }
        drawPath(path2, lineColor2, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        points2.forEachIndexed { index, value ->
            val x = step * index
            val y = h * (1f - value) * 0.8f + h * 0.1f
            drawCircle(color = lineColor2, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
private fun DetailAreaPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val fillColor = ChartColors[0].copy(alpha = 0.15f)
    val lineColor = ChartColors[0]
    val gridColor = colors.border

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (i in 0..5) {
            val y = h * i / 5
            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        }

        val points = listOf(0.55f, 0.4f, 0.65f, 0.35f, 0.5f, 0.25f, 0.4f, 0.55f, 0.3f, 0.6f)
        val step = w / (points.size - 1).coerceAtLeast(1)

        val areaPath = Path().apply {
            moveTo(0f, h)
            points.forEachIndexed { i, v ->
                lineTo(step * i, h * (1f - v) * 0.8f + h * 0.1f)
            }
            lineTo(w, h)
            close()
        }
        clipRect { drawPath(areaPath, fillColor) }

        val linePath = Path()
        points.forEachIndexed { i, v ->
            val x = step * i
            val y = h * (1f - v) * 0.8f + h * 0.1f
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, lineColor, style = Stroke(2.5f, cap = StrokeCap.Round))
    }
}

@Composable
private fun DetailBarPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val gridColor = colors.border

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (i in 0..5) {
            val y = h * i / 5
            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        }

        val values = listOf(0.7f, 0.5f, 0.85f, 0.4f, 0.6f, 0.75f, 0.55f, 0.8f)
        val barCount = values.size
        val totalPadding = w * 0.12f
        val gap = totalPadding / (barCount + 1)
        val barWidth = (w - totalPadding) / barCount

        values.forEachIndexed { i, v ->
            val x = gap + i * (barWidth + gap)
            val barH = h * v * 0.8f
            val y = h - barH
            val color = ChartColors[i % ChartColors.size]

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
private fun DetailPiePlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height) * 0.75f
        val strokeWidth = diameter * 0.18f
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        val slices = listOf(0.35f, 0.25f, 0.20f, 0.12f, 0.08f)
        var startAngle = -90f

        slices.forEachIndexed { i, fraction ->
            val sweep = 360f * fraction
            drawArc(
                color = ChartColors[i % ChartColors.size],
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun DetailProgressPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val progressColor = ChartColors[0]
    val trackColor = colors.border

    Column(
        modifier = modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "68%",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            val w = size.width
            val h = size.height
            val radius = CornerRadius(h / 2, h / 2)

            drawRoundRect(color = trackColor, size = Size(w, h), cornerRadius = radius)
            drawRoundRect(color = progressColor, size = Size(w * 0.68f, h), cornerRadius = radius)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("当前值", fontSize = 12.sp, color = colors.textSecondary)
            Text("目标值", fontSize = 12.sp, color = colors.textSecondary)
        }
    }
}

@Composable
private fun DetailStatPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "\u00A5128,450",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2191 +3.2%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.semanticGreen
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "较上月",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}
