package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shiftId = intent.getLongExtra("EXTRA_SHIFT_ID", 0L)
        val employeeName = intent.getStringExtra("EXTRA_EMPLOYEE_NAME") ?: "Employee"
        val shiftDate = intent.getStringExtra("EXTRA_SHIFT_DATE") ?: "Today"
        val shiftTimes = intent.getStringExtra("EXTRA_SHIFT_TIMES") ?: ""
        val role = intent.getStringExtra("EXTRA_SHIFT_ROLE") ?: "Work Shift"

        val title = "⏰ Work Shift Reminder - $shiftTimes"
        val message = "$employeeName, your shift is starting soon! ($shiftDate: $shiftTimes) - $role"

        val notificationId = (shiftId % 100000).toInt().coerceAtLeast(101)
        NotificationHelper.showShiftAlarmNotification(
            context = context,
            notificationId = notificationId,
            shiftId = shiftId,
            title = title,
            message = message
        )
    }
}
