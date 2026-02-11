package com.nickfinance.dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nickfinance.dashboard.data.local.converter.ColumnType
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import com.nickfinance.dashboard.data.model.SheetFullData
import com.nickfinance.dashboard.data.repository.DashboardRepository
import com.nickfinance.dashboard.data.repository.SheetRepository
import com.nickfinance.dashboard.ui.theme.ChartColors
import com.nickfinance.dashboard.util.FormulaParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickStat(
    val label: String,
    val value: Double,
    val changePercent: Double?,
    val color: androidx.compose.ui.graphics.Color
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val sheetRepository: SheetRepository
) : ViewModel() {

    val cards: StateFlow<List<DashboardCardEntity>> = dashboardRepository.getAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _quickStats = MutableStateFlow<List<QuickStat>>(emptyList())
    val quickStats: StateFlow<List<QuickStat>> = _quickStats

    private val _sheetDataMap = MutableStateFlow<Map<Long, SheetFullData>>(emptyMap())
    val sheetDataMap: StateFlow<Map<Long, SheetFullData>> = _sheetDataMap

    private val _loadedSheetIds = mutableSetOf<Long>()

    init {
        loadQuickStats()
    }

    fun loadSheetDataForCards(cards: List<DashboardCardEntity>) {
        cards.forEach { card ->
            if (card.dataSheetId > 0 && card.dataSheetId !in _loadedSheetIds) {
                _loadedSheetIds.add(card.dataSheetId)
                loadSheetData(card.dataSheetId)
            }
        }
    }

    fun extractChartData(card: DashboardCardEntity, sheetData: SheetFullData): ChartData? {
        val columns = sheetData.columns
        val rows = sheetData.rows
        if (columns.isEmpty() || rows.isEmpty()) return null

        // Find x-axis column (date/time column)
        val xColumn = if (card.xAxisColumnId != null && card.xAxisColumnId > 0) {
            columns.find { it.id == card.xAxisColumnId }
        } else {
            // Auto-detect: use first DATE column or first column
            columns.firstOrNull { ColumnType.fromString(it.type) == ColumnType.DATE }
                ?: columns.firstOrNull()
        }

        // Parse selected column IDs
        val selectedIds = card.selectedColumnIds
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }

        // If no selected columns, auto-pick all numeric columns
        val seriesColumns = if (selectedIds.isNotEmpty()) {
            selectedIds.mapNotNull { id -> columns.find { it.id == id } }
        } else {
            columns.filter {
                val t = ColumnType.fromString(it.type)
                t == ColumnType.CURRENCY || t == ColumnType.NUMBER
            }
        }

        if (seriesColumns.isEmpty()) return null

        // Extract x-axis labels and series values
        val xLabels = mutableListOf<String>()
        val seriesValues = seriesColumns.map { mutableListOf<Double>() }

        var prevResolved: Map<Long, String>? = null
        for (row in rows) {
            // Resolve formulas with previous row context
            val resolved = resolveRowValues(row.cells, columns, prevResolved)

            // X label
            val xVal = if (xColumn != null) resolved[xColumn.id] ?: "" else ""
            val xLabel = formatXLabel(xVal, xColumn)
            xLabels.add(xLabel)

            // Series values
            seriesColumns.forEachIndexed { idx, col ->
                val rawValue = resolved[col.id]?.toDoubleOrNull() ?: 0.0
                seriesValues[idx].add(rawValue)
            }

            prevResolved = resolved
        }

        // Build series
        val series = seriesColumns.mapIndexed { idx, col ->
            ChartSeriesData(
                name = col.name,
                color = ChartColors[idx % ChartColors.size],
                values = seriesValues[idx]
            )
        }

        return ChartData(
            xLabels = xLabels,
            series = series
        )
    }

    private fun resolveRowValues(
        cells: Map<Long, String>,
        columns: List<ColumnDefEntity>,
        prevRowValues: Map<Long, String>? = null
    ): Map<Long, String> {
        val result = cells.toMutableMap()
        for (col in columns) {
            if (!col.formula.isNullOrBlank()) {
                val evaluated = FormulaParser.evaluate(col.formula, columns, result, prevRowValues)
                result[col.id] = evaluated
            }
        }
        return result
    }

    private fun formatXLabel(value: String, column: ColumnDefEntity?): String {
        if (value.isBlank()) return ""
        if (column == null) return value
        return when (ColumnType.fromString(column.type)) {
            ColumnType.DATE -> {
                // Try to format as short date
                val ts = value.toLongOrNull()
                if (ts != null) {
                    val sdf = java.text.SimpleDateFormat("M/d", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(ts))
                } else {
                    value
                }
            }
            else -> value
        }
    }

    fun loadQuickStats() {
        viewModelScope.launch {
            val stats = mutableListOf<QuickStat>()

            // Total assets from "月度资产信贷统计"
            val assetSheet = sheetRepository.getSheetByName("月度资产信贷统计")
            if (assetSheet != null) {
                val resolvedRows = getResolvedRows(assetSheet.id)
                val col = sheetRepository.getColumnsSync(assetSheet.id).find { it.name == "总计" }
                val totalValue = if (col != null && resolvedRows.isNotEmpty())
                    resolvedRows.last()[col.id]?.toDoubleOrNull() ?: 0.0 else 0.0
                val prevValue = if (col != null && resolvedRows.size >= 2)
                    resolvedRows[resolvedRows.size - 2][col.id]?.toDoubleOrNull() else null
                val change = if (prevValue != null && prevValue != 0.0)
                    ((totalValue - prevValue) / prevValue * 100) else null
                stats.add(QuickStat("总资产", totalValue, change,
                    com.nickfinance.dashboard.ui.theme.AccentBlue))
            } else {
                stats.add(QuickStat("总资产", 0.0, null,
                    com.nickfinance.dashboard.ui.theme.AccentBlue))
            }

            // Monthly income from "月度收支统计"
            val incomeSheet = sheetRepository.getSheetByName("月度收支统计")
            if (incomeSheet != null) {
                val resolvedRows = getResolvedRows(incomeSheet.id)
                val columns = sheetRepository.getColumnsSync(incomeSheet.id)
                val incomeCol = columns.find { it.name == "收入总计" }
                val expenseCol = columns.find { it.name == "开销总计" }

                val incomeValue = if (incomeCol != null && resolvedRows.isNotEmpty())
                    resolvedRows.last()[incomeCol.id]?.toDoubleOrNull() ?: 0.0 else 0.0
                val prevIncome = if (incomeCol != null && resolvedRows.size >= 2)
                    resolvedRows[resolvedRows.size - 2][incomeCol.id]?.toDoubleOrNull() else null
                val incomeChange = if (prevIncome != null && prevIncome != 0.0)
                    ((incomeValue - prevIncome) / prevIncome * 100) else null
                stats.add(QuickStat("本月收入", incomeValue, incomeChange,
                    com.nickfinance.dashboard.ui.theme.SemanticGreen))

                val expenseValue = if (expenseCol != null && resolvedRows.isNotEmpty())
                    resolvedRows.last()[expenseCol.id]?.toDoubleOrNull() ?: 0.0 else 0.0
                val prevExpense = if (expenseCol != null && resolvedRows.size >= 2)
                    resolvedRows[resolvedRows.size - 2][expenseCol.id]?.toDoubleOrNull() else null
                val expenseChange = if (prevExpense != null && prevExpense != 0.0)
                    ((expenseValue - prevExpense) / prevExpense * 100) else null
                stats.add(QuickStat("本月支出", expenseValue, expenseChange,
                    com.nickfinance.dashboard.ui.theme.SemanticRed))
            } else {
                stats.add(QuickStat("本月收入", 0.0, null,
                    com.nickfinance.dashboard.ui.theme.SemanticGreen))
                stats.add(QuickStat("本月支出", 0.0, null,
                    com.nickfinance.dashboard.ui.theme.SemanticRed))
            }

            _quickStats.value = stats
        }
    }

    /**
     * Load all rows for a sheet and resolve formulas row by row (with PREV context).
     */
    private suspend fun getResolvedRows(sheetId: Long): List<Map<Long, String>> {
        val columns = sheetRepository.getColumnsSync(sheetId)
        val rows = sheetRepository.getRowsSync(sheetId)
        if (rows.isEmpty() || columns.isEmpty()) return emptyList()

        val cells = sheetRepository.getCellsSync(rows.map { it.id })
        val cellsByRow = cells.groupBy { it.rowId }

        val resolvedRows = mutableListOf<Map<Long, String>>()
        for (row in rows) {
            val rowCells = (cellsByRow[row.id] ?: emptyList())
                .associate { it.columnId to it.value }
            val prevResolved = resolvedRows.lastOrNull()
            val resolved = resolveRowValues(rowCells, columns, prevResolved)
            resolvedRows.add(resolved)
        }
        return resolvedRows
    }

    fun loadSheetData(sheetId: Long) {
        viewModelScope.launch {
            sheetRepository.getSheetFullData(sheetId).collect { data ->
                _sheetDataMap.value = _sheetDataMap.value + (sheetId to data)
            }
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteCard(cardId)
        }
    }

    fun saveCard(card: DashboardCardEntity) {
        viewModelScope.launch {
            if (card.id == 0L) {
                dashboardRepository.insertCard(card)
            } else {
                dashboardRepository.updateCard(card)
            }
        }
    }
}
