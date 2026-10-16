package com.example.alarm

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import com.example.model.Shift
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object NativeClockHelper {

    /**
     * Integrates with the phone's default Clock app using AlarmClock.ACTION_SET_ALARM.
     * Label: SHIFT REMINDER - [Actual Shift Date] - [Actual Shift Time]
     */
    fun setAlarmInDefaultClock(
        context: Context,
        shift: Shift,
        alarmTimeMillis: Long,
        skipUi: Boolean = false
    ): Boolean {
        try {
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(alarmTimeMillis),
                ZoneId.systemDefault()
            )

            val hour = dateTime.hour
            val minute = dateTime.minute
            val label = "SHIFT REMINDER - ${shift.dateString} - ${shift.displayTimeRange}"

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
                putExtra(AlarmClock.EXTRA_VIBRATE, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            } else {
                Toast.makeText(
                    context,
                    "No compatible default Clock app found. Internal Alarm remains active.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open Clock app: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
    }
}
