package com.nickfinance.dashboard.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import com.nickfinance.dashboard.data.model.ChartType
import com.nickfinance.dashboard.data.model.SheetFullData
import com.nickfinance.dashboard.ui.navigation.Screen
import com.nickfinance.dashboard.ui.settings.SettingsViewModel
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.ChartColors
import com.nickfinance.dashboard.ui.theme.MonoFontFamily
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// ══════════════════════════════════════════════════════════════════
//  Dashboard Screen
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val cards by viewModel.cards.collectAsState()
    val quickStats by viewModel.quickStats.collectAsState()
    val sheetDataMap by viewModel.sheetDataMap.collectAsState()
    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()

    // Auto-load sheet data for all chart cards
    LaunchedEffect(cards) {
        viewModel.loadSheetDataForCards(cards)
    }

    var showAddSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<DashboardCardEntity?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Header ───────────────────────────────────────────────
        DashboardHeader(
            onAddClick = { showAddSheet = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Quick stat cards ─────────────────────────────────────
        QuickStatsRow(
            stats = quickStats,
            currencySymbol = currencySymbol
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Chart cards area ─────────────────────────────────────
        if (cards.isEmpty()) {
            EmptyDashboardState(
                onAddClick = { showAddSheet = true }
            )
        } else {
            ChartCardsGrid(
                cards = cards,
                viewModel = viewModel,
                sheetDataMap = sheetDataMap,
                onViewCard = { card ->
                    navController.navigate(
                        Screen.ChartDetail.createRoute(cardId = card.id)
                    )
                },
                onEditCard = { card ->
                    navController.navigate(
                        Screen.ChartConfig.createRoute(
                            cardId = card.id,
                            chartType = card.chartType
                        )
                    )
                },
                onDeleteCard = { card ->
                    showDeleteDialog = card
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Add chart bottom sheet ───────────────────────────────────
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = colors.cardSurface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textTertiary)
                )
            }
        ) {
            AddChartBottomSheet(
                onChartTypeSelected = { chartType ->
                    scope.launch { sheetState.hide() }
                    showAddSheet = false
                    navController.navigate(
                        Screen.ChartConfig.createRoute(
                            chartType = chartType.name
                        )
                    )
                }
            )
        }
    }

    // ── Delete confirmation dialog ───────────────────────────────
    showDeleteDialog?.let { card ->
        DeleteCardDialog(
            cardTitle = card.title,
            onConfirm = {
                viewModel.deleteCard(card.id)
                showDeleteDialog = null
            },
            onDismiss = {
                showDeleteDialog = null
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════
//  Header
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DashboardHeader(
    onAddClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "财务仪表盘",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "2026年2月 \u00B7 已更新",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.accentBlueBg)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加图表",
                tint = colors.accentBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Quick Stats Row
// ══════════════════════════════════════════════════════════════════

@Composable
private fun QuickStatsRow(
    stats: List<QuickStat>,
    currencySymbol: String
) {
    if (stats.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(stats, key = { it.label }) { stat ->
            QuickStatCard(stat = stat, currencySymbol = currencySymbol)
        }
    }
}

@Composable
private fun QuickStatCard(
    stat: QuickStat,
    currencySymbol: String
) {
    val colors = AppTheme.colors
    val currencyFormatter = remember { DecimalFormat("#,##0") }
    val percentFormatter = remember { DecimalFormat("0.0") }

    val displayColor = if (stat.value == 0.0) colors.textTertiary else stat.color
    val formattedValue = "$currencySymbol${currencyFormatter.format(stat.value)}"

    Column(
        modifier = Modifier
            .widthIn(min = 140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardSurface)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = stat.label,
            fontSize = 12.sp,
            color = colors.textSecondary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formattedValue,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily,
            color = displayColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (stat.changePercent != null) {
            val isPositive = stat.changePercent >= 0
            val changeColor = if (isPositive) colors.semanticGreen else colors.semanticRed
            val arrow = if (isPositive) "\u2191" else "\u2193"
            val sign = if (isPositive) "+" else ""

            Text(
                text = "$arrow $sign${percentFormatter.format(stat.changePercent)}%",
                fontSize = 11.sp,
                color = changeColor,
                maxLines = 1
            )
        } else {
            Text(
                text = "-- %",
                fontSize = 11.sp,
                color = colors.textTertiary,
                maxLines = 1
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Empty State
// ══════════════════════════════════════════════════════════════════

@Composable
private fun EmptyDashboardState(
    onAddClick: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "添加图表组件来可视化你的数据",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = onAddClick,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accentBlueBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.accentBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "添加图表",
                color = colors.accentBlue,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Chart Cards Grid
// ══════════════════════════════════════════════════════════════════

@Composable
private fun ChartCardsGrid(
    cards: List<DashboardCardEntity>,
    viewModel: DashboardViewModel,
    sheetDataMap: Map<Long, SheetFullData>,
    onViewCard: (DashboardCardEntity) -> Unit,
    onEditCard: (DashboardCardEntity) -> Unit,
    onDeleteCard: (DashboardCardEntity) -> Unit
) {
    val sortedCards = cards.sortedBy { it.sortOrder }
    var index = 0

    // Pre-compute chart data for all cards
    val chartDataCache = remember(sortedCards, sheetDataMap) {
        sortedCards.associateWith { card ->
            val sheetData = sheetDataMap[card.dataSheetId]
            if (sheetData != null) viewModel.extractChartData(card, sheetData) else null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        while (index < sortedCards.size) {
            val card = sortedCards[index]
            val chartData = chartDataCache[card]

            if (card.cardSize == "HALF") {
                // Try to pair with the next card if it is also HALF
                val nextCard = sortedCards.getOrNull(index + 1)
                if (nextCard != null && nextCard.cardSize == "HALF") {
                    val nextChartData = chartDataCache[nextCard]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ChartCard(
                                card = card,
                                chartData = chartData,
                                onClick = { onViewCard(card) },
                                onEdit = { onEditCard(card) },
                                onDelete = { onDeleteCard(card) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ChartCard(
                                card = nextCard,
                                chartData = nextChartData,
                                onClick = { onViewCard(nextCard) },
                                onEdit = { onEditCard(nextCard) },
                                onDelete = { onDeleteCard(nextCard) }
                            )
                        }
                    }
                    index += 2
                } else {
                    // Lone HALF card: render at half width on the left
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ChartCard(
                                card = card,
                                chartData = chartData,
                                onClick = { onViewCard(card) },
                                onEdit = { onEditCard(card) },
                                onDelete = { onDeleteCard(card) }
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    index += 1
                }
            } else {
                // FULL width card
                ChartCard(
                    card = card,
                    chartData = chartData,
                    onClick = { onViewCard(card) },
                    onEdit = { onEditCard(card) },
                    onDelete = { onDeleteCard(card) },
                    modifier = Modifier.fillMaxWidth()
                )
                index += 1
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Chart Card
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChartCard(
    card: DashboardCardEntity,
    chartData: ChartData?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val chartType = ChartType.fromString(card.chartType)
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardSurface)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(16.dp)
        ) {
            // Card header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${chartType.icon} ${chartType.displayName}",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart area: real data or placeholder
            val chartHeight = if (card.cardSize == "HALF") 100.dp else 180.dp
            if (chartData != null && chartData.series.isNotEmpty()) {
                RealChartRenderer(
                    chartType = chartType,
                    chartData = chartData,
                    showLabels = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                )
            } else {
                ChartPlaceholder(
                    chartType = chartType,
                    isHalf = card.cardSize == "HALF"
                )
            }
        }

        // Context menu (popup)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 8.dp, y = 0.dp),
            containerColor = colors.cardSurfaceSecondary,
            shape = RoundedCornerShape(12.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "编辑",
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )
                    }
                },
                onClick = {
                    showMenu = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = colors.semanticRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "删除",
                            color = colors.semanticRed,
                            fontSize = 13.sp
                        )
                    }
                },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Chart Placeholder Rendering
// ══════════════════════════════════════════════════════════════════

@Composable
private fun ChartPlaceholder(
    chartType: ChartType,
    isHalf: Boolean
) {
    val chartHeight = if (isHalf) 100.dp else 160.dp

    when (chartType) {
        ChartType.LINE -> LinePlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        )
        ChartType.AREA -> AreaPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        )
        ChartType.BAR, ChartType.HORIZONTAL_BAR, ChartType.COMBINED -> BarPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        )
        ChartType.PIE -> PiePlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        )
        ChartType.PROGRESS -> ProgressPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        )
        ChartType.STAT_NUMBER -> StatNumberPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        )
    }
}

// ── Line chart placeholder ───────────────────────────────────────

@Composable
private fun LinePlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val lineColor = ChartColors[0]
    val gridColor = colors.border

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gridLineCount = 4

        // Draw grid lines
        for (i in 0..gridLineCount) {
            val y = h * i / gridLineCount
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        // Draw a sample line
        val points = listOf(0.6f, 0.4f, 0.7f, 0.3f, 0.5f, 0.2f, 0.45f)
        val path = Path()
        val step = w / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, value ->
            val x = step * index
            val y = h * (1f - value) * 0.8f + h * 0.1f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Draw dots
        points.forEachIndexed { index, value ->
            val x = step * index
            val y = h * (1f - value) * 0.8f + h * 0.1f
            drawCircle(color = lineColor, radius = 3.5f, center = Offset(x, y))
        }
    }
}

// ── Area chart placeholder ───────────────────────────────────────

@Composable
private fun AreaPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val fillColor = ChartColors[0].copy(alpha = 0.15f)
    val lineColor = ChartColors[0]
    val gridColor = colors.border

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Grid
        for (i in 0..4) {
            val y = h * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        }

        val points = listOf(0.55f, 0.4f, 0.65f, 0.35f, 0.5f, 0.25f, 0.4f)
        val step = w / (points.size - 1).coerceAtLeast(1)

        // Fill area
        val areaPath = Path().apply {
            moveTo(0f, h)
            points.forEachIndexed { i, v ->
                val x = step * i
                val y = h * (1f - v) * 0.8f + h * 0.1f
                lineTo(x, y)
            }
            lineTo(w, h)
            close()
        }
        clipRect { drawPath(areaPath, fillColor) }

        // Line
        val linePath = Path()
        points.forEachIndexed { i, v ->
            val x = step * i
            val y = h * (1f - v) * 0.8f + h * 0.1f
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, lineColor, style = Stroke(2.5f, cap = StrokeCap.Round))
    }
}

// ── Bar chart placeholder ────────────────────────────────────────

@Composable
private fun BarPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val gridColor = colors.border

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Grid
        for (i in 0..4) {
            val y = h * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        }

        val values = listOf(0.7f, 0.5f, 0.85f, 0.4f, 0.6f, 0.75f)
        val barCount = values.size
        val totalPadding = w * 0.15f
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
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}

// ── Pie chart placeholder ────────────────────────────────────────

@Composable
private fun PiePlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height) * 0.7f
        val strokeWidth = diameter * 0.2f
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
                sweepAngle = sweep - 2f, // gap between slices
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

// ── Progress chart placeholder ───────────────────────────────────

@Composable
private fun ProgressPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val progressColor = ChartColors[0]
    val trackColor = colors.border

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "68%",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            val w = size.width
            val h = size.height
            val radius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)

            // Track
            drawRoundRect(
                color = trackColor,
                size = Size(w, h),
                cornerRadius = radius
            )

            // Progress
            drawRoundRect(
                color = progressColor,
                size = Size(w * 0.68f, h),
                cornerRadius = radius
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "当前值",
                fontSize = 10.sp,
                color = colors.textSecondary
            )
            Text(
                text = "目标值",
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }
}

// ── Stat number placeholder ──────────────────────────────────────

@Composable
private fun StatNumberPlaceholder(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "\u00A5128,450",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = colors.semanticGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+3.2%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.semanticGreen
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "较上月",
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Add Chart Bottom Sheet
// ══════════════════════════════════════════════════════════════════

@Composable
private fun AddChartBottomSheet(
    onChartTypeSelected: (ChartType) -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "选择图表类型",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "选择一种图表类型添加到仪表盘",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2-column x 4-row grid
        val chartTypes = ChartType.entries.toList()
        val rows = chartTypes.chunked(2)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { type ->
                        ChartTypeGridItem(
                            chartType = type,
                            onClick = { onChartTypeSelected(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number in row
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartTypeGridItem(
    chartType: ChartType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardSurfaceSecondary)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chartType.icon,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = chartType.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// ══════════════════════════════════════════════════════════════════
//  Delete Confirmation Dialog
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DeleteCardDialog(
    cardTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "删除图表",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = "确定删除「$cardTitle」吗？此操作不可撤销。",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "删除",
                    color = colors.semanticRed,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = colors.textSecondary
                )
            }
        }
    )
}
