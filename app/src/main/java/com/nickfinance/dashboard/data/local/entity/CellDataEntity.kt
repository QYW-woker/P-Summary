package com.nickfinance.dashboard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cell_data",
    foreignKeys = [
        ForeignKey(
            entity = RowDataEntity::class,
            parentColumns = ["id"],
            childColumns = ["rowId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ColumnDefEntity::class,
            parentColumns = ["id"],
            childColumns = ["columnId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("rowId"),
        Index("columnId"),
        Index(value = ["rowId", "columnId"], unique = true)
    ]
)
data class CellDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rowId: Long,
    val columnId: Long,
    val value: String = ""
)
