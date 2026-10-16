package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple

@Composable
fun PrivacyScreen(
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Privacy & Security Policy",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )

        // Local Storage Guarantee Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LiquidTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("100% Device-Local Persistence", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All shifts, detected messages, and reminders are stored securely on your device via Room Database. No shift dates, employee names, or messages are ever uploaded to third-party advertising or analytics servers.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 17.sp
                )
            }
        }

        // Permissions Rationale
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Android Permissions Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)

                PermissionRationaleRow(
                    icon = Icons.Default.Alarm,
                    title = "Exact Alarms (SCHEDULE_EXACT_ALARM)",
                    reason = "Required to wake the device and fire pre-shift alarms at the exact scheduled minute, even during Doze mode."
                )

                PermissionRationaleRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications (POST_NOTIFICATIONS)",
                    reason = "Required to post heads-up alerts with shift details and audible notifications."
                )

                PermissionRationaleRow(
                    icon = Icons.Default.Sms,
                    title = "SMS Detection (RECEIVE_SMS)",
                    reason = "Optional. Only used if you enable SMS monitoring to catch incoming shift reschedule text messages."
                )
            }
        }

        // Delete Data Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Data Erasure", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Permanently purge all saved shift schedules, detected messages, and active alarms from local storage.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = {
                        onClearAllData()
                        Toast.makeText(context, "All local shift records & alarms deleted.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete All Local Shift Data")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionRationaleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    reason: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ElectricCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(reason, fontSize = 11.sp, color = TextSecondaryDark, lineHeight = 15.sp)
        }
    }
}
