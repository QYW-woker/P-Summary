package com.nickfinance.dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import com.nickfinance.dashboard.data.model.RowWithCells
import com.nickfinance.dashboard.data.model.SheetFullData
import com.nickfinance.dashboard.data.repository.DashboardRepository
import com.nickfinance.dashboard.data.repository.SheetRepository
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

    init {
        loadQuickStats()
    }

    fun loadQuickStats() {
        viewModelScope.launch {
            val stats = mutableListOf<QuickStat>()

            // Total assets from "月度资产信贷统计"
            val assetSheet = sheetRepository.getSheetByName("月度资产信贷统计")
            if (assetSheet != null) {
                val totalValue = getLatestColumnValue(assetSheet.id, "总计")
                val prevValue = getPreviousColumnValue(assetSheet.id, "总计")
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
                val incomeValue = getLatestColumnValue(incomeSheet.id, "收入总计")
                val prevIncome = getPreviousColumnValue(incomeSheet.id, "收入总计")
                val change = if (prevIncome != null && prevIncome != 0.0)
                    ((incomeValue - prevIncome) / prevIncome * 100) else null
                stats.add(QuickStat("本月收入", incomeValue, change,
                    com.nickfinance.dashboard.ui.theme.SemanticGreen))
            } else {
                stats.add(QuickStat("本月收入", 0.0, null,
                    com.nickfinance.dashboard.ui.theme.SemanticGreen))
            }

            // Monthly expenses from "月度收支统计"
            if (incomeSheet != null) {
                val expenseValue = getLatestColumnValue(incomeSheet.id, "开销总计")
                val prevExpense = getPreviousColumnValue(incomeSheet.id, "开销总计")
                val change = if (prevExpense != null && prevExpense != 0.0)
                    ((expenseValue - prevExpense) / prevExpense * 100) else null
                stats.add(QuickStat("本月支出", expenseValue, change,
                    com.nickfinance.dashboard.ui.theme.SemanticRed))
            } else {
                stats.add(QuickStat("本月支出", 0.0, null,
                    com.nickfinance.dashboard.ui.theme.SemanticRed))
            }

            _quickStats.value = stats
        }
    }

    fun loadSheetData(sheetId: Long) {
        viewModelScope.launch {
            sheetRepository.getSheetFullData(sheetId).collect { data ->
                _sheetDataMap.value = _sheetDataMap.value + (sheetId to data)
            }
        }
    }

    private suspend fun getLatestColumnValue(sheetId: Long, columnName: String): Double {
        val columns = sheetRepository.getColumnsSync(sheetId)
        val col = columns.find { it.name == columnName } ?: return 0.0
        val rows = sheetRepository.getRows(sheetId)
        var latestValue = 0.0
        rows.collect { rowList ->
            if (rowList.isNotEmpty()) {
                val lastRow = rowList.last()
                sheetRepository.getCellsForRows(listOf(lastRow.id)).collect { cells ->
                    val cell = cells.find { it.columnId == col.id }
                    latestValue = cell?.value?.toDoubleOrNull() ?: 0.0
                }
            }
        }
        return latestValue
    }

    private suspend fun getPreviousColumnValue(sheetId: Long, columnName: String): Double? {
        val columns = sheetRepository.getColumnsSync(sheetId)
        val col = columns.find { it.name == columnName } ?: return null
        val rows = sheetRepository.getRows(sheetId)
        var prevValue: Double? = null
        rows.collect { rowList ->
            if (rowList.size >= 2) {
                val prevRow = rowList[rowList.size - 2]
                sheetRepository.getCellsForRows(listOf(prevRow.id)).collect { cells ->
                    val cell = cells.find { it.columnId == col.id }
                    prevValue = cell?.value?.toDoubleOrNull()
                }
            }
        }
        return prevValue
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
