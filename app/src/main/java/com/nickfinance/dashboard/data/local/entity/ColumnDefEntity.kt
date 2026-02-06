package com.nickfinance.dashboard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "column_defs",
    foreignKeys = [ForeignKey(
        entity = DataSheetEntity::class,
        parentColumns = ["id"],
        childColumns = ["sheetId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sheetId")]
)
data class ColumnDefEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sheetId: Long,
    val name: String,
    val type: String, // DATE / NUMBER / CURRENCY / PERCENTAGE / TEXT
    val groupName: String? = null,
    val groupColor: String? = null,
    val sortOrder: Int = 0,
    val isRequired: Boolean = false,
    val formula: String? = null,
    val isNegativeRed: Boolean = false
)
