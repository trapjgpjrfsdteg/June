package com.denser.june.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.denser.june.core.data.database.journal.JournalDatabase

class DatabaseFactory(
    private val context: Context
) {
    fun createJournalDatabase(): RoomDatabase.Builder<JournalDatabase> {
        val appContext = context.applicationContext

        return Room.databaseBuilder(
            appContext,
            JournalDatabase::class.java,
            JournalDatabase.DB_NAME
        ).apply {
            addMigrations(
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4,
                DatabaseMigrations.MIGRATION_4_5
            )
        }
    }
}
