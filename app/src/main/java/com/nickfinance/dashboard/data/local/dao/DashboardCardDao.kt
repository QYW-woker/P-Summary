package com.nickfinance.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardCardDao {
    @Query("SELECT * FROM dashboard_cards ORDER BY sortOrder ASC")
    fun getAllCards(): Flow<List<DashboardCardEntity>>

    @Query("SELECT * FROM dashboard_cards WHERE id = :id")
    suspend fun getCardById(id: Long): DashboardCardEntity?

    @Insert
    suspend fun insertCard(card: DashboardCardEntity): Long

    @Update
    suspend fun updateCard(card: DashboardCardEntity)

    @Delete
    suspend fun deleteCard(card: DashboardCardEntity)

    @Query("DELETE FROM dashboard_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("SELECT MAX(sortOrder) FROM dashboard_cards")
    suspend fun getMaxSortOrder(): Int?

    @Query("DELETE FROM dashboard_cards")
    suspend fun deleteAll()
}
