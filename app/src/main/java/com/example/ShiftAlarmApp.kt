package com.example

import android.app.Application
import com.example.alarm.NotificationHelper
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShiftAlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        val database = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure defaults are seeded if needed
            val filters = database.senderFilterDao().getActiveFilters()
            if (filters.isEmpty()) {
                AppDatabase.seedDefaults(database)
            }
        }
    }
}
