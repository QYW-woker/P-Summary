package com.nickfinance.dashboard.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nickfinance.dashboard.data.local.dao.CellDataDao
import com.nickfinance.dashboard.data.local.dao.ColumnDefDao
import com.nickfinance.dashboard.data.local.dao.DashboardCardDao
import com.nickfinance.dashboard.data.local.dao.RowDataDao
import com.nickfinance.dashboard.data.local.dao.SheetDao
import com.nickfinance.dashboard.data.local.entity.CellDataEntity
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.local.entity.RowDataEntity
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val sheets: List<DataSheetEntity>,
    val columns: List<ColumnDefEntity>,
    val rows: List<RowDataEntity>,
    val cells: List<CellDataEntity>,
    val dashboardCards: List<DashboardCardEntity>
)

@Singleton
class BackupManager @Inject constructor(
    private val sheetDao: SheetDao,
    private val columnDefDao: ColumnDefDao,
    private val rowDataDao: RowDataDao,
    private val cellDataDao: CellDataDao,
    private val dashboardCardDao: DashboardCardDao
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(context: Context): Uri? {
        return try {
            val sheets = sheetDao.getAllSheets().first()
            val allColumns = mutableListOf<ColumnDefEntity>()
            val allRows = mutableListOf<RowDataEntity>()
            val allCells = mutableListOf<CellDataEntity>()

            for (sheet in sheets) {
                val columns = columnDefDao.getColumnsBySheetSync(sheet.id)
                allColumns.addAll(columns)
                val rows = rowDataDao.getRowsBySheetSync(sheet.id)
                allRows.addAll(rows)
                if (rows.isNotEmpty()) {
                    val cells = cellDataDao.getCellsByRowsSync(rows.map { it.id })
                    allCells.addAll(cells)
                }
            }

            val cards = dashboardCardDao.getAllCards().first()

            val backup = BackupData(
                sheets = sheets,
                columns = allColumns,
                rows = allRows,
                cells = allCells,
                dashboardCards = cards
            )

            val json = gson.toJson(backup)
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val fileName = "finance_backup_$dateStr.json"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(json)

            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun importFromJson(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val json = inputStream.bufferedReader().use { it.readText() }
            val backup = gson.fromJson(json, BackupData::class.java)

            // Clear existing data
            dashboardCardDao.deleteAll()
            // Sheets cascade delete handles columns, rows, cells
            val existingSheets = sheetDao.getAllSheets().first()
            existingSheets.forEach { sheetDao.deleteSheet(it) }

            // Restore sheets (without IDs to let auto-generate work, mapping old to new)
            val sheetIdMap = mutableMapOf<Long, Long>()
            for (sheet in backup.sheets) {
                val newId = sheetDao.insertSheet(sheet.copy(id = 0))
                sheetIdMap[sheet.id] = newId
            }

            // Restore columns
            val columnIdMap = mutableMapOf<Long, Long>()
            for (col in backup.columns) {
                val newSheetId = sheetIdMap[col.sheetId] ?: continue
                val newId = columnDefDao.insertColumn(col.copy(id = 0, sheetId = newSheetId))
                columnIdMap[col.id] = newId
            }

            // Restore rows
            val rowIdMap = mutableMapOf<Long, Long>()
            for (row in backup.rows) {
                val newSheetId = sheetIdMap[row.sheetId] ?: continue
                val newId = rowDataDao.insertRow(row.copy(id = 0, sheetId = newSheetId))
                rowIdMap[row.id] = newId
            }

            // Restore cells
            for (cell in backup.cells) {
                val newRowId = rowIdMap[cell.rowId] ?: continue
                val newColumnId = columnIdMap[cell.columnId] ?: continue
                cellDataDao.upsertCell(cell.copy(id = 0, rowId = newRowId, columnId = newColumnId))
            }

            // Restore dashboard cards
            for (card in backup.dashboardCards) {
                val newSheetId = sheetIdMap[card.dataSheetId] ?: continue
                val newXAxisColId = card.xAxisColumnId?.let { columnIdMap[it] }
                val newSelectedColIds = card.selectedColumnIds.split(",")
                    .mapNotNull { it.trim().toLongOrNull() }
                    .mapNotNull { columnIdMap[it] }
                    .joinToString(",")
                val newCurrentValueColId = card.currentValueColumnId?.let { columnIdMap[it] }
                val newStatColId = card.statColumnId?.let { columnIdMap[it] }

                dashboardCardDao.insertCard(
                    card.copy(
                        id = 0,
                        dataSheetId = newSheetId,
                        xAxisColumnId = newXAxisColId,
                        selectedColumnIds = newSelectedColIds,
                        currentValueColumnId = newCurrentValueColId,
                        statColumnId = newStatColId
                    )
                )
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun exportToCsv(context: Context, sheetIds: List<Long>): List<Uri> {
        val uris = mutableListOf<Uri>()
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        for (sheetId in sheetIds) {
            try {
                val sheet = sheetDao.getSheetById(sheetId) ?: continue
                val columns = columnDefDao.getColumnsBySheetSync(sheetId)
                val rows = rowDataDao.getRowsBySheetSync(sheetId)
                val cells = if (rows.isNotEmpty()) {
                    cellDataDao.getCellsByRowsSync(rows.map { it.id })
                } else emptyList()
                val cellMap = cells.groupBy { it.rowId }

                val sb = StringBuilder()
                // UTF-8 BOM for Excel compatibility
                sb.append("\uFEFF")
                // Header row
                sb.appendLine(columns.joinToString(",") { escCsv(it.name) })
                // Data rows
                for (row in rows) {
                    val rowCells = cellMap[row.id] ?: emptyList()
                    val cellValMap = rowCells.associate { it.columnId to it.value }
                    val values = columns.map { col ->
                        val raw = cellValMap[col.id] ?: ""
                        escCsv(raw)
                    }
                    sb.appendLine(values.joinToString(","))
                }

                val fileName = "${sheet.name}.csv"
                val file = File(downloadsDir, fileName)
                file.writeText(sb.toString())
                uris.add(Uri.fromFile(file))
            } catch (e: Exception) {
                // Skip failed sheets
            }
        }
        return uris
    }

    private fun escCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
