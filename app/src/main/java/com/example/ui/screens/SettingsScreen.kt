package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferencesEntity
import com.example.ui.AppScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple

@Composable
fun SettingsScreen(
    userPreferences: UserPreferencesEntity,
    onUpdatePreferences: (UserPreferencesEntity) -> Unit,
    onNavigateTo: (AppScreen) -> Unit
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
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )

        // General Preferences Group
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("App Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)

                // Water-drop animation toggle
                SettingToggleRow(
                    icon = Icons.Default.WaterDrop,
                    title = "Liquid RGB Water-Drop Animations",
                    subtitle = "Touch ripple and glowing perimeter animations",
                    checked = userPreferences.waterDropAnimationsEnabled,
                    onCheckedChange = {
                        onUpdatePreferences(userPreferences.copy(waterDropAnimationsEnabled = it))
                    }
                )

                // Sound enabled toggle
                SettingToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Alarm Ringtone Sound",
                    subtitle = "Audible alarm sound for high-priority shift reminder",
                    checked = userPreferences.soundEnabled,
                    onCheckedChange = {
                        onUpdatePreferences(userPreferences.copy(soundEnabled = it))
                    }
                )

                // Vibration enabled toggle
                SettingToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Alarm Vibration",
                    subtitle = "Haptic feedback and vibration pattern",
                    checked = userPreferences.vibrationEnabled,
                    onCheckedChange = {
                        onUpdatePreferences(userPreferences.copy(vibrationEnabled = it))
                    }
                )

                // SMS Monitoring toggle
                SettingToggleRow(
                    icon = Icons.Default.Sms,
                    title = "SMS Shift Detection",
                    subtitle = "Automatically catch incoming shift text notifications",
                    checked = userPreferences.smsMonitoringEnabled,
                    onCheckedChange = {
                        onUpdatePreferences(userPreferences.copy(smsMonitoringEnabled = it))
                        if (it) {
                            Toast.makeText(context, "SMS monitoring enabled. Grant permission when prompted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // Navigation Links
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Configuration & Subsystems", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)

                SettingNavigationRow(
                    icon = Icons.Default.Tune,
                    title = "Reminder Timing Studio",
                    subtitle = "Current default: ${userPreferences.defaultReminderMinutes / 60}h before shift",
                    onClick = { onNavigateTo(AppScreen.REMINDER_STUDIO) }
                )

                SettingNavigationRow(
                    icon = Icons.Default.Email,
                    title = "Gmail Synchronization",
                    subtitle = if (!userPreferences.gmailConnectedAccount.isNullOrBlank()) userPreferences.gmailConnectedAccount ?: "" else "Connect account",
                    onClick = { onNavigateTo(AppScreen.GMAIL_ACCOUNTS) }
                )

                SettingNavigationRow(
                    icon = Icons.Default.FilterList,
                    title = "Sender & Keyword Filters",
                    subtitle = "Manage whitelist and employee name filter",
                    onClick = { onNavigateTo(AppScreen.SENDER_FILTERS) }
                )

                SettingNavigationRow(
                    icon = Icons.Default.Alarm,
                    title = "Alarm Manager Destination",
                    subtitle = "Internal scheduler & system Clock app setup",
                    onClick = { onNavigateTo(AppScreen.ALARM_MANAGER) }
                )

                SettingNavigationRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy & Security",
                    subtitle = "Local Room database storage & offline status",
                    onClick = { onNavigateTo(AppScreen.PRIVACY) }
                )

                SettingNavigationRow(
                    icon = Icons.Default.Info,
                    title = "About Shift Alarm Assistant",
                    subtitle = "Developer credits & technical details",
                    onClick = { onNavigateTo(AppScreen.ABOUT) }
                )
            }
        }

        // Developer Credit Card (Required in Settings)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Shift Alarm Assistant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Developed by Emon Howlader",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Text(subtitle, fontSize = 11.sp, color = TextMutedDark)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MidnightDark,
                checkedTrackColor = ElectricCyan,
                uncheckedThumbColor = TextMutedDark,
                uncheckedTrackColor = MidnightNavyElevated
            )
        )
    }
}

@Composable
fun SettingNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = VividPurple, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Text(subtitle, fontSize = 11.sp, color = TextSecondaryDark)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(16.dp))
    }
}
