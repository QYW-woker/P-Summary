package com.nickfinance.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nickfinance.dashboard.data.local.entity.CellDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CellDataDao {
    @Query("SELECT * FROM cell_data WHERE rowId IN (:rowIds)")
    fun getCellsByRows(rowIds: List<Long>): Flow<List<CellDataEntity>>

    @Query("SELECT * FROM cell_data WHERE rowId IN (:rowIds)")
    suspend fun getCellsByRowsSync(rowIds: List<Long>): List<CellDataEntity>

    @Query("SELECT * FROM cell_data WHERE rowId = :rowId AND columnId = :columnId")
    suspend fun getCell(rowId: Long, columnId: Long): CellDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCell(cell: CellDataEntity)

    @Query("UPDATE cell_data SET value = :value WHERE rowId = :rowId AND columnId = :columnId")
    suspend fun updateCellValue(rowId: Long, columnId: Long, value: String)

    @Delete
    suspend fun deleteCell(cell: CellDataEntity)

    @Query("DELETE FROM cell_data")
    suspend fun deleteAll()
}
