package com.nickfinance.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SheetDao {
    @Query("SELECT * FROM data_sheets ORDER BY updatedAt DESC")
    fun getAllSheets(): Flow<List<DataSheetEntity>>

    @Query("SELECT * FROM data_sheets WHERE id = :id")
    suspend fun getSheetById(id: Long): DataSheetEntity?

    @Query("SELECT * FROM data_sheets WHERE name = :name LIMIT 1")
    suspend fun getSheetByName(name: String): DataSheetEntity?

    @Insert
    suspend fun insertSheet(sheet: DataSheetEntity): Long

    @Update
    suspend fun updateSheet(sheet: DataSheetEntity)

    @Delete
    suspend fun deleteSheet(sheet: DataSheetEntity)

    @Query("DELETE FROM data_sheets WHERE id = :id")
    suspend fun deleteSheetById(id: Long)

    @Query("SELECT COUNT(*) FROM row_data WHERE sheetId = :sheetId")
    fun getRowCount(sheetId: Long): Flow<Int>
}
