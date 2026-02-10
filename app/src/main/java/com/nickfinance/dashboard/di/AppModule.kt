package com.nickfinance.dashboard.di

import android.content.Context
import androidx.room.Room
import com.nickfinance.dashboard.data.local.AppDatabase
import com.nickfinance.dashboard.data.local.dao.CellDataDao
import com.nickfinance.dashboard.data.local.dao.ColumnDefDao
import com.nickfinance.dashboard.data.local.dao.DashboardCardDao
import com.nickfinance.dashboard.data.local.dao.RowDataDao
import com.nickfinance.dashboard.data.local.dao.SheetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_dashboard.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideSheetDao(db: AppDatabase): SheetDao = db.sheetDao()

    @Provides
    fun provideColumnDefDao(db: AppDatabase): ColumnDefDao = db.columnDefDao()

    @Provides
    fun provideRowDataDao(db: AppDatabase): RowDataDao = db.rowDataDao()

    @Provides
    fun provideCellDataDao(db: AppDatabase): CellDataDao = db.cellDataDao()

    @Provides
    fun provideDashboardCardDao(db: AppDatabase): DashboardCardDao = db.dashboardCardDao()
}
