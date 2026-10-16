package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferencesEntity
import com.example.model.AlarmDestination
import com.example.model.Shift
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingMagenta
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightCardBorder
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple

@Composable
fun AlarmManagerScreen(
    userPreferences: UserPreferencesEntity,
    upcomingShifts: List<Shift>,
    onUpdatePreferences: (UserPreferencesEntity) -> Unit,
    onTestInternalAlarm: () -> Unit
) {
    val context = LocalContext.current
    var destination by remember(userPreferences) {
        mutableStateOf(userPreferences.alarmDestination)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Alarm Manager & Clock Integration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )
        Text(
            text = "Configure reminder delivery between the internal Android AlarmManager scheduler and the phone's native Clock app.",
            fontSize = 13.sp,
            color = TextSecondaryDark
        )

        // Alarm Destination Selector Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Alarm Destination", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Choose where your shift reminder alarms are routed.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                DestinationOption(
                    title = "Dual Destination (Recommended)",
                    description = "Schedules date-specific exact AlarmManager alarm AND opens native Clock app pre-filled with shift label.",
                    selected = destination == AlarmDestination.BOTH.name,
                    onClick = {
                        destination = AlarmDestination.BOTH.name
                        onUpdatePreferences(userPreferences.copy(alarmDestination = destination))
                    }
                )

                DestinationOption(
                    title = "Shift Alarm Assistant Internal Scheduler",
                    description = "Exact date-specific alarms managed via Android AlarmManager with full-screen heads-up alerts.",
                    selected = destination == AlarmDestination.INTERNAL_ONLY.name,
                    onClick = {
                        destination = AlarmDestination.INTERNAL_ONLY.name
                        onUpdatePreferences(userPreferences.copy(alarmDestination = destination))
                    }
                )

                DestinationOption(
                    title = "Phone Default Clock App",
                    description = "Dispatches AlarmClock.ACTION_SET_ALARM intent to your device's built-in Clock application.",
                    selected = destination == AlarmDestination.SYSTEM_CLOCK_ONLY.name,
                    onClick = {
                        destination = AlarmDestination.SYSTEM_CLOCK_ONLY.name
                        onUpdatePreferences(userPreferences.copy(alarmDestination = destination))
                    }
                )
            }
        }

        // Native Clock Integration Disclaimer & Limits Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Android Clock Intent Notice", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The standard Android system intent (`AlarmClock.ACTION_SET_ALARM`) pre-fills the hour, minute, and label (`SHIFT REMINDER - [Date] - [Time]`). Because Android's Clock intent does not universally guarantee a single-date trigger, Shift Alarm Assistant maintains its internal date-specific AlarmManager scheduler to ensure reliability.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 17.sp
                )
            }
        }

        // Live Test Controls
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Verification & Diagnostics", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)

                // Test Internal Alarm Button
                WaterDropSurface(
                    onClick = {
                        onTestInternalAlarm()
                        Toast.makeText(context, "Test alarm notification fired!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MidnightNavyElevated,
                    borderColor = GlowingMagenta.copy(alpha = 0.5f),
                    borderWidth = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GlowingMagenta.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GlowingMagenta, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Test Internal Alarm & Notification", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("Triggers instant high-priority ring & vibration", fontSize = 11.sp, color = TextMutedDark)
                        }
                    }
                }

                // Test Native Clock Intent Button
                WaterDropSurface(
                    onClick = {
                        try {
                            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                putExtra(AlarmClock.EXTRA_HOUR, 10)
                                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                                putExtra(AlarmClock.EXTRA_MESSAGE, "SHIFT REMINDER - TEST ALARM")
                                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No default clock app found: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MidnightNavyElevated,
                    borderColor = ElectricBlue.copy(alpha = 0.5f),
                    borderWidth = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Test Phone Default Clock Intent", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("Launches system Clock pre-filled with sample shift", fontSize = 11.sp, color = TextMutedDark)
                        }
                    }
                }
            }
        }

        // Active Alarms Status
        val activeCount = upcomingShifts.count { it.isAlarmEnabled }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scheduler Queue", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text("$activeCount Alarms Armed", fontSize = 12.sp, color = LiquidTeal, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alarms persist across device restarts via BootReceiver and automatically adjust on timezone transitions.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DestinationOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = ElectricCyan, unselectedColor = TextMutedDark)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(description, fontSize = 11.sp, color = TextSecondaryDark, lineHeight = 15.sp)
        }
    }
}
