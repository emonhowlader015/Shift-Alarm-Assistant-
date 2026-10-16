package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ShiftEntity::class,
        DetectedMessageEntity::class,
        SenderFilterEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftDao(): ShiftDao
    abstract fun detectedMessageDao(): DetectedMessageDao
    abstract fun senderFilterDao(): SenderFilterDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shift_alarm_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial default filters and preferences
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaults(database)
                    }
                }
            }
        }

        suspend fun seedDefaults(database: AppDatabase) {
            val filterDao = database.senderFilterDao()
            val prefDao = database.userPreferencesDao()

            // Default suggested sender filter per prompt specification: "CuttingEdger-PayRol"
            filterDao.insertFilters(
                listOf(
                    SenderFilterEntity(
                        pattern = "CuttingEdger-PayRol",
                        isSenderName = true,
                        isEnabled = true,
                        isDefault = true
                    ),
                    SenderFilterEntity(
                        pattern = "Shift Re-scheduled",
                        isKeyword = true,
                        isEnabled = true,
                        isDefault = true
                    ),
                    SenderFilterEntity(
                        pattern = "Shift Rescheduled",
                        isKeyword = true,
                        isEnabled = true,
                        isDefault = true
                    ),
                    SenderFilterEntity(
                        pattern = "Shift Schedule Updated",
                        isKeyword = true,
                        isEnabled = true,
                        isDefault = true
                    ),
                    SenderFilterEntity(
                        pattern = "Shift Timing Changed",
                        isKeyword = true,
                        isEnabled = true,
                        isDefault = true
                    ),
                    SenderFilterEntity(
                        pattern = "Updated Shift",
                        isKeyword = true,
                        isEnabled = true,
                        isDefault = true
                    )
                )
            )

            // Seed initial preferences
            prefDao.savePreferences(
                UserPreferencesEntity(
                    id = 1,
                    userEmployeeName = "",
                    isEmployeeFilterEnabled = false,
                    defaultReminderMinutes = 120, // 2 hours before
                    alarmDestination = "BOTH",
                    waterDropAnimationsEnabled = true,
                    smsMonitoringEnabled = false,
                    soundEnabled = true,
                    vibrationEnabled = true,
                    hasCompletedOnboarding = false
                )
            )
        }
    }
}
