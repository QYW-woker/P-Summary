package com.nickfinance.dashboard.ui.sheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.model.TemplateType
import com.nickfinance.dashboard.data.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SheetListItem(
    val sheet: DataSheetEntity,
    val rowCount: Int = 0
)

@HiltViewModel
class SheetListViewModel @Inject constructor(
    private val sheetRepository: SheetRepository
) : ViewModel() {

    val sheets: StateFlow<List<DataSheetEntity>> = sheetRepository.getAllSheets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rowCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val rowCounts: StateFlow<Map<Long, Int>> = _rowCounts

    fun observeRowCount(sheetId: Long) {
        viewModelScope.launch {
            sheetRepository.getRowCount(sheetId).collect { count ->
                _rowCounts.value = _rowCounts.value + (sheetId to count)
            }
        }
    }

    fun createFromTemplate(templateType: TemplateType, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val sheetId = sheetRepository.createFromTemplate(templateType)
            onCreated(sheetId)
        }
    }

    fun createBlankSheet(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val sheetId = sheetRepository.createSheet(name, "\uD83D\uDCC4")
            onCreated(sheetId)
        }
    }

    fun renameSheet(sheetId: Long, newName: String) {
        viewModelScope.launch {
            sheetRepository.updateSheetName(sheetId, newName)
        }
    }

    fun deleteSheet(sheetId: Long) {
        viewModelScope.launch {
            sheetRepository.deleteSheet(sheetId)
        }
    }
}
