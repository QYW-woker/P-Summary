package com.nickfinance.dashboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nickfinance.dashboard.data.local.dao.CellDataDao
import com.nickfinance.dashboard.data.local.dao.ColumnDefDao
import com.nickfinance.dashboard.data.local.dao.DashboardCardDao
import com.nickfinance.dashboard.data.local.dao.RowDataDao
import com.nickfinance.dashboard.data.local.dao.SheetDao
import com.nickfinance.dashboard.data.local.entity.CellDataEntity
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.local.entity.RowDataEntity

@Database(
    entities = [
        DataSheetEntity::class,
        ColumnDefEntity::class,
        RowDataEntity::class,
        CellDataEntity::class,
        DashboardCardEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sheetDao(): SheetDao
    abstract fun columnDefDao(): ColumnDefDao
    abstract fun rowDataDao(): RowDataDao
    abstract fun cellDataDao(): CellDataDao
    abstract fun dashboardCardDao(): DashboardCardDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE column_defs ADD COLUMN columnRole TEXT DEFAULT NULL")
            }
        }
    }
}
