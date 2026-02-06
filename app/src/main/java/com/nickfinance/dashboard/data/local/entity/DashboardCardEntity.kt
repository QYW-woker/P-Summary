package com.nickfinance.dashboard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_cards")
data class DashboardCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val chartType: String, // LINE / BAR / PIE / AREA / PROGRESS / STAT_NUMBER / COMBINED
    val dataSheetId: Long,
    val xAxisColumnId: Long? = null,
    val selectedColumnIds: String = "", // comma-separated "1,3,5"
    val colorMapping: String? = null, // JSON: {"columnId": "#color"}
    val cardSize: String = "FULL", // FULL / HALF
    val sortOrder: Int = 0,
    val targetValue: Double? = null,
    val currentValueColumnId: Long? = null,
    val statColumnId: Long? = null,
    val statRowIndex: Int? = null // -1 = latest row
)
