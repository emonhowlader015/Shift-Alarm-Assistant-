package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_SHIFT_ALARM = "shift_alarm_channel"
    const val CHANNEL_SYNC_STATUS = "shift_sync_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                CHANNEL_SHIFT_ALARM,
                "Shift Reminders & Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarms and pre-shift reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 1000)
                setSound(alarmSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_STATUS,
                "Gmail & Shift Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Status of shift synchronization"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(syncChannel)
        }
    }

    fun showShiftAlarmNotification(
        context: Context,
        notificationId: Int,
        shiftId: Long,
        title: String,
        message: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SHIFT_ID", shiftId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(context, CHANNEL_SHIFT_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(alarmSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 1000))
            .setFullScreenIntent(pendingIntent, true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission might not be granted
        }
    }
}
