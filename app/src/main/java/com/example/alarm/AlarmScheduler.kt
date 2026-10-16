package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.model.Shift
import com.example.receiver.AlarmReceiver

object AlarmScheduler {
    const val ACTION_SHIFT_ALARM = "com.example.ACTION_SHIFT_ALARM"

    fun scheduleShiftAlarm(context: Context, shift: Shift, reminderEpochMillis: Long): Boolean {
        if (reminderEpochMillis <= System.currentTimeMillis()) {
            return false // In the past, don't schedule
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SHIFT_ALARM
            putExtra("EXTRA_SHIFT_ID", shift.id)
            putExtra("EXTRA_EMPLOYEE_NAME", shift.employeeName)
            putExtra("EXTRA_SHIFT_DATE", shift.dateString)
            putExtra("EXTRA_SHIFT_TIMES", shift.displayTimeRange)
            putExtra("EXTRA_SHIFT_ROLE", shift.department ?: "Work Shift")
        }

        val requestCode = (shift.id % 100000).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderEpochMillis,
                        pendingIntent
                    )
                    return true
                } else {
                    // Fallback to inexact if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderEpochMillis,
                        pendingIntent
                    )
                    return true
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderEpochMillis,
                    pendingIntent
                )
                return true
            }
        } catch (_: SecurityException) {
            return false
        }
    }

    fun cancelShiftAlarm(context: Context, shiftId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SHIFT_ALARM
        }
        val requestCode = (shiftId % 100000).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
