package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.AppDatabase
import com.example.data.ShiftRepository
import com.example.model.MessageSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
            if (messages.isEmpty()) return

            val sender = messages[0].displayOriginatingAddress ?: "Unknown SMS Sender"
            val fullBody = buildString {
                for (sms in messages) {
                    append(sms.displayMessageBody)
                }
            }

            val database = AppDatabase.getDatabase(context)
            val repository = ShiftRepository.getInstance(context)

            CoroutineScope(Dispatchers.IO).launch {
                val prefs = database.userPreferencesDao().getPreferencesDirect()
                if (prefs?.smsMonitoringEnabled == true) {
                    repository.processIncomingMessage(
                        source = MessageSource.SMS,
                        sender = sender,
                        subject = "SMS: Work Shift Update",
                        body = fullBody
                    )
                }
            }
        }
    }
}
