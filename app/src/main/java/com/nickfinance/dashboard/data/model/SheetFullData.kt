package com.nickfinance.dashboard.data.model

import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.local.entity.RowDataEntity

data class SheetFullData(
    val sheet: DataSheetEntity,
    val columns: List<ColumnDefEntity>,
    val rows: List<RowWithCells>
)

data class RowWithCells(
    val row: RowDataEntity,
    val cells: Map<Long, String> // columnId → value
)
