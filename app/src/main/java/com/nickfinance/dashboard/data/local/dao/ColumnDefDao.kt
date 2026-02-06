package com.nickfinance.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColumnDefDao {
    @Query("SELECT * FROM column_defs WHERE sheetId = :sheetId ORDER BY sortOrder ASC")
    fun getColumnsBySheet(sheetId: Long): Flow<List<ColumnDefEntity>>

    @Query("SELECT * FROM column_defs WHERE sheetId = :sheetId ORDER BY sortOrder ASC")
    suspend fun getColumnsBySheetSync(sheetId: Long): List<ColumnDefEntity>

    @Query("SELECT * FROM column_defs WHERE id = :id")
    suspend fun getColumnById(id: Long): ColumnDefEntity?

    @Insert
    suspend fun insertColumn(column: ColumnDefEntity): Long

    @Insert
    suspend fun insertColumns(columns: List<ColumnDefEntity>)

    @Update
    suspend fun updateColumn(column: ColumnDefEntity)

    @Delete
    suspend fun deleteColumn(column: ColumnDefEntity)

    @Query("DELETE FROM column_defs WHERE id = :id")
    suspend fun deleteColumnById(id: Long)

    @Query("SELECT MAX(sortOrder) FROM column_defs WHERE sheetId = :sheetId")
    suspend fun getMaxSortOrder(sheetId: Long): Int?

    @Query("UPDATE column_defs SET sortOrder = :newOrder WHERE id = :columnId")
    suspend fun updateSortOrder(columnId: Long, newOrder: Int)
}
