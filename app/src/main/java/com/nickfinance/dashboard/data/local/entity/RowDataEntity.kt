package com.nickfinance.dashboard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "row_data",
    foreignKeys = [ForeignKey(
        entity = DataSheetEntity::class,
        parentColumns = ["id"],
        childColumns = ["sheetId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sheetId")]
)
data class RowDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sheetId: Long,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
