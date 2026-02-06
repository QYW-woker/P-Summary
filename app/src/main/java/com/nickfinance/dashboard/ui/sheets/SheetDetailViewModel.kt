package com.nickfinance.dashboard.ui.sheets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.model.SheetFullData
import com.nickfinance.dashboard.data.model.RowWithCells
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.local.entity.RowDataEntity
import com.nickfinance.dashboard.data.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SheetDetailViewModel @Inject constructor(
    private val sheetRepository: SheetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sheetId: Long = savedStateHandle.get<Long>("sheetId") ?: 0L

    val sheetFullData: StateFlow<SheetFullData?> = sheetRepository.getSheetFullData(sheetId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addRow() {
        viewModelScope.launch {
            sheetRepository.addRow(sheetId)
        }
    }

    fun deleteRow(rowId: Long) {
        viewModelScope.launch {
            sheetRepository.deleteRow(rowId)
        }
    }

    fun setCellValue(rowId: Long, columnId: Long, value: String) {
        viewModelScope.launch {
            sheetRepository.setCellValue(rowId, columnId, value)
        }
    }

    fun renameSheet(newName: String) {
        viewModelScope.launch {
            sheetRepository.updateSheetName(sheetId, newName)
        }
    }

    fun addColumn(
        name: String,
        type: String,
        groupName: String?,
        groupColor: String?,
        isNegativeRed: Boolean,
        formula: String?
    ) {
        viewModelScope.launch {
            sheetRepository.addColumn(sheetId, name, type, groupName, groupColor, isNegativeRed, formula)
        }
    }

    fun updateColumn(column: ColumnDefEntity) {
        viewModelScope.launch {
            sheetRepository.updateColumn(column)
        }
    }

    fun deleteColumn(columnId: Long) {
        viewModelScope.launch {
            sheetRepository.deleteColumn(columnId)
        }
    }

    fun reorderColumns(columns: List<ColumnDefEntity>) {
        viewModelScope.launch {
            columns.forEachIndexed { index, col ->
                sheetRepository.reorderColumn(col.id, index)
            }
        }
    }
}
