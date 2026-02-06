package com.nickfinance.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nickfinance.dashboard.data.local.entity.RowDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RowDataDao {
    @Query("SELECT * FROM row_data WHERE sheetId = :sheetId ORDER BY sortOrder ASC")
    fun getRowsBySheet(sheetId: Long): Flow<List<RowDataEntity>>

    @Query("SELECT * FROM row_data WHERE sheetId = :sheetId ORDER BY sortOrder ASC")
    suspend fun getRowsBySheetSync(sheetId: Long): List<RowDataEntity>

    @Query("SELECT * FROM row_data WHERE id = :id")
    suspend fun getRowById(id: Long): RowDataEntity?

    @Insert
    suspend fun insertRow(row: RowDataEntity): Long

    @Delete
    suspend fun deleteRow(row: RowDataEntity)

    @Query("DELETE FROM row_data WHERE id = :id")
    suspend fun deleteRowById(id: Long)

    @Query("SELECT MAX(sortOrder) FROM row_data WHERE sheetId = :sheetId")
    suspend fun getMaxSortOrder(sheetId: Long): Int?
}
