package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.AlarmScheduler
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val database = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val shifts = database.shiftDao().getUpcomingShifts()
                shifts.collect { shiftList ->
                    val now = System.currentTimeMillis()
                    for (entity in shiftList) {
                        val shift = entity.toDomain()
                        if (shift.isAlarmEnabled && shift.alarmScheduledTimeMillis > now) {
                            AlarmScheduler.scheduleShiftAlarm(
                                context,
                                shift,
                                shift.alarmScheduledTimeMillis
                            )
                        }
                    }
                }
            }
        }
    }
}
