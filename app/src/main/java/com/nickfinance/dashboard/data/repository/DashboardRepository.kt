package com.nickfinance.dashboard.data.repository

import com.nickfinance.dashboard.data.local.dao.DashboardCardDao
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val dashboardCardDao: DashboardCardDao
) {
    fun getAllCards(): Flow<List<DashboardCardEntity>> = dashboardCardDao.getAllCards()

    suspend fun getCardById(id: Long): DashboardCardEntity? = dashboardCardDao.getCardById(id)

    suspend fun insertCard(card: DashboardCardEntity): Long {
        val maxOrder = dashboardCardDao.getMaxSortOrder() ?: -1
        return dashboardCardDao.insertCard(card.copy(sortOrder = maxOrder + 1))
    }

    suspend fun updateCard(card: DashboardCardEntity) {
        dashboardCardDao.updateCard(card)
    }

    suspend fun deleteCard(id: Long) {
        dashboardCardDao.deleteCardById(id)
    }
}
