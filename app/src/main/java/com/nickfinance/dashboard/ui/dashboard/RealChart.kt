package com.nickfinance.dashboard.ui.dashboard

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nickfinance.dashboard.data.model.ChartType
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.MonoFontFamily
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// ══════════════════════════════════════════════════════════════════
//  Data Models
// ══════════════════════════════════════════════════════════════════

data class ChartSeriesData(
    val name: String,
    val color: Color,
    val values: List<Double>
)

data class ChartData(
    val xLabels: List<String>,
    val series: List<ChartSeriesData>,
    val yAxisPrefix: String = "\u00A5"
)

// ══════════════════════════════════════════════════════════════════
//  Main Entry Point
// ══════════════════════════════════════════════════════════════════

@Composable
fun RealChartRenderer(
    chartType: ChartType,
    chartData: ChartData,
    showLabels: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (chartData.series.isEmpty() || chartData.xLabels.isEmpty()) {
        FallbackPlaceholder(
            message = "No data available",
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier) {
        when (chartType) {
            ChartType.LINE, ChartType.AREA -> {
                RealLineChart(
                    chartData = chartData,
                    showLabels = showLabels,
                    fillArea = chartType == ChartType.AREA,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            ChartType.BAR, ChartType.HORIZONTAL_BAR, ChartType.COMBINED -> {
                RealBarChart(
                    chartData = chartData,
                    showLabels = showLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            ChartType.PIE -> {
                RealPieChart(
                    chartData = chartData,
                    showLabels = showLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            ChartType.PROGRESS, ChartType.STAT_NUMBER -> {
                FallbackPlaceholder(
                    message = "${chartType.displayName} - use dedicated widget",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ChartLegend(series = chartData.series)
    }
}

// ══════════════════════════════════════════════════════════════════
//  Y-Axis Utilities
// ══════════════════════════════════════════════════════════════════

private const val GRID_LINE_COUNT = 5

private val yAxisFormatter = DecimalFormat("#,##0.00")

/**
 * Compute a "nice" Y-axis range that brackets all data values.
 * Returns a pair of (minY, maxY) snapped to round numbers.
 */
private fun computeYRange(series: List<ChartSeriesData>): Pair<Double, Double> {
    val allValues = series.flatMap { it.values }
    if (allValues.isEmpty()) return 0.0 to 1.0

    val rawMin = allValues.min()
    val rawMax = allValues.max()

    // If all values are the same, create a range around them
    if (rawMin == rawMax) {
        return if (rawMin == 0.0) {
            0.0 to 1.0
        } else {
            val margin = rawMin * 0.1
            (rawMin - margin) to (rawMax + margin)
        }
    }

    val range = rawMax - rawMin
    // Compute a nice step size
    val rawStep = range / GRID_LINE_COUNT
    val magnitude = Math.pow(10.0, floor(Math.log10(rawStep)))
    val normalizedStep = rawStep / magnitude

    val niceStep = when {
        normalizedStep <= 1.0 -> 1.0
        normalizedStep <= 2.0 -> 2.0
        normalizedStep <= 5.0 -> 5.0
        else -> 10.0
    } * magnitude

    val niceMin = floor(rawMin / niceStep) * niceStep
    val niceMax = ceil(rawMax / niceStep) * niceStep

    // Ensure min is at most 0 if data is all positive
    val finalMin = if (rawMin >= 0.0) max(0.0, niceMin) else niceMin

    return finalMin to niceMax
}

/**
 * Generate Y-axis tick values from min to max.
 */
private fun computeYTicks(minY: Double, maxY: Double, count: Int = GRID_LINE_COUNT): List<Double> {
    if (count <= 0) return emptyList()
    val step = (maxY - minY) / count
    return (0..count).map { minY + it * step }
}

// ══════════════════════════════════════════════════════════════════
//  Real Line Chart
// ══════════════════════════════════════════════════════════════════

@Composable
private fun RealLineChart(
    chartData: ChartData,
    showLabels: Boolean,
    fillArea: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val gridColor = colors.border
    val textSecondaryColor = colors.textSecondary
    val textTertiaryColor = colors.textTertiary

    val density = LocalDensity.current
    val leftPaddingPx = with(density) { 60.dp.toPx() }
    val bottomPaddingPx = with(density) { 24.dp.toPx() }
    val topPaddingPx = with(density) { 12.dp.toPx() }
    val rightPaddingPx = with(density) { 8.dp.toPx() }

    val (minY, maxY) = remember(chartData.series) {
        computeYRange(chartData.series)
    }
    val yTicks = remember(minY, maxY) {
        computeYTicks(minY, maxY, GRID_LINE_COUNT)
    }

    val gridColorArgb = gridColor.toArgb()
    val textSecondaryArgb = textSecondaryColor.toArgb()
    val textTertiaryArgb = textTertiaryColor.toArgb()
    val prefix = chartData.yAxisPrefix

    val dotRadius = if (showLabels) 5f else 4f

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val chartLeft = leftPaddingPx
        val chartRight = canvasWidth - rightPaddingPx
        val chartTop = topPaddingPx
        val chartBottom = canvasHeight - bottomPaddingPx
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val nativeCanvas = drawContext.canvas.nativeCanvas

        // ── Y-axis labels paint ──
        val yLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textSecondaryArgb
            textSize = with(density) { 10.sp.toPx() }
            typeface = Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        // ── X-axis labels paint ──
        val xLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textTertiaryArgb
            textSize = with(density) { 9.sp.toPx() }
            typeface = Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // ── Data label paint (for showLabels mode) ──
        val dataLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = with(density) { 9.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.LEFT
        }

        // ── Draw horizontal grid lines and Y-axis labels ──
        val yRange = maxY - minY
        yTicks.forEach { tickValue ->
            val fraction = if (yRange != 0.0) (tickValue - minY) / yRange else 0.0
            val y = chartBottom - (fraction * chartHeight).toFloat()

            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f
            )

            // Y-axis label
            val label = "$prefix${yAxisFormatter.format(tickValue)}"
            nativeCanvas.drawText(
                label,
                chartLeft - with(density) { 6.dp.toPx() },
                y + yLabelPaint.textSize / 3f,
                yLabelPaint
            )
        }

        // ── Draw X-axis labels ──
        val xCount = chartData.xLabels.size
        if (xCount > 0) {
            val xStep = if (xCount > 1) chartWidth / (xCount - 1) else 0f

            // Determine label skip to avoid overlap
            val maxLabelWidth = chartData.xLabels.maxOfOrNull { xLabelPaint.measureText(it) } ?: 0f
            val minLabelSpacing = maxLabelWidth + with(density) { 8.dp.toPx() }
            val labelSkip = if (xStep > 0f && minLabelSpacing > xStep) {
                ceil(minLabelSpacing / xStep).toInt().coerceAtLeast(1)
            } else {
                1
            }

            chartData.xLabels.forEachIndexed { index, label ->
                if (index % labelSkip == 0 || index == xCount - 1) {
                    val x = if (xCount > 1) chartLeft + xStep * index else chartLeft + chartWidth / 2f
                    nativeCanvas.drawText(
                        label,
                        x,
                        chartBottom + bottomPaddingPx * 0.75f,
                        xLabelPaint
                    )
                }
            }
        }

        // ── Draw data series lines ──
        chartData.series.forEach { seriesData ->
            val pointCount = min(seriesData.values.size, xCount)
            if (pointCount < 1) return@forEach

            val xStep = if (pointCount > 1) chartWidth / (pointCount - 1) else 0f

            // Build path
            val linePath = Path()
            val points = mutableListOf<Offset>()

            for (i in 0 until pointCount) {
                val value = seriesData.values[i]
                val fraction = if (yRange != 0.0) (value - minY) / yRange else 0.5
                val x = if (pointCount > 1) chartLeft + xStep * i else chartLeft + chartWidth / 2f
                val y = chartBottom - (fraction * chartHeight).toFloat()
                val point = Offset(x, y)
                points.add(point)

                if (i == 0) {
                    linePath.moveTo(point.x, point.y)
                } else {
                    linePath.lineTo(point.x, point.y)
                }
            }

            // Optional area fill for AREA chart type
            if (fillArea && points.size >= 2) {
                val areaPath = Path().apply {
                    moveTo(points.first().x, chartBottom)
                    points.forEach { p -> lineTo(p.x, p.y) }
                    lineTo(points.last().x, chartBottom)
                    close()
                }
                drawPath(
                    path = areaPath,
                    color = seriesData.color.copy(alpha = 0.12f)
                )
            }

            // Draw line
            drawPath(
                path = linePath,
                color = seriesData.color,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // Draw data point circles
            points.forEach { point ->
                // White fill behind the dot for clarity
                drawCircle(
                    color = Color.White,
                    radius = dotRadius + 1.5f,
                    center = point
                )
                drawCircle(
                    color = seriesData.color,
                    radius = dotRadius,
                    center = point
                )
            }

            // Draw data labels when in detail mode
            if (showLabels && points.isNotEmpty()) {
                val labelIndex = 0 // Label the first data point
                val point = points[labelIndex]
                val value = seriesData.values[labelIndex]
                val labelText = "${seriesData.name}, $prefix${yAxisFormatter.format(value)}"

                dataLabelPaint.color = seriesData.color.toArgb()

                // Position label slightly above and to the right of the point
                val labelX = point.x + with(density) { 6.dp.toPx() }
                val labelY = point.y - with(density) { 6.dp.toPx() }

                // Clamp within chart bounds
                val adjustedX = labelX.coerceIn(
                    chartLeft,
                    chartRight - dataLabelPaint.measureText(labelText)
                )
                val adjustedY = labelY.coerceAtLeast(chartTop + dataLabelPaint.textSize)

                nativeCanvas.drawText(labelText, adjustedX, adjustedY, dataLabelPaint)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Real Bar Chart
// ══════════════════════════════════════════════════════════════════

@Composable
private fun RealBarChart(
    chartData: ChartData,
    showLabels: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val gridColor = colors.border
    val textSecondaryColor = colors.textSecondary
    val textTertiaryColor = colors.textTertiary

    val density = LocalDensity.current
    val leftPaddingPx = with(density) { 60.dp.toPx() }
    val bottomPaddingPx = with(density) { 24.dp.toPx() }
    val topPaddingPx = with(density) { 12.dp.toPx() }
    val rightPaddingPx = with(density) { 8.dp.toPx() }

    val (minY, maxY) = remember(chartData.series) {
        computeYRange(chartData.series)
    }
    val yTicks = remember(minY, maxY) {
        computeYTicks(minY, maxY, GRID_LINE_COUNT)
    }

    val textSecondaryArgb = textSecondaryColor.toArgb()
    val textTertiaryArgb = textTertiaryColor.toArgb()
    val prefix = chartData.yAxisPrefix

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val chartLeft = leftPaddingPx
        val chartRight = canvasWidth - rightPaddingPx
        val chartTop = topPaddingPx
        val chartBottom = canvasHeight - bottomPaddingPx
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val nativeCanvas = drawContext.canvas.nativeCanvas

        // ── Paint objects ──
        val yLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textSecondaryArgb
            textSize = with(density) { 10.sp.toPx() }
            typeface = Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        val xLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textTertiaryArgb
            textSize = with(density) { 9.sp.toPx() }
            typeface = Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val valueLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = with(density) { 8.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // ── Draw horizontal grid lines and Y-axis labels ──
        val yRange = maxY - minY
        yTicks.forEach { tickValue ->
            val fraction = if (yRange != 0.0) (tickValue - minY) / yRange else 0.0
            val y = chartBottom - (fraction * chartHeight).toFloat()

            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f
            )

            val label = "$prefix${yAxisFormatter.format(tickValue)}"
            nativeCanvas.drawText(
                label,
                chartLeft - with(density) { 6.dp.toPx() },
                y + yLabelPaint.textSize / 3f,
                yLabelPaint
            )
        }

        // ── Compute bar layout ──
        val groupCount = chartData.xLabels.size
        val seriesCount = chartData.series.size
        if (groupCount == 0 || seriesCount == 0) return@Canvas

        val groupWidth = chartWidth / groupCount
        val groupPadding = groupWidth * 0.15f
        val barsAreaWidth = groupWidth - groupPadding * 2
        val barSpacing = if (seriesCount > 1) barsAreaWidth * 0.08f else 0f
        val totalSpacing = barSpacing * (seriesCount - 1).coerceAtLeast(0)
        val barWidth = (barsAreaWidth - totalSpacing) / seriesCount

        val baselineY = if (yRange != 0.0) {
            chartBottom - ((0.0 - minY) / yRange * chartHeight).toFloat()
        } else {
            chartBottom
        }.coerceIn(chartTop, chartBottom)

        // ── Draw grouped bars ──
        for (groupIndex in 0 until groupCount) {
            val groupLeft = chartLeft + groupIndex * groupWidth

            chartData.series.forEachIndexed { seriesIndex, seriesData ->
                if (groupIndex >= seriesData.values.size) return@forEachIndexed

                val value = seriesData.values[groupIndex]
                val fraction = if (yRange != 0.0) (value - minY) / yRange else 0.0
                val valueY = chartBottom - (fraction * chartHeight).toFloat()

                val barLeft = groupLeft + groupPadding +
                        seriesIndex * (barWidth + barSpacing)

                val barTop = min(valueY, baselineY)
                val barBottom = max(valueY, baselineY)
                val barHeight = barBottom - barTop

                if (barHeight > 0f) {
                    drawRoundRect(
                        color = seriesData.color,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }

                // Value label above bar (in detail mode)
                if (showLabels) {
                    val labelText = "$prefix${yAxisFormatter.format(value)}"
                    valueLabelPaint.color = seriesData.color.toArgb()
                    val labelX = barLeft + barWidth / 2f
                    val labelY = barTop - with(density) { 4.dp.toPx() }
                    val clampedY = labelY.coerceAtLeast(chartTop + valueLabelPaint.textSize)

                    nativeCanvas.drawText(labelText, labelX, clampedY, valueLabelPaint)
                }
            }

            // X-axis label
            val xCenter = groupLeft + groupWidth / 2f
            if (groupIndex < chartData.xLabels.size) {
                nativeCanvas.drawText(
                    chartData.xLabels[groupIndex],
                    xCenter,
                    chartBottom + bottomPaddingPx * 0.75f,
                    xLabelPaint
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Real Pie Chart (Donut)
// ══════════════════════════════════════════════════════════════════

@Composable
private fun RealPieChart(
    chartData: ChartData,
    showLabels: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val textPrimaryColor = colors.textPrimary
    val density = LocalDensity.current

    // Use the latest (last) row of values
    val latestValues = remember(chartData) {
        val lastIndex = chartData.xLabels.lastIndex
        chartData.series.map { series ->
            if (lastIndex in series.values.indices) {
                series.values[lastIndex].coerceAtLeast(0.0)
            } else {
                0.0
            }
        }
    }

    val total = remember(latestValues) { latestValues.sum() }

    val textPrimaryArgb = textPrimaryColor.toArgb()

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (total <= 0.0) return@Canvas

        val diameter = min(canvasWidth, canvasHeight) * 0.70f
        val strokeWidth = diameter * 0.18f
        val arcRadius = (diameter - strokeWidth) / 2f

        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        val arcTopLeft = Offset(
            centerX - diameter / 2f,
            centerY - diameter / 2f
        )
        val arcSize = Size(diameter, diameter)

        val nativeCanvas = drawContext.canvas.nativeCanvas

        val segmentLabelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textPrimaryArgb
            textSize = with(density) { 10.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val gapDegrees = 2f
        var startAngle = -90f

        chartData.series.forEachIndexed { index, seriesData ->
            val value = latestValues[index]
            val fraction = value / total
            val sweepAngle = (360f * fraction.toFloat()) - gapDegrees

            if (sweepAngle > 0f) {
                drawArc(
                    color = seriesData.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Draw label near segment midpoint
                if (showLabels && fraction > 0.04) {
                    val midAngle = startAngle + sweepAngle / 2f
                    val midAngleRad = midAngle * PI.toFloat() / 180f
                    val labelRadius = diameter / 2f + strokeWidth / 2f + with(density) { 14.dp.toPx() }

                    val labelX = centerX + cos(midAngleRad) * labelRadius
                    val labelY = centerY + sin(midAngleRad) * labelRadius + segmentLabelPaint.textSize / 3f

                    segmentLabelPaint.color = seriesData.color.toArgb()
                    nativeCanvas.drawText(seriesData.name, labelX, labelY, segmentLabelPaint)
                }
            }

            startAngle += sweepAngle + gapDegrees
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  Fallback Placeholder
// ══════════════════════════════════════════════════════════════════

@Composable
private fun FallbackPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary
        )
    }
}

// ══════════════════════════════════════════════════════════════════
//  Chart Legend
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartLegend(series: List<ChartSeriesData>) {
    val colors = AppTheme.colors

    if (series.isEmpty()) return

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        series.forEach { seriesData ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(seriesData.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = seriesData.name,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
