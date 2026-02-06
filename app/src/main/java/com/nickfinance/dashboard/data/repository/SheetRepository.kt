package com.nickfinance.dashboard.data.repository

import com.nickfinance.dashboard.data.local.dao.CellDataDao
import com.nickfinance.dashboard.data.local.dao.ColumnDefDao
import com.nickfinance.dashboard.data.local.dao.RowDataDao
import com.nickfinance.dashboard.data.local.dao.SheetDao
import com.nickfinance.dashboard.data.local.entity.CellDataEntity
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.local.entity.RowDataEntity
import com.nickfinance.dashboard.data.model.RowWithCells
import com.nickfinance.dashboard.data.model.SheetFullData
import com.nickfinance.dashboard.data.model.TemplateType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SheetRepository @Inject constructor(
    private val sheetDao: SheetDao,
    private val columnDefDao: ColumnDefDao,
    private val rowDataDao: RowDataDao,
    private val cellDataDao: CellDataDao
) {
    // ── Sheet CRUD ──
    fun getAllSheets(): Flow<List<DataSheetEntity>> = sheetDao.getAllSheets()

    suspend fun getSheetById(id: Long): DataSheetEntity? = sheetDao.getSheetById(id)

    suspend fun getSheetByName(name: String): DataSheetEntity? = sheetDao.getSheetByName(name)

    suspend fun createSheet(name: String, icon: String = "\uD83D\uDCB0"): Long {
        return sheetDao.insertSheet(
            DataSheetEntity(name = name, icon = icon)
        )
    }

    suspend fun updateSheetName(id: Long, name: String) {
        val sheet = sheetDao.getSheetById(id) ?: return
        sheetDao.updateSheet(sheet.copy(name = name, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSheet(id: Long) {
        sheetDao.deleteSheetById(id)
    }

    fun getRowCount(sheetId: Long): Flow<Int> = sheetDao.getRowCount(sheetId)

    // ── Column CRUD ──
    fun getColumns(sheetId: Long): Flow<List<ColumnDefEntity>> =
        columnDefDao.getColumnsBySheet(sheetId)

    suspend fun getColumnsSync(sheetId: Long): List<ColumnDefEntity> =
        columnDefDao.getColumnsBySheetSync(sheetId)

    suspend fun addColumn(
        sheetId: Long,
        name: String,
        type: String,
        groupName: String? = null,
        groupColor: String? = null,
        isNegativeRed: Boolean = false,
        formula: String? = null
    ): Long {
        val maxOrder = columnDefDao.getMaxSortOrder(sheetId) ?: -1
        return columnDefDao.insertColumn(
            ColumnDefEntity(
                sheetId = sheetId,
                name = name,
                type = type,
                groupName = groupName,
                groupColor = groupColor,
                sortOrder = maxOrder + 1,
                isNegativeRed = isNegativeRed,
                formula = formula
            )
        )
    }

    suspend fun updateColumn(column: ColumnDefEntity) {
        columnDefDao.updateColumn(column)
    }

    suspend fun deleteColumn(columnId: Long) {
        columnDefDao.deleteColumnById(columnId)
    }

    suspend fun reorderColumn(columnId: Long, newSortOrder: Int) {
        columnDefDao.updateSortOrder(columnId, newSortOrder)
    }

    // ── Row CRUD ──
    fun getRows(sheetId: Long): Flow<List<RowDataEntity>> =
        rowDataDao.getRowsBySheet(sheetId)

    suspend fun addRow(sheetId: Long): Long {
        val maxOrder = rowDataDao.getMaxSortOrder(sheetId) ?: -1
        return rowDataDao.insertRow(
            RowDataEntity(sheetId = sheetId, sortOrder = maxOrder + 1)
        )
    }

    suspend fun deleteRow(rowId: Long) {
        rowDataDao.deleteRowById(rowId)
    }

    // ── Cell CRUD ──
    fun getCellsForRows(rowIds: List<Long>): Flow<List<CellDataEntity>> {
        if (rowIds.isEmpty()) return flowOf(emptyList())
        return cellDataDao.getCellsByRows(rowIds)
    }

    suspend fun setCellValue(rowId: Long, columnId: Long, value: String) {
        val existing = cellDataDao.getCell(rowId, columnId)
        if (existing != null) {
            cellDataDao.updateCellValue(rowId, columnId, value)
        } else {
            cellDataDao.upsertCell(
                CellDataEntity(rowId = rowId, columnId = columnId, value = value)
            )
        }
    }

    // ── Full data query ──
    fun getSheetFullData(sheetId: Long): Flow<SheetFullData> {
        val sheetFlow = sheetDao.getAllSheets().map { sheets ->
            sheets.find { it.id == sheetId }
        }
        val columnsFlow = columnDefDao.getColumnsBySheet(sheetId)
        val rowsWithCellsFlow = rowDataDao.getRowsBySheet(sheetId).flatMapLatest { rows ->
            if (rows.isEmpty()) {
                flowOf(emptyList())
            } else {
                cellDataDao.getCellsByRows(rows.map { it.id }).map { cells ->
                    val cellsByRow = cells.groupBy { it.rowId }
                    rows.map { row ->
                        RowWithCells(
                            row = row,
                            cells = (cellsByRow[row.id] ?: emptyList())
                                .associate { it.columnId to it.value }
                        )
                    }
                }
            }
        }

        return combine(sheetFlow, columnsFlow, rowsWithCellsFlow) { sheet, columns, rows ->
            SheetFullData(
                sheet = sheet ?: DataSheetEntity(id = sheetId, name = ""),
                columns = columns,
                rows = rows
            )
        }
    }

    // ── Template creation ──
    suspend fun createFromTemplate(templateType: TemplateType): Long {
        val sheetId = sheetDao.insertSheet(
            DataSheetEntity(
                name = templateType.displayName,
                icon = templateType.icon
            )
        )
        val columnEntities = templateType.columns.mapIndexed { index, col ->
            ColumnDefEntity(
                sheetId = sheetId,
                name = col.name,
                type = col.type,
                groupName = col.groupName,
                groupColor = col.groupColor,
                sortOrder = index,
                isNegativeRed = col.isNegativeRed,
                formula = col.formula
            )
        }
        columnDefDao.insertColumns(columnEntities)
        return sheetId
    }
}
